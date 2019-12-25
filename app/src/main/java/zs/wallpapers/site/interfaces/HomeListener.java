package zs.wallpapers.site.interfaces;

import zs.wallpapers.site.items.ItemCat;
import zs.wallpapers.site.items.ItemWallpaper;

import java.util.ArrayList;

public interface HomeListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemWallpaper> arrayListFeatured, ArrayList<ItemWallpaper> arrayListPotrait, ArrayList<ItemWallpaper> arrayListLandscape, ArrayList<ItemWallpaper> arrayListSquare, ArrayList<ItemCat> arrayListCat);
}