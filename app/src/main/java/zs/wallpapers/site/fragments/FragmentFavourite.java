package zs.wallpapers.site.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import zs.wallpapers.site.R;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.Methods;

public class FragmentFavourite extends Fragment {

    Methods methods;
    TextView tv_wall, tv_gif;
    LinearLayout ll_fav;
    int tab_no = 2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favourite, container, false);

        methods = new Methods(getActivity());

        ll_fav = rootView.findViewById(R.id.ll_fav);
        if (!Constant.isGIFEnabled) {
            tab_no = 1;
            ll_fav.setVisibility(View.GONE);
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        final ViewPager mViewPager = rootView.findViewById(R.id.container_fav);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        tv_wall = rootView.findViewById(R.id.tv_tab_wall);
        tv_gif = rootView.findViewById(R.id.tv_tabs_gif);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTabsBG(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tv_wall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTabsBG(0);
                mViewPager.setCurrentItem(0);
            }
        });

        tv_gif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTabsBG(1);
                mViewPager.setCurrentItem(1);
            }
        });

        return rootView;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentFavWall.newInstance(position);
                case 1:
                    return FragmentFavGIFs.newInstance(position);
                default:
                    return FragmentFavWall.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            return tab_no;
        }
    }

    private void setTabsBG(int tabs) {
        if (tabs == 0) {
            tv_wall.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            tv_gif.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

            tv_wall.setBackgroundResource(R.drawable.bg_tab_fav_selected);
            tv_gif.setBackgroundResource(R.drawable.bg_tab_fav_unselected);
        } else {
            tv_gif.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            tv_wall.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

            tv_gif.setBackgroundResource(R.drawable.bg_tab_fav_selected);
            tv_wall.setBackgroundResource(R.drawable.bg_tab_fav_unselected);
        }
    }
}