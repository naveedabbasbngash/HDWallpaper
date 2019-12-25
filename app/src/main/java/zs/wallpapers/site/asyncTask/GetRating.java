package zs.wallpapers.site.asyncTask;

import android.os.AsyncTask;

import zs.wallpapers.site.interfaces.GetRatingListener;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetRating extends AsyncTask<String, String, Boolean> {

    private String rate = "0";
    private GetRatingListener ratingListener;

    public GetRating(GetRatingListener ratingListener) {
        this.ratingListener = ratingListener;
    }

    @Override
    protected void onPreExecute() {
        ratingListener.onStart();
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
                rate = c.getString(Constant.TAG_WALL_TOTAL_RATE);
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
        ratingListener.onEnd(String.valueOf(s), "", Float.parseFloat(rate));
        super.onPostExecute(s);
    }
}