package zs.wallpapers.site.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import zs.wallpapers.site.R;
import zs.wallpapers.site.adapter.AdapterCategories;
import zs.wallpapers.site.asyncTask.LoadCat;
import zs.wallpapers.site.hdwallpaper.MainActivity;

import zs.wallpapers.site.interfaces.CategoryListener;
import zs.wallpapers.site.interfaces.InterAdListener;
import zs.wallpapers.site.items.ItemCat;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;
import zs.wallpapers.site.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class FragmentCategories extends Fragment {

    DBHelper dbHelper;
    Methods methods;
    RecyclerView recyclerView;
    AdapterCategories adapterCategories;
    ArrayList<ItemCat> arrayList;
    CircularProgressBar progressBar;
    TextView textView_empty;
    LoadCat loadCat;
    InterAdListener interAdListener;
    SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        interAdListener = new InterAdListener() {
            @Override
            public void onClick(int pos, String type) {
                int position = getPosition(adapterCategories.getID(pos));

                FragmentWallByCat frag = new FragmentWallByCat();
                Bundle bundle = new Bundle();
                bundle.putString("cid", arrayList.get(position).getId());
                bundle.putString("cname", arrayList.get(position).getName());
                bundle.putString("from", "");
                frag.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.frame_layout, frag, arrayList.get(position).getName());
                ft.addToBackStack(arrayList.get(position).getName());
                ft.commit();
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(arrayList.get(position).getName());
            }
        };

        dbHelper = new DBHelper(getActivity());
        methods = new Methods(getActivity(), interAdListener);

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb_cat);
        textView_empty = rootView.findViewById(R.id.tv_empty_cat);
        recyclerView = rootView.findViewById(R.id.rv_cat);
        GridLayoutManager grid = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(grid);

        if (methods.isNetworkAvailable()) {
            loadCat = new LoadCat(getActivity(), new CategoryListener() {
                @Override
                public void onStart() {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemCat> arrayListCat) {
                    if (getActivity() != null) {
                        arrayList.addAll(arrayListCat);
                        progressBar.setVisibility(View.INVISIBLE);

                        setAdapter();
                    }
                }
            });
            loadCat.execute(Constant.URL_CATEGORY);
        } else {
            arrayList = dbHelper.getCat();
            if (arrayList != null) {
                setAdapter();
            }
            progressBar.setVisibility(View.GONE);
        }

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInter(position, "");
            }
        }));

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);

        super.onCreateOptionsMenu(menu, inflater);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {

            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (!searchView.isIconified() && adapterCategories != null) {
                adapterCategories.getFilter().filter(s);
                adapterCategories.notifyDataSetChanged();
            }
            return false;
        }
    };

    public void setAdapter() {
        adapterCategories = new AdapterCategories(getActivity(), arrayList);
        AnimationAdapter adapterAnim = new ScaleInAnimationAdapter(adapterCategories);
        adapterAnim.setFirstOnly(true);
        adapterAnim.setDuration(500);
        adapterAnim.setInterpolator(new OvershootInterpolator(.9f));
        recyclerView.setAdapter(adapterAnim);
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