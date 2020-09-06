package zs.wallpapers.site.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.banner.BannerView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import zs.wallpapers.site.FragmentDashboard;
import zs.wallpapers.site.R;
import zs.wallpapers.site.adapter.AdapterCategoriesHome;
import zs.wallpapers.site.adapter.AdapterImageHome;
import zs.wallpapers.site.asyncTask.LoadHome;
import zs.wallpapers.site.hdwallpaper.MainActivity;

import zs.wallpapers.site.hdwallpaper.SearchWallActivity;
import zs.wallpapers.site.hdwallpaper.WallPaperDetailsActivity;
import zs.wallpapers.site.interfaces.HomeListener;
import zs.wallpapers.site.interfaces.InterAdListener;
import zs.wallpapers.site.interfaces.RecyclerViewClickListener;
import zs.wallpapers.site.items.ItemCat;
import zs.wallpapers.site.items.ItemWallpaper;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;
import zs.wallpapers.site.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;

public class FragmentHome extends Fragment {

    DBHelper dbHelper;
    Methods methods;
    RecyclerView rv_portrait, rv_landscape, rv_square, rv_cat;
    AdapterCategoriesHome adapterCategories;
    AdapterImageHome adapter_portrait, adapter_landscape, adapter_square;
    ArrayList<ItemWallpaper> arrayList_featured, arrayList_portrait, arrayList_landscape, arrayList_square;
    ArrayList<ItemCat> arrayList_cat;
    Button button_portrait, button_landscape, button_square, button_cat;
    TextView tv_empty_cat, tv_empty_portrait, tv_empty_landscape, tv_empty_square;
    LoadHome loadHome;
    LinearLayout linearLayout;
    CircularProgressBar progressBar;

    HorizontalInfiniteCycleViewPager infiniteCycleViewPager;
    ImagePagerAdapter pagerAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        BannerView bannerView = rootView.findViewById(R.id.hw_banner_view);
        bannerView.setAdId("u4jqa6ms9f");
        bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_57);
        // Create an ad request to load an ad.
        AdParam adParam = new AdParam.Builder().build();
        bannerView.loadAd(adParam);
        dbHelper = new DBHelper(getActivity());
        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                switch (type) {
                    case "featured":
                        Intent intent_feat = new Intent(getActivity(), WallPaperDetailsActivity.class);
                        Constant.arrayList.clear();
                        Constant.arrayList.addAll(arrayList_featured);
                        intent_feat.putExtra("pos", position);
                        startActivity(intent_feat);
                        break;
                    case "latest":
                        Intent intent_lat = new Intent(getActivity(), WallPaperDetailsActivity.class);
                        Constant.arrayList.clear();
                        Constant.arrayList.addAll(arrayList_portrait);
                        intent_lat.putExtra("pos", position);
                        startActivity(intent_lat);
                        break;
                    case "viewed":
                        Intent intent_view = new Intent(getActivity(), WallPaperDetailsActivity.class);
                        Constant.arrayList.clear();
                        Constant.arrayList.addAll(arrayList_landscape);
                        intent_view.putExtra("pos", position);
                        startActivity(intent_view);
                        break;
                    case "rated":
                        Intent intent = new Intent(getActivity(), WallPaperDetailsActivity.class);
                        Constant.arrayList.clear();
                        Constant.arrayList.addAll(arrayList_square);
                        intent.putExtra("pos", position);
                        startActivity(intent);
                        break;
                    case "cat":
                        FragmentWallByCat frag = new FragmentWallByCat();
                        Bundle bundle = new Bundle();
                        bundle.putString("cid", arrayList_cat.get(position).getId());
                        bundle.putString("cname", arrayList_cat.get(position).getName());
                        bundle.putString("from", "");
                        frag.setArguments(bundle);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                        ft.add(R.id.frame_layout, frag, arrayList_cat.get(position).getName());
                        ft.addToBackStack(arrayList_cat.get(position).getName());
                        ft.commit();
                        ((MainActivity) getActivity()).getSupportActionBar().setTitle(arrayList_cat.get(position).getName());
                        break;
                }
            }
        });
        arrayList_featured = new ArrayList<>();
        arrayList_portrait = new ArrayList<>();
        arrayList_landscape = new ArrayList<>();
        arrayList_square = new ArrayList<>();
        arrayList_cat = new ArrayList<>();

        button_cat = rootView.findViewById(R.id.button_cat_all);
        button_portrait = rootView.findViewById(R.id.button_portrait_all);
        button_landscape = rootView.findViewById(R.id.button_landscape_all);
        button_square = rootView.findViewById(R.id.button_square_all);
        tv_empty_cat = rootView.findViewById(R.id.tv_empty_home_cat);
        tv_empty_portrait = rootView.findViewById(R.id.tv_empty_home_portrait);
        tv_empty_landscape = rootView.findViewById(R.id.tv_empty_home_landscape);
        tv_empty_square = rootView.findViewById(R.id.tv_empty_home_square);

        rv_portrait = rootView.findViewById(R.id.rv_home_portrait);
        rv_portrait.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_portrait.setLayoutManager(llm);

        rv_landscape = rootView.findViewById(R.id.rv_home_landscape);
        rv_landscape.setHasFixedSize(true);
        LinearLayoutManager llm2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_landscape.setLayoutManager(llm2);

        rv_square = rootView.findViewById(R.id.rv_home_square);
        rv_square.setHasFixedSize(true);
        LinearLayoutManager llm3 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_square.setLayoutManager(llm3);

        rv_cat = rootView.findViewById(R.id.rv_home_cat);
        rv_cat.setHasFixedSize(true);
        LinearLayoutManager llm_cat = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_cat.setLayoutManager(llm_cat);

        linearLayout = rootView.findViewById(R.id.ll_main_home);
        progressBar = rootView.findViewById(R.id.pb_home);

        Constant.isFav = false;

        button_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentWallByType fragment = new FragmentWallByType();
                Bundle bundle = new Bundle();
                bundle.putString("type", getString(R.string.portrait));
                fragment.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.frame_layout, fragment, getString(R.string.portrait));
                ft.addToBackStack(getString(R.string.portrait));
                ft.commit();
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.portrait));
            }
        });

        button_landscape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentWallByType fragment = new FragmentWallByType();
                Bundle bundle = new Bundle();
                bundle.putString("type", getString(R.string.landscape));
                fragment.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.frame_layout, fragment, getString(R.string.landscape));
                ft.addToBackStack(getString(R.string.landscape));
                ft.commit();
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.landscape));
            }
        });

        button_square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentWallByType fragment = new FragmentWallByType();
                Bundle bundle = new Bundle();
                bundle.putString("type", getString(R.string.square));
                fragment.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.frame_layout, fragment, getString(R.string.square));
                ft.addToBackStack(getString(R.string.square));
                ft.commit();
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.square));
            }
        });

        button_cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (FragmentDashboard.bottomNavigationMenu != null) {
                        FragmentDashboard.bottomNavigationMenu.setSelectedItemId(R.id.nav_bottom_cat);
                        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.categories));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        rv_cat.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInter(position, "cat");
            }
        }));

        infiniteCycleViewPager = rootView.findViewById(R.id.infinitViewPager);
        infiniteCycleViewPager.setInterpolator(new OvershootInterpolator());

        int height = (int) (methods.getScreenHeight() * 0.6);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        params.setMargins(50, 0, 50, 0);
        infiniteCycleViewPager.setLayoutParams(params);


        getWallpapers();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);

        super.onCreateOptionsMenu(menu, inflater);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Constant.search_item = s;
            Intent intent = new Intent(getActivity(), SearchWallActivity.class);
            startActivity(intent);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void getWallpapers() {
        if (methods.isNetworkAvailable()) {
            loadHome = new LoadHome(getActivity(), new HomeListener() {
                @Override
                public void onStart() {
                    linearLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemWallpaper> arrayListFeatured, ArrayList<ItemWallpaper> arrayListPotrait, ArrayList<ItemWallpaper> arrayListLandscape, ArrayList<ItemWallpaper> arrayListSquare, ArrayList<ItemCat> arrayListCat) {
                    if (getActivity() != null) {
                        arrayList_featured.addAll(arrayListFeatured);
                        arrayList_portrait.addAll(arrayListPotrait);
                        arrayList_landscape.addAll(arrayListLandscape);
                        arrayList_square.addAll(arrayListSquare);
                        arrayList_cat.addAll(arrayListCat);
                        setAdapterToListview();
                    }
                }
            });
            loadHome.execute(Constant.URL_HOME);
        } else {
            arrayList_featured = dbHelper.getWallpapers("latest", "", "");
            arrayList_portrait = dbHelper.getWallpapers("latest", "", getString(R.string.portrait));
            arrayList_landscape = dbHelper.getWallpapers("latest", "views", getString(R.string.landscape));
            arrayList_square = dbHelper.getWallpapers("latest", "rate", getString(R.string.square));
            arrayList_cat = dbHelper.getCat();
            setAdapterToListview();
        }
    }

    public void setAdapterToListview() {
        adapter_portrait = new AdapterImageHome(getActivity(), getString(R.string.portrait), arrayList_portrait, new RecyclerViewClickListener() {
            @Override
            public void onClick(int position) {
                methods.showInter(position, "latest");
            }
        });
        AnimationAdapter adapterAnim_portrait = new SlideInRightAnimationAdapter(adapter_portrait);
        adapterAnim_portrait.setFirstOnly(true);
        adapterAnim_portrait.setDuration(500);
        adapterAnim_portrait.setInterpolator(new OvershootInterpolator(.9f));
        rv_portrait.setAdapter(adapterAnim_portrait);

        adapter_landscape = new AdapterImageHome(getActivity(), getString(R.string.landscape), arrayList_landscape, new RecyclerViewClickListener() {
            @Override
            public void onClick(int position) {
                methods.showInter(position, "viewed");
            }
        });
        AnimationAdapter adapterAnim_land = new SlideInRightAnimationAdapter(adapter_landscape);
        adapterAnim_land.setFirstOnly(true);
        adapterAnim_land.setDuration(500);
        adapterAnim_land.setInterpolator(new OvershootInterpolator(.9f));
        rv_landscape.setAdapter(adapterAnim_land);

        adapter_square = new AdapterImageHome(getActivity(), getString(R.string.square), arrayList_square, new RecyclerViewClickListener() {
            @Override
            public void onClick(int position) {
                methods.showInter(position, "rated");
            }
        });
        AnimationAdapter adapterAnim_square = new SlideInRightAnimationAdapter(adapter_square);
        adapterAnim_square.setFirstOnly(true);
        adapterAnim_square.setDuration(500);
        adapterAnim_square.setInterpolator(new OvershootInterpolator(.9f));
        rv_square.setAdapter(adapterAnim_square);

        adapterCategories = new AdapterCategoriesHome(arrayList_cat);
        AnimationAdapter adapterAnim4 = new SlideInRightAnimationAdapter(adapterCategories);
        adapterAnim4.setFirstOnly(true);
        adapterAnim4.setDuration(500);
        adapterAnim4.setInterpolator(new OvershootInterpolator(.9f));
        rv_cat.setAdapter(adapterAnim4);

        pagerAdapter = new ImagePagerAdapter();
        infiniteCycleViewPager.setAdapter(pagerAdapter);

        setExmptTextView();
    }

    private void setExmptTextView() {
        linearLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        if (arrayList_portrait.size() == 0) {
            tv_empty_portrait.setVisibility(View.VISIBLE);
            rv_portrait.setVisibility(View.GONE);
        } else {
            rv_portrait.setVisibility(View.VISIBLE);
            tv_empty_portrait.setVisibility(View.GONE);
        }

        if (arrayList_landscape.size() == 0) {
            tv_empty_landscape.setVisibility(View.VISIBLE);
            rv_landscape.setVisibility(View.GONE);
        } else {
            rv_landscape.setVisibility(View.VISIBLE);
            tv_empty_landscape.setVisibility(View.GONE);
        }

        if (arrayList_square.size() == 0) {
            tv_empty_square.setVisibility(View.VISIBLE);
            rv_square.setVisibility(View.GONE);
        } else {
            rv_square.setVisibility(View.VISIBLE);
            tv_empty_square.setVisibility(View.GONE);
        }

        if (arrayList_cat.size() == 0) {
            tv_empty_cat.setVisibility(View.VISIBLE);
            rv_cat.setVisibility(View.GONE);
        } else {
            rv_cat.setVisibility(View.VISIBLE);
            tv_empty_cat.setVisibility(View.GONE);
        }
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        private ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return arrayList_featured.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @Override
        public int getItemPosition(final Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {

            View imageLayout = inflater.inflate(R.layout.layout_viewpager_home, container, false);
            RoundedImageView imageView = imageLayout.findViewById(R.id.iv_vp_home);
            TextView textViewCat = imageLayout.findViewById(R.id.tv_pager_cat);
            final RelativeLayout rl = imageLayout.findViewById(R.id.rl);

            LikeButton likeButton = imageLayout.findViewById(R.id.button_pager_fav);
            likeButton.setLiked(dbHelper.isFav(arrayList_featured.get(position).getId()));

            likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    dbHelper.addtoFavorite(arrayList_featured.get(position));
                    methods.showSnackBar(rl, getActivity().getString(R.string.added_to_fav));
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    dbHelper.removeFav(arrayList_featured.get(position).getId());
                    methods.showSnackBar(rl, getActivity().getString(R.string.removed_from_fav));
                }
            });

            textViewCat.setText(arrayList_featured.get(position).getCName());
            Picasso.get()
                    .load(methods.getImageThumbSize(arrayList_featured.get(position).getImageThumb(),getString(R.string.home)))
                    .placeholder(R.drawable.placeholder_wall)
                    .into(imageView);

            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    methods.showInter(infiniteCycleViewPager.getRealItem(), "featured");
                }
            });

            container.addView(imageLayout);
            return imageLayout;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}