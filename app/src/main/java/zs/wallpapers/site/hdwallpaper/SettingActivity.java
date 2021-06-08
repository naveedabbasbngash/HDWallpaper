package zs.wallpapers.site.hdwallpaper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.onesignal.OneSignal;


import zs.wallpapers.site.R;
import zs.wallpapers.site.utils.AdConsent;
import zs.wallpapers.site.interfaces.AdConsentListener;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.Methods;
import zs.wallpapers.site.utils.SharedPref;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingActivity extends AppCompatActivity {

    Toolbar toolbar;
    Methods methods;
    SharedPref sharedPref;
    AdConsent adConsent;
    LinearLayout ll_consent, ll_adView, ll_clearcache;
    SwitchCompat switch_consent, switch_noti;
    Boolean isNoti = true;
    View view_moreapp;
    ProgressDialog progressDialog;
    TextView tv_privacy, tv_moreapp, tv_rateapp, tv_cachesize;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.setStatusColor(getWindow());
        methods.forceRTLIfSupported(getWindow());

        isNoti = sharedPref.getIsNotification();

        toolbar = this.findViewById(R.id.toolbar_setting);
        toolbar.setTitle(getString(R.string.action_settings));
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(SettingActivity.this);
        progressDialog.setMessage(getString(R.string.clearing_cache));

        adConsent = new AdConsent(this, new AdConsentListener() {
            @Override
            public void onConsentUpdate() {
                setConsentSwitch();
            }
        });

        ll_consent = findViewById(R.id.ll_consent);
        switch_noti = findViewById(R.id.switch_noti);
        switch_consent = findViewById(R.id.switch_consent);
        tv_rateapp = findViewById(R.id.tv_rateapp);
        tv_moreapp = findViewById(R.id.tv_moreapp);
        tv_privacy = findViewById(R.id.tv_privacy);
        tv_cachesize = findViewById(R.id.tv_cachesize);
        ll_adView = findViewById(R.id.ll_adView);
        ll_clearcache = findViewById(R.id.ll_cache);
        view_moreapp = findViewById(R.id.view_moreapp);
        methods.showBannerAd(ll_adView);

        if(getString(R.string.play_more_apps).equals("")) {
            view_moreapp.setVisibility(View.GONE);
            tv_moreapp.setVisibility(View.GONE);
        }

        initializeCache();

        if (adConsent.isUserFromEEA()) {
            setConsentSwitch();
        } else {
            ll_consent.setVisibility(View.GONE);
        }
        if (isNoti) {
            switch_noti.setChecked(true);
        } else {
            switch_noti.setChecked(false);
        }

        switch_noti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                OneSignal.setSubscription(isChecked);
                sharedPref.setIsNotification(isChecked);
            }
        });

        switch_consent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ConsentInformation.getInstance(SettingActivity.this).setConsentStatus(ConsentStatus.PERSONALIZED);
                } else {
                    ConsentInformation.getInstance(SettingActivity.this).setConsentStatus(ConsentStatus.NON_PERSONALIZED);
                }
            }
        });

        tv_rateapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appName = getPackageName();//your application package name i.e play store application url
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id="
                                    + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + appName)));
                }
            }
        });

        tv_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPrivacyDialog();
            }
        });

        tv_moreapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
            }
        });

        ll_consent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adConsent.requestConsent();
            }
        });

        ll_clearcache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AsyncTask<String, String, String>() {
                    @Override
                    protected void onPreExecute() {
                        progressDialog.show();
                        super.onPreExecute();
                    }

                    @Override
                    protected String doInBackground(String... strings) {
                        FileUtils.deleteQuietly(getCacheDir());
                        FileUtils.deleteQuietly(getExternalCacheDir());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingActivity.this, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show();
                        tv_cachesize.setText("0 MB");
                        super.onPostExecute(s);
                    }
                }.execute();
            }
        });

        changeThemeColor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void setConsentSwitch() {
        if (ConsentInformation.getInstance(this).getConsentStatus() == ConsentStatus.PERSONALIZED) {
            switch_consent.setChecked(true);
        } else {
            switch_consent.setChecked(false);
        }
    }

    public void openPrivacyDialog() {
        Dialog dialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new Dialog(SettingActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            dialog = new Dialog(SettingActivity.this);
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_privacy);

        WebView webview = dialog.findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        String mimeType = "text/html;charset=UTF-8";
        String encoding = "utf-8";

        if (Constant.itemAbout != null) {
            String text = "<html><head>"
                    + "<style> body{color: #000 !important;text-align:left}"
                    + "</style></head>"
                    + "<body>"
                    + Constant.itemAbout.getPrivacy()
                    + "</body></html>";

            webview.loadData(text, mimeType, encoding);
        }

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void changeThemeColor() {

        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked},
        };

        int[] thumbColors = new int[]{
                ContextCompat.getColor(SettingActivity.this, R.color.switch_thumb_disable),
                ContextCompat.getColor(SettingActivity.this, R.color.colorPrimary),
        };

        int[] trackColors = new int[]{
                ContextCompat.getColor(SettingActivity.this, R.color.switch_track),
                ContextCompat.getColor(SettingActivity.this, R.color.switch_track),
        };
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_noti.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_noti.getTrackDrawable()), new ColorStateList(states, trackColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_consent.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switch_consent.getTrackDrawable()), new ColorStateList(states, trackColors));
    }

    private void initializeCache() {
        long size = 0;
        size += getDirSize(this.getCacheDir());
        size += getDirSize(this.getExternalCacheDir());
        tv_cachesize.setText(readableFileSize(size));
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0 Bytes";
        final String[] units = new String[]{"Bytes", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}