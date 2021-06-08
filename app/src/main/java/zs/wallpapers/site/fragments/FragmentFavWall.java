package zs.wallpapers.site.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.RadioGroup;
import android.widget.TextView;

import zs.wallpapers.site.R;
import zs.wallpapers.site.adapter.AdapterWallpaperFav;

import zs.wallpapers.site.hdwallpaper.WallPaperDetailsActivity;
import zs.wallpapers.site.interfaces.InterAdListener;
import zs.wallpapers.site.interfaces.RecyclerViewClickListener;
import zs.wallpapers.site.items.ItemWallpaper;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class FragmentFavWall extends Fragment {

    DBHelper dbHelper;
    Methods methods;
    RecyclerView recyclerView;
    AdapterWallpaperFav adapter;
    ArrayList<ItemWallpaper> arrayList;
    ProgressBar progressBar;
    TextView textView_empty;
    int pos;
    GridLayoutManager grid;
    InterAdListener interAdListener;
    SearchView searchView;
    String wallType;
    FloatingActionButton fab;

    public static FragmentFavWall newInstance(int position) {
        FragmentFavWall fragment = new FragmentFavWall();
        Bundle args = new Bundle();
        args.putInt("pos", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallpaper, container, false);

        pos = getArguments().getInt("pos");
        wallType = "";

        interAdListener = new InterAdListener() {
            @Override
            public void onClick(int pos, String type) {
                int position = getPosition(adapter.getID(pos));
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

        progressBar = rootView.findViewById(R.id.pb_wall);
        progressBar.setVisibility(View.GONE);
        textView_empty = rootView.findViewById(R.id.tv_empty_wall);

        fab = rootView.findViewById(R.id.fab);
        recyclerView = rootView.findViewById(R.id.rv_wall);
        recyclerView.setHasFixedSize(true);
        grid = new GridLayoutManager(getActivity(), 3);

        recyclerView.setLayoutManager(grid);
        getFavData();

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
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);

        super.onCreateOptionsMenu(menu, inflater);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (!searchView.isIconified() && adapter != null) {
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }
            return true;
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

    private void openFilterDialog() {
        final CharSequence[] type = {getString(R.string.all), getString(R.string.portrait), getString(R.string.landscape), getString(R.string.square)};

        final Dialog dialog_rate = new Dialog(getActivity());
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

                getFavData();
                dialog_rate.dismiss();
            }
        });

        dialog_rate.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_rate.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog_rate.show();
        Window window = dialog_rate.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
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

    private void getFavData() {
        arrayList.addAll(dbHelper.getWallpapers("fav", "", wallType));
        if (adapter == null) {
            adapter = new AdapterWallpaperFav(getActivity(), wallType, arrayList, new RecyclerViewClickListener() {
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

    private int getPosition(String id) {
        int count = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            if (id.equals(arrayList.get(i).getId())) {
                count = i;
                break;
            }
        }
        return count;
    }
}