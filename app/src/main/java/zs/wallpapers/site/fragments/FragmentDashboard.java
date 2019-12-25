package zs.wallpapers.site.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import zs.wallpapers.site.R;
import zs.wallpapers.site.hdwallpaper.MainActivity;

import zs.wallpapers.site.utils.Methods;

import java.lang.reflect.Field;

public class FragmentDashboard extends Fragment {

    Methods methods;
    public static BottomNavigationView bottomNavigationMenu;
    FragmentManager fm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        methods = new Methods(getActivity());
        fm = getFragmentManager();

        bottomNavigationMenu = rootView.findViewById(R.id.navigation_bottom);
        bottomNavigationMenu.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView)
                bottomNavigationMenu.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass()
                    .getDeclaredField("mShiftingMode");

            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);

            for (int i = 0; i < menuView.getChildCount(); i++) {

                BottomNavigationItemView item =
                        (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                //To update view, set the checked value again
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();

        } catch (IllegalAccessException e) {
            e.printStackTrace();

        } catch (SecurityException e) {
            e.printStackTrace();
        }

        FragmentHome f1 = new FragmentHome();
        loadFrag(f1, getString(R.string.home));

        return rootView;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_bottom_home:
                    FragmentHome f1 = new FragmentHome();
                    loadFrag(f1, getString(R.string.home));
                    return true;
                case R.id.nav_bottom_latest:
                    FragmentLatest flatest = new FragmentLatest();
                    Bundle args = new Bundle();
                    args.putInt("pos", 0);
                    flatest.setArguments(args);
                    loadFrag(flatest, getString(R.string.latest));
                    return true;
                case R.id.nav_bottom_popular:
                    FragmentLatest fpop = new FragmentLatest();
                    Bundle argsPop = new Bundle();
                    argsPop.putInt("pos", 1);
                    fpop.setArguments(argsPop);
                    loadFrag(fpop, getString(R.string.popular));
                    return true;
                case R.id.nav_bottom_rated:
                    FragmentLatest frate = new FragmentLatest();
                    Bundle argsrate = new Bundle();
                    argsrate.putInt("pos", 2);
                    frate.setArguments(argsrate);
                    loadFrag(frate, getString(R.string.rated));
                    return true;
                case R.id.nav_bottom_cat:
                    FragmentCategories fcat = new FragmentCategories();
                    loadFrag(fcat, getString(R.string.categories));
                    return true;
            }
            return false;
        }
    };

    public void loadFrag(Fragment f1, String name) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (name.equals(getString(R.string.search))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment_dash, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment_dash, f1, name);
        }
        ft.commit();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(name);
    }
}
