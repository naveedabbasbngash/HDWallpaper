package zs.wallpapers.site.hdwallpaper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import zs.wallpapers.site.R;
import zs.wallpapers.site.adapter.AdapterWallpaperFav;
import zs.wallpapers.site.asyncTask.LoadWallpaper;



import zs.wallpapers.site.interfaces.InterAdListener;
import zs.wallpapers.site.interfaces.LatestWallListener;
import zs.wallpapers.site.interfaces.RecyclerViewClickListener;
import zs.wallpapers.site.items.ItemWallpaper;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.Methods;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchWallActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    AdapterWallpaperFav adapter;
    ArrayList<ItemWallpaper> arrayList;
    ProgressBar progressBar;
    Methods methods;
    InterAdListener interAdListener;
    TextView textView_empty;
    LoadWallpaper loadWallpaper;
    GridLayoutManager grid;
    String wallType;
    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Constant.search_item = s;
            getWallpaperData();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_by_cat);

        wallType = "";
        interAdListener = new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(SearchWallActivity.this, WallPaperDetailsActivity.class);
                intent.putExtra("pos", position);
                Constant.arrayList.clear();
                Constant.arrayList.addAll(arrayList);
                startActivity(intent);
            }
        };

        methods = new Methods(this, interAdListener);
        methods.setStatusColor(getWindow());
        methods.forceRTLIfSupported(getWindow());

        toolbar = this.findViewById(R.id.toolbar_wall_by_cat);
        toolbar.setTitle(Constant.search_item);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout ll_ad = findViewById(R.id.ll_ad_search);
        methods.showBannerAd(ll_ad);

        arrayList = new ArrayList<>();
        progressBar = findViewById(R.id.pb_wallcat);
        textView_empty = findViewById(R.id.tv_empty_wallcat);

        recyclerView = findViewById(R.id.rv_wall_by_cat);
        recyclerView.setHasFixedSize(true);
        grid = new GridLayoutManager(SearchWallActivity.this, 3);
        recyclerView.setLayoutManager(grid);

        getWallpaperData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menu.findItem(R.id.menu_filter).setVisible(true);
        MenuItem item = menu.findItem(R.id.menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
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
                    arrayList.clear();
                    recyclerView.setVisibility(View.GONE);
                    textView_empty.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemWallpaper> arrayListWall) {
                    arrayList.addAll(arrayListWall);
                    progressBar.setVisibility(View.INVISIBLE);
                    setAdapter();
                }
            });
            loadWallpaper.execute(Constant.URL_SEARCH_WALL + Constant.search_item.replace(" ", "%20") + "&type=" + wallType);
        } else {
            setAdapter();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setAdapter() {
        if (adapter == null) {
            adapter = new AdapterWallpaperFav(SearchWallActivity.this, wallType, arrayList, new RecyclerViewClickListener() {
                @Override
                public void onClick(int position) {
                    methods.showInter(position, "");
                }
            });
            AnimationAdapter adapterAnim = new ScaleInAnimationAdapter(adapter);
            adapterAnim.setFirstOnly(true);
            adapterAnim.setDuration(500);
            adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
            recyclerView.setAdapter(adapterAnim);
        } else {
            adapter.notifyDataSetChanged();
        }
        setExmptTextView();
    }

    private void setExmptTextView() {
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

        final Dialog dialog_rate = new Dialog(SearchWallActivity.this);
        dialog_rate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_rate.setContentView(R.layout.layout_wall_chooser);

        RadioGroup radioGroup = dialog_rate.findViewById(R.id.rg_wall_type);
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
