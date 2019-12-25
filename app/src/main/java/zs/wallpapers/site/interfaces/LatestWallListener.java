package zs.wallpapers.site.interfaces;

import zs.wallpapers.site.items.ItemWallpaper;

import java.util.ArrayList;

public interface LatestWallListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemWallpaper> arrayListCat);
}
