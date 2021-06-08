package zs.wallpapers.site.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.TypedValue;
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
import android.widget.RadioButton;
import android.widget.RelativeLayout;
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

public class FragmentLatest extends Fragment {

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
    int pos, page = 1;
    Boolean isOver = false, isScroll = false;
    String wallType;
    FloatingActionButton fab;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallpaper, container, false);

        wallType = "";
        if (getArguments() != null) {
            pos = getArguments().getInt("pos");
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
        recyclerView.setLayoutManager(grid);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
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

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);


        int margin = 0;

        try {
            if (FragmentDashboard.bottomNavigationMenu != null) {
                margin = FragmentDashboard.bottomNavigationMenu.getHeight();
            } else {
                float dip = 60f;
                Resources r = getResources();
                margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
            }
        } catch (Exception e) {
            float dip = 60f;
            Resources r = getResources();
            margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
            e.printStackTrace();
        }
        params.bottomMargin = margin + 10;
        params.rightMargin = 40;
        params.leftMargin = 40;

        fab.setLayoutParams(params);

        getLatestData();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                openFilterDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getLatestData() {
        if (methods.isNetworkAvailable()) {
            loadWallpaper = new LoadWallpaper(new LatestWallListener() {
                @Override
                public void onStart() {
                    if (arrayList.size() == 0) {
                        if (pos == 0) {
                            dbHelper.removeAllWallpaper("latest");
                        }
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
                            for (int i = 0; i < arrayListWall.size(); i++) {
                                dbHelper.addWallpaper(arrayListWall.get(i), "latest");
                            }
                            page = page + 1;
                            arrayList.addAll(arrayListWall);
                            setAdapter();
                        }
                    }
                }
            });
            if (pos == 0) {
                loadWallpaper.execute(Constant.URL_LATEST + page + "&type=" + wallType);
            } else if (pos == 1) {
                loadWallpaper.execute(Constant.URL_MOST_VIEWED + page + "&type=" + wallType);
            } else if (pos == 2) {
                loadWallpaper.execute(Constant.URL_MOST_RATED + page + "&type=" + wallType);
            }
        } else {
            if (pos == 0) {
                arrayList = dbHelper.getWallpapers("latest", "", wallType);
            } else if (pos == 1) {
                arrayList = dbHelper.getWallpapers("latest", "views", wallType);
            } else if (pos == 2) {
                arrayList = dbHelper.getWallpapers("latest", "rate", wallType);
            }
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
                    adapter.notifyDataSetChanged();
                    adapter.setType(wallType);
                }

                if (wallType.equals(getString(R.string.landscape))) {
                    grid.setSpanCount(2);
                } else {
                    grid.setSpanCount(3);
                }

                page = 1;
                isScroll = false;
                isOver = false;

                getLatestData();
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