package zs.wallpapers.site.hdwallpaper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import zs.wallpapers.site.R;
import zs.wallpapers.site.asyncTask.LoadAbout;
import zs.wallpapers.site.fragments.FragmentDashboard;
import zs.wallpapers.site.fragments.FragmentFavourite;
import zs.wallpapers.site.fragments.FragmentGIFs;



import zs.wallpapers.site.interfaces.AboutListener;
import zs.wallpapers.site.utils.AdConsent;
import zs.wallpapers.site.interfaces.AdConsentListener;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;
import zs.wallpapers.site.utils.SharedPref;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSIONS_REQUEST_BIND_WALLPAPER = 103;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    Methods methods;
    DBHelper dbHelper;
    FragmentManager fm;
    LoadAbout loadAbout;
    LinearLayout ll_ad;
    Toolbar toolbar;
    AdConsent adConsent;
    DrawerLayout drawer;
    SharedPref sharedPref;
    NavigationView navigationView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = new SharedPref(this);
        dbHelper = new DBHelper(this);
        methods = new Methods(this);
        methods.setStatusColor(getWindow());
        methods.forceRTLIfSupported(getWindow());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ll_ad = findViewById(R.id.ll_ad_main);

        fm = getSupportFragmentManager();

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        toggle.setHomeAsUpIndicator(R.mipmap.nav);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        toggle.setDrawerIndicatorEnabled(false);

        Constant.isGIFEnabled = sharedPref.getIsGIF();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        hideGIFMenu();

        FragmentDashboard f1 = new FragmentDashboard();
        loadFrag(f1, getResources().getString(R.string.dashboard), fm);
        getSupportActionBar().setTitle(getResources().getString(R.string.home));
        navigationView.setCheckedItem(R.id.nav_home);

        adConsent = new AdConsent(this, new AdConsentListener() {
            @Override
            public void onConsentUpdate() {
                methods.showBannerAd(ll_ad);
            }
        });

        if (methods.isNetworkAvailable()) {
            loadAbout = new LoadAbout(new AboutListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onEnd(Boolean success) {
                    adConsent.checkForConsent();
                    dbHelper.addtoAbout();
                    sharedPref.setIsGIF(Constant.isGIFEnabled);
                    hideGIFMenu();
                }
            });
            loadAbout.execute();
        } else {
            adConsent.checkForConsent();
            dbHelper.getAbout();
        }

        checkPer();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() != 0) {
            String title = fm.getFragments().get(fm.getBackStackEntryCount()).getTag();
            if (title.equals(getString(R.string.dashboard)) || title.equals(getString(R.string.home))) {
                title = getString(R.string.home);
                navigationView.setCheckedItem(R.id.nav_home);
            }
            getSupportActionBar().setTitle(title);
            super.onBackPressed();
        } else {
            exitDialog();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        clickNav(item.getItemId());
        return true;
    }

    private void clickNav(int item) {
        switch (item) {
            case R.id.nav_home:
                FragmentDashboard fhome = new FragmentDashboard();
                loadFrag(fhome, getResources().getString(R.string.dashboard), fm);
                getSupportActionBar().setTitle(getResources().getString(R.string.home));
                break;
            case R.id.nav_gif:
                FragmentGIFs fgif = new FragmentGIFs();
                loadFrag(fgif, getResources().getString(R.string.gifs), fm);
                getSupportActionBar().setTitle(getResources().getString(R.string.gifs));
                break;
            case R.id.nav_fav:
                FragmentFavourite ffav = new FragmentFavourite();
                loadFrag(ffav, getResources().getString(R.string.favourite), fm);
                toolbar.setTitle(getResources().getString(R.string.favourite));
                break;
            case R.id.nav_setting:
                Intent intent_set = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent_set);
                break;
            case R.id.nav_shareapp:
                Intent ishare = new Intent(Intent.ACTION_SEND);
                ishare.setType("text/plain");
                ishare.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(ishare);
                break;
            case R.id.nav_about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (!name.equals(getString(R.string.dashboard))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.frame_layout, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.frame_layout, f1, name);
        }

        ft.commit();
    }

    private void hideGIFMenu() {
        if (!Constant.isGIFEnabled && navigationView != null) {
            navigationView.getMenu().getItem(1).setVisible(false);
        } else {
            navigationView.getMenu().getItem(1).setVisible(true);
        }
    }

    private void exitDialog() {
        AlertDialog.Builder alert;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert = new AlertDialog.Builder(MainActivity.this, R.style.ThemeDialog);
        } else {
            alert = new AlertDialog.Builder(MainActivity.this);
        }

        alert.setTitle(getString(R.string.exit));
        alert.setMessage(getString(R.string.sure_exit));
        alert.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alert.show();
    }

    public void checkPer() {
        if ((ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
        if ((ContextCompat.checkSelfPermission
                (MainActivity.this, "android.permission.BIND_WALLPAPER")
                != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.BIND_WALLPAPER"},
                        MY_PERMISSIONS_REQUEST_BIND_WALLPAPER);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
            case MY_PERMISSIONS_REQUEST_BIND_WALLPAPER: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }
}