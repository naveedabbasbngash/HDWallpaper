package zs.wallpapers.site.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
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

import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class FragmentWallByCat extends Fragment {

    DBHelper dbHelper;
    RecyclerView recyclerView;
    AdapterWallpaper adapter;
    ArrayList<ItemWallpaper> arrayList;
    ProgressBar progressBar;
    Methods methods;
    InterAdListener interAdListener;
    Boolean isOver = false, isScroll = false;
    TextView textView_empty;
    LoadWallpaper loadWallpaper;
    int page = 1;
    GridLayoutManager grid;
    String cid, cname, from = "";
    String wallType;
    FloatingActionButton fab;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wall_by_cat, container, false);

        wallType = "";

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

        dbHelper.getAbout();

        cid = getArguments().getString("cid");
        cname = getArguments().getString("cname");
        from = getArguments().getString("from");

        arrayList = new ArrayList<>();
        progressBar = rootView.findViewById(R.id.pb_wallcat);
        textView_empty = rootView.findViewById(R.id.tv_empty_wallcat);

        fab = rootView.findViewById(R.id.fab);
        recyclerView = rootView.findViewById(R.id.rv_wall_by_cat);
        recyclerView.setHasFixedSize(true);
        grid = new GridLayoutManager(getActivity(), 3);
        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? grid.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(grid);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScroll = true;
                            getWallpaperData();
                        }
                    }, 0);
                } else {
                    adapter.hideHeader();
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = grid.findFirstVisibleItemPosition();

                if (firstVisibleItem > 6) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        getWallpaperData();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        menu.findItem(R.id.menu_filter).setVisible(true);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_filter:
                openFilterDialog();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void getWallpaperData() {
        if (methods.isNetworkAvailable()) {
            loadWallpaper = new LoadWallpaper(new LatestWallListener() {
                @Override
                public void onStart() {
                    if (arrayList.size() == 0) {
                        dbHelper.removeWallByCat("catlist", cid);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success, ArrayList<ItemWallpaper> arrayListWall) {
                    if (arrayListWall.size() == 0) {
                        isOver = true;
                        try {
                            adapter.hideHeader();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setEmptTextView();
                    } else {
                        for (int i = 0; i < arrayListWall.size(); i++) {
                            dbHelper.addWallpaper(arrayListWall.get(i), "catlist");
                        }
                        page = page + 1;
                        arrayList.addAll(arrayListWall);
                        progressBar.setVisibility(View.INVISIBLE);
                        setAdapter();
                    }
                }
            });
            loadWallpaper.execute(Constant.URL_WALLPAPER_BY_CAT + cid + "&page=" + page + "&type=" + wallType);
        } else {
            arrayList = dbHelper.getWallByCat("catlist", cid, wallType);
            setAdapter();
            isOver = true;
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter() {
        if (!isScroll) {
            adapter = new AdapterWallpaper(getActivity(), wallType, arrayList, new RecyclerViewClickListener() {
                @Override
                public void onClick(int position) {
                    methods.showInter(position, "");
                }
            });
            AnimationAdapter adapterAnim = new ScaleInAnimationAdapter(adapter);
            adapterAnim.setFirstOnly(true);
            adapterAnim.setDuration(500);
            adapterAnim.setInterpolator(new OvershootInterpolator(.5f));
            recyclerView.setAdapter(adapterAnim);
            setEmptTextView();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void setEmptTextView() {
        progressBar.setVisibility(View.INVISIBLE);
        if (arrayList.size() == 0) {
            textView_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textView_empty.setVisibility(View.GONE);
        }
    }

    private void openFilterDialog() {
        final CharSequence[] type = {getString(R.string.all), getString(R.string.portrait), getString(R.string.landscape), getString(R.string.square)};

        final Dialog dialog_rate = new Dialog(getActivity());
        dialog_rate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_rate.setContentView(R.layout.layout_wall_chooser);

        ImageView iv_close = dialog_rate.findViewById(R.id.iv_rate_close);
        final RadioButton rb_all = dialog_rate.findViewById(R.id.rb_all);
        final RadioButton rb_portrait = dialog_rate.findViewById(R.id.rb_portrait);
        final RadioButton rb_landscape = dialog_rate.findViewById(R.id.rb_landscape);
        final RadioButton rb_square = dialog_rate.findViewById(R.id.rb_square);

        AppCompatButton button_select = dialog_rate.findViewById(R.id.button_select_type);

        if (methods.getWallTypePos(wallType) == 0) {
            rb_all.setChecked(true);
        } else if (methods.getWallTypePos(wallType) == 1) {
            rb_portrait.setChecked(true);
        } else if (methods.getWallTypePos(wallType) == 2) {
            rb_landscape.setChecked(true);
        } else if (methods.getWallTypePos(wallType) == 3) {
            rb_square.setChecked(true);
        }

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_rate.dismiss();
            }
        });

        button_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rb_all.isChecked()) {
                    wallType = "";
                } else if (rb_portrait.isChecked()) {
                    wallType = type[1].toString();
                } else if (rb_landscape.isChecked()) {
                    wallType = type[2].toString();
                } else if (rb_square.isChecked()) {
                    wallType = type[3].toString();
                }

                arrayList.clear();
                if (adapter != null) {
                    adapter.setType(wallType);
                    adapter.notifyDataSetChanged();
                }

                if (wallType.equals(getString(R.string.landscape))) {
                    grid.setSpanCount(2);
                } else {
                    grid.setSpanCount(3);
                }

                page = 1;
                isScroll = false;
                isOver = false;

                getWallpaperData();
                dialog_rate.dismiss();
            }
        });

        dialog_rate.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_rate.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog_rate.show();
        Window window = dialog_rate.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }
}