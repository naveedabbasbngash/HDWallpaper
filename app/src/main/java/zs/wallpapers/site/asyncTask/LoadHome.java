package zs.wallpapers.site.asyncTask;

import android.content.Context;
import android.os.AsyncTask;

import zs.wallpapers.site.interfaces.HomeListener;
import zs.wallpapers.site.items.ItemCat;
import zs.wallpapers.site.items.ItemWallpaper;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadHome extends AsyncTask<String, String, Boolean> {

    private HomeListener homeListener;
    private DBHelper dbHelper;
    private ArrayList<ItemCat> arrayListCat;
    private ArrayList<ItemWallpaper> arrayListFeatured, arrayListPotrait, arrayListLandscape, arrayListSquare;

    public LoadHome(Context context, HomeListener homeListener) {
        dbHelper = new DBHelper(context);
        this.homeListener = homeListener;
        arrayListFeatured = new ArrayList<>();
        arrayListLandscape = new ArrayList<>();
        arrayListPotrait = new ArrayList<>();
        arrayListSquare = new ArrayList<>();
        arrayListCat = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        dbHelper.removeAllWallpaper("latest");
        homeListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String url = strings[0];
        String json = JSONParser.okhttpGET(url);
        try {
            JSONObject jOb = new JSONObject(json);
            JSONObject jsonObj = jOb.getJSONObject(Constant.TAG_ROOT);

            JSONArray jsonArray_featured = jsonObj.getJSONArray(Constant.TAG_FEATURED_WALL);
            for (int i = 0; i < jsonArray_featured.length(); i++) {
                JSONObject objJson = jsonArray_featured.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_WALL_ID);
                String cid = objJson.getString(Constant.TAG_CAT_ID);
                String cat_name = objJson.getString(Constant.TAG_CAT_NAME);
                String img = objJson.getString(Constant.TAG_WALL_IMAGE).replace(" ", "%20");
                String img_thumb = objJson.getString(Constant.TAG_WALL_IMAGE_THUMB).replace(" ", "%20");
                String totalviews = objJson.getString(Constant.TAG_WALL_VIEWS);
                String totalrate = objJson.getString(Constant.TAG_WALL_TOTAL_RATE);
                String averagerate = objJson.getString(Constant.TAG_WALL_AVG_RATE);
                String tags = objJson.getString(Constant.TAG_WALL_TAGS);
                String type = objJson.getString(Constant.TAG_WALL_TYPE);

                ItemWallpaper itemWallpaper = new ItemWallpaper(id, cid, cat_name, img, img_thumb, totalviews, totalrate, averagerate, "", tags, type);
                arrayListFeatured.add(itemWallpaper);
                dbHelper.addWallpaper(itemWallpaper, "latest");
            }

            JSONArray jsonArray_potrait = jsonObj.getJSONArray(Constant.TAG_POTRAIT_WALL);
            for (int i = 0; i < jsonArray_potrait.length(); i++) {
                JSONObject objJson = jsonArray_potrait.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_WALL_ID);
                String cid = objJson.getString(Constant.TAG_CAT_ID);
                String cat_name = objJson.getString(Constant.TAG_CAT_NAME);
                String img = objJson.getString(Constant.TAG_WALL_IMAGE).replace(" ", "%20");
                String img_thumb = objJson.getString(Constant.TAG_WALL_IMAGE_THUMB).replace(" ", "%20");
                String totalviews = objJson.getString(Constant.TAG_WALL_VIEWS);
                String totalrate = objJson.getString(Constant.TAG_WALL_TOTAL_RATE);
                String averagerate = objJson.getString(Constant.TAG_WALL_AVG_RATE);
                String tags = objJson.getString(Constant.TAG_WALL_TAGS);
                String type = objJson.getString(Constant.TAG_WALL_TYPE);

                ItemWallpaper itemWallpaper = new ItemWallpaper(id, cid, cat_name, img, img_thumb, totalviews, totalrate, averagerate, "", tags, type);
                arrayListPotrait.add(itemWallpaper);
            }

            JSONArray jsonArray_landscape = jsonObj.getJSONArray(Constant.TAG_LANDSCAPE_WALL);
            for (int i = 0; i < jsonArray_landscape.length(); i++) {
                JSONObject objJson = jsonArray_landscape.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_WALL_ID);
                String cid = objJson.getString(Constant.TAG_CAT_ID);
                String cat_name = objJson.getString(Constant.TAG_CAT_NAME);
                String img = objJson.getString(Constant.TAG_WALL_IMAGE).replace(" ", "%20");
                String img_thumb = objJson.getString(Constant.TAG_WALL_IMAGE_THUMB).replace(" ", "%20");
                String totalviews = objJson.getString(Constant.TAG_WALL_VIEWS);
                String totalrate = objJson.getString(Constant.TAG_WALL_TOTAL_RATE);
                String averagerate = objJson.getString(Constant.TAG_WALL_AVG_RATE);
                String tags = objJson.getString(Constant.TAG_WALL_TAGS);
                String type = objJson.getString(Constant.TAG_WALL_TYPE);

                ItemWallpaper itemWallpaper = new ItemWallpaper(id, cid, cat_name, img, img_thumb, totalviews, totalrate, averagerate, "", tags, type);
                arrayListLandscape.add(itemWallpaper);
            }

            JSONArray jsonArray_square = jsonObj.getJSONArray(Constant.TAG_SQUARE_WALL);
            for (int i = 0; i < jsonArray_square.length(); i++) {
                JSONObject objJson = jsonArray_square.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_WALL_ID);
                String cid = objJson.getString(Constant.TAG_CAT_ID);
                String cat_name = objJson.getString(Constant.TAG_CAT_NAME);
                String img = objJson.getString(Constant.TAG_WALL_IMAGE).replace(" ", "%20");
                String img_thumb = objJson.getString(Constant.TAG_WALL_IMAGE_THUMB).replace(" ", "%20");
                String totalviews = objJson.getString(Constant.TAG_WALL_VIEWS);
                String totalrate = objJson.getString(Constant.TAG_WALL_TOTAL_RATE);
                String averagerate = objJson.getString(Constant.TAG_WALL_AVG_RATE);
                String tags = objJson.getString(Constant.TAG_WALL_TAGS);
                String type = objJson.getString(Constant.TAG_WALL_TYPE);

                ItemWallpaper itemWallpaper = new ItemWallpaper(id, cid, cat_name, img, img_thumb, totalviews, totalrate, averagerate, "", tags, type);
                arrayListSquare.add(itemWallpaper);
            }

            JSONArray jsonArray_cat = jsonObj.getJSONArray(Constant.TAG_WALL_CAT);
            for (int i = 0; i < jsonArray_cat.length(); i++) {
                JSONObject objJson = jsonArray_cat.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_CAT_ID);
                String name = objJson.getString(Constant.TAG_CAT_NAME);
                String image = objJson.getString(Constant.TAG_CAT_IMAGE).replace(" ", "%20");
                String image_thumb = objJson.getString(Constant.TAG_CAT_IMAGE).replace(" ", "%20");
                String tot_wall = objJson.getString(Constant.TAG_TOTAL_WALL);

                ItemCat itemCat = new ItemCat(id, name, image, image_thumb, tot_wall);
                arrayListCat.add(itemCat);
            }
            return true;
        } catch (Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean s) {
        homeListener.onEnd(String.valueOf(s), arrayListFeatured, arrayListPotrait, arrayListLandscape, arrayListSquare, arrayListCat);
        super.onPostExecute(s);
    }
}