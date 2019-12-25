package zs.wallpapers.site.asyncTask;

import android.content.Context;
import android.os.AsyncTask;

import zs.wallpapers.site.interfaces.CategoryListener;
import zs.wallpapers.site.items.ItemCat;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadCat extends AsyncTask<String,String,Boolean> {

    private CategoryListener categoryListener;
    private ArrayList<ItemCat> arrayList_cat;
    private DBHelper dbHelper;

    public LoadCat(Context context, CategoryListener categoryListener) {
        this.categoryListener = categoryListener;
        arrayList_cat = new ArrayList<>();
        dbHelper = new DBHelper(context);
    }

    @Override
    protected void onPreExecute() {
        dbHelper.removeAllCat();
        categoryListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String url = strings[0];
        String json = JSONParser.okhttpGET(url);
        try {
            JSONObject jOb = new JSONObject(json);
            JSONArray jsonArray = jOb.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                String id = c.getString(Constant.TAG_CAT_ID);
                String name = c.getString(Constant.TAG_CAT_NAME);
                String image = c.getString(Constant.TAG_CAT_IMAGE).replace(" ","%20");
                String image_thumb = c.getString(Constant.TAG_CAT_IMAGE).replace(" ","%20");
                String tot_wall = c.getString(Constant.TAG_TOTAL_WALL);

                ItemCat itemCat = new ItemCat(id,name,image, image_thumb,tot_wall);
                arrayList_cat.add(itemCat);
                dbHelper.addtoCatList(itemCat);
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean s) {
        categoryListener.onEnd(String.valueOf(s),arrayList_cat);
        super.onPostExecute(s);
    }
}
