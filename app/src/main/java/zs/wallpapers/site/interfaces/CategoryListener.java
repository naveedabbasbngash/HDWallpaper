package zs.wallpapers.site.interfaces;

import zs.wallpapers.site.items.ItemCat;

import java.util.ArrayList;

public interface CategoryListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemCat> arrayListCat);
}
