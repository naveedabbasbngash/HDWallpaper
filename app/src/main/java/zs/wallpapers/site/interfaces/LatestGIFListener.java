package zs.wallpapers.site.interfaces;

import zs.wallpapers.site.items.ItemGIF;

import java.util.ArrayList;

public interface LatestGIFListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemGIF> arrayListCat);
}