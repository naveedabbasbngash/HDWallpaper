package zs.wallpapers.site.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import zs.wallpapers.site.R;
import zs.wallpapers.site.adapter.AdapterWallpaper;
import zs.wallpapers.site.asyncTask.LoadWallpaper;

import zs.wallpapers.site.hdwallpaper.SearchWallActivity;
import zs.wallpapers.site.hdwallpaper.WallPaperDetailsActivity;
import zs.wallpapers.site.interfaces.InterAdListener;
import zs.wallpapers.site.interfaces.LatestWallListener;
import zs.wallpapers.site.interfaces.RecyclerViewClickListener;
import zs.wallpapers.site.items.ItemWallpaper;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.EndlessRecyclerViewScrollListener;
import zs.wallpapers.site.utils.Methods;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class FragmentWallByType extends Fragment {

    DBHelper dbHelper;
    Methods methods;
    RecyclerView recyclerView;
    AdapterWallpaper adapter;
    ArrayList<ItemWallpaper> arrayList;
    CircularProgressBar progressBar;
    TextView textView_empty;
    LoadWallpaper loadWallpaper;
    GridLayoutManager grid;
    InterAdListener interAdListener;
    AnimationAdapter adapterAnim;
    int page = 1;
    Boolean isOver = false, isScroll = false;
    String wallType;
    FloatingActionButton fab;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallpaper, container, false);

        if (getArguments() != null) {
            wallType = getArguments().getString("type");
        }

        interAdListener = new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(getActivity(), WallPaperDetailsActivity.class);
                intent.putExtra("pos", position);
                Constant.arrayList.clear();
                Constant.arrayList.addAll(arrayList);
                startActivity(intent);
            }
        };

        dbHelper = new DBHelper(getActivity());
        methods = new Methods(getActivity(), interAdListener);
        arrayList = new ArrayList<>();

        fab = rootView.findViewById(R.id.fab);
        progressBar = rootView.findViewById(R.id.pb_wall);
        textView_empty = rootView.findViewById(R.id.tv_empty_wall);

        recyclerView = rootView.findViewById(R.id.rv_wall);
        recyclerView.setHasFixedSize(true);
        grid = new GridLayoutManager(getActivity(), 3);
        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? grid.getSpanCount() : 1;
            }
        });

        if (wallType.equals(getString(R.string.landscape))) {
            grid.setSpanCount(2);
        } else {
            grid.setSpanCount(3);
        }
        recyclerView.setLayoutManager(grid);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    fab.hide();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getLatestData();
                        }
                    }, 0);
                } else {
                    adapter.hideHeader();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        getLatestData();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
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

    private void getLatestData() {
        if (methods.isNetworkAvailable()) {
            loadWallpaper = new LoadWallpaper(new LatestWallListener() {
                @Override
                public void onStart() {
                    if (arrayList.size() == 0) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success, ArrayList<ItemWallpaper> arrayListWall) {
                    if (getActivity() != null) {
                        if (arrayListWall.size() == 0) {
                            isOver = true;
                            try {
                                adapter.hideHeader();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            setEmptyTextView();
                        } else {
                            page = page + 1;
                            arrayList.addAll(arrayListWall);
                            setAdapter();
                        }
                    }
                }
            });
            if (wallType.equals(getString(R.string.portrait))) {
                loadWallpaper.execute(Constant.URL_WALL_PORTRAIT + page);
            } else if (wallType.equals(getString(R.string.landscape))) {
                loadWallpaper.execute(Constant.URL_WALL_LANDSCAPE + page);
            } else if (wallType.equals(getString(R.string.square))) {
                loadWallpaper.execute(Constant.URL_WALL_SQUARE + page);
            }
        } else {
            setAdapter();
            isOver = true;
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isScroll) {
                    adapter = new AdapterWallpaper(getActivity(), wallType, arrayList, new RecyclerViewClickListener() {
                        @Override
                        public void onClick(int position) {
                            methods.showInter(position, "");
                        }
                    });

                    adapterAnim = new ScaleInAnimationAdapter(adapter);
                    adapterAnim.setFirstOnly(true);
                    adapterAnim.setDuration(500);
                    adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
                    recyclerView.setAdapter(adapterAnim);
                    setEmptyTextView();
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        }, 0);
    }

    private void setEmptyTextView() {
        progressBar.setVisibility(View.INVISIBLE);
        if (arrayList.size() == 0) {
            textView_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textView_empty.setVisibility(View.GONE);
        }
    }
}