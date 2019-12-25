package zs.wallpapers.site.asyncTask;

import android.os.AsyncTask;

import zs.wallpapers.site.interfaces.AboutListener;
import zs.wallpapers.site.items.ItemAbout;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoadAbout extends AsyncTask<String, String, Boolean> {

    private AboutListener aboutListener;

    public LoadAbout(AboutListener aboutListener) {
        this.aboutListener = aboutListener;
    }

    @Override
    protected void onPreExecute() {
        aboutListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            JSONObject jsonObject = new JSONObject(JSONParser.okhttpGET(Constant.URL_ABOUT));
            JSONArray jsonArray = jsonObject.getJSONArray(Constant.TAG_ROOT);
            JSONObject c;
            for (int i = 0; i < jsonArray.length(); i++) {
                c = jsonArray.getJSONObject(i);

                String appname = c.getString("app_name");
                String applogo = c.getString("app_logo");
                String desc = c.getString("app_description");
                String appversion = c.getString("app_version");
                String appauthor = c.getString("app_author");
                String appcontact = c.getString("app_contact");
                String email = c.getString("app_email");
                String website = c.getString("app_website");
                String privacy = c.getString("app_privacy_policy");
                String developedby = c.getString("app_developed_by");
                Constant.ad_banner_id = c.getString("banner_ad_id");
                Constant.ad_inter_id = c.getString("interstital_ad_id");
                Constant.isBannerAd = Boolean.parseBoolean(c.getString("banner_ad"));
                Constant.isInterAd = Boolean.parseBoolean(c.getString("interstital_ad"));
                Constant.ad_publisher_id = c.getString("publisher_id");
                Constant.adShow = Integer.parseInt(c.getString("interstital_ad_click"));
                Constant.isGIFEnabled = Boolean.parseBoolean(c.getString("gif_on_off"));

                Constant.itemAbout = new ItemAbout(appname, applogo, desc, appversion, appauthor, appcontact, email, website, privacy, developedby);
            }
            return true;
        } catch (Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean s) {
        aboutListener.onEnd(s);
        super.onPostExecute(s);
    }
}
