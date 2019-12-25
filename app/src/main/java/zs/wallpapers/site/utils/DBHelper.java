package zs.wallpapers.site.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import zs.wallpapers.site.items.ItemAbout;
import zs.wallpapers.site.items.ItemCat;
import zs.wallpapers.site.items.ItemGIF;
import zs.wallpapers.site.items.ItemWallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static String DB_FAKE_NAME = "wall.db";
    private static String DB_NAME = "wallpaper.db";
    private SQLiteDatabase db;
    private final Context context;
    private String DB_PATH;

    public DBHelper(Context context) {
        super(context, DB_FAKE_NAME, null, 4);
        this.context = context;
//        DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        DB_PATH = getReadableDatabase().getPath().substring(0,(getReadableDatabase().getPath().length()-DB_FAKE_NAME.length()));
    }

    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();
        if (!dbExist) {
            getWritableDatabase();
            copyDataBase();
        } else {
            this.getWritableDatabase();
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {

        InputStream myInput = context.getAssets().open(DB_FAKE_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private Cursor getData(String Query) {
        String myPath = DB_PATH + DB_NAME;
        Cursor c = null;
        try {
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            c = db.rawQuery(Query, null);
        } catch (Exception e) {
            Log.e("Err", e.toString());
        }
        return c;
    }

    //UPDATE temp_dquot SET age='20',name1='--',rdt='11/08/2014',basic_sa='100000',plno='814',pterm='20',mterm='20',mat_date='11/08/2034',mode='YLY',dab_sa='100000',tr_sa='0',cir_sa='',bonus_rate='42',prem='5276',basic_prem='5118',dab_prem='100.0',step_rate='for Life',loyal_rate='0',bonus_rate='42',act_mat='1,88,000',mly_b_pr='448',qly_b_pr='1345',hly_b_pr='2664',yly_b_pr='5276'  WHERE uniqid=1
    private void dml(String Query) {
        String myPath = DB_PATH + DB_NAME;
        if (db == null)
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        try {
            db.execSQL(Query);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    public ArrayList<ItemWallpaper> getWallpapers(String table, String typeee, String wallType) {
        ArrayList<ItemWallpaper> arrayList = new ArrayList<>();

        if(wallType.equals("")) {
            wallType = "'";
        } else {
            wallType = "' where type='" + wallType+"'";
        }

        String query = "";
        switch (typeee) {
            case "":
                query = "select * from '" + table + wallType;
                break;
            case "views":
                query = "select * from '" + table + "' order by views + 0 desc" +  wallType;
                break;
            case "rate":
                query = "select * from '" + table + "' order by avg_rate + 0 desc" + wallType;
                break;
        }
        Cursor cursor = getData(query);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String pid = cursor.getString(cursor.getColumnIndex("pid"));
                String cid = cursor.getString(cursor.getColumnIndex("cid"));
                String cname = cursor.getString(cursor.getColumnIndex("cname"));
                String img = cursor.getString(cursor.getColumnIndex("img"));
                String img_thumb = cursor.getString(cursor.getColumnIndex("img_thumb"));
                String views = cursor.getString(cursor.getColumnIndex("views"));
                String totalrate = cursor.getString(cursor.getColumnIndex("total_rate"));
                String averagerate = cursor.getString(cursor.getColumnIndex("avg_rate"));
                String download = cursor.getString(cursor.getColumnIndex("total_download"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String res = cursor.getString(cursor.getColumnIndex("res"));
                String size = cursor.getString(cursor.getColumnIndex("size"));

                ItemWallpaper itemWallpaper = new ItemWallpaper(pid, cid, cname, img, img_thumb, views, totalrate, averagerate, download, tags, type);
                itemWallpaper.setResolution(res);
                itemWallpaper.setSize(size);
                arrayList.add(itemWallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public ArrayList<ItemWallpaper> getWallByCat(String table, String id, String wallType) {
        ArrayList<ItemWallpaper> arrayList = new ArrayList<>();

        if(wallType.equals("")) {
            wallType = "'";
        } else {
            wallType = "' &type='" + wallType+"'";
        }

        Cursor cursor = getData("select * from '" + table + "' where cid = '" + id + wallType);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String pid = cursor.getString(cursor.getColumnIndex("pid"));
                String cid = cursor.getString(cursor.getColumnIndex("cid"));
                String cname = cursor.getString(cursor.getColumnIndex("cname"));
                String img = cursor.getString(cursor.getColumnIndex("img"));
                String img_thumb = cursor.getString(cursor.getColumnIndex("img_thumb"));
                String views = cursor.getString(cursor.getColumnIndex("views"));
                String totalrate = cursor.getString(cursor.getColumnIndex("total_rate"));
                String averagerate = cursor.getString(cursor.getColumnIndex("avg_rate"));
                String download = cursor.getString(cursor.getColumnIndex("total_download"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String res = cursor.getString(cursor.getColumnIndex("res"));
                String size = cursor.getString(cursor.getColumnIndex("size"));

                ItemWallpaper itemWallpaper = new ItemWallpaper(pid, cid, cname, img, img_thumb, views, totalrate, averagerate, download, tags, type);
                itemWallpaper.setResolution(res);
                itemWallpaper.setSize(size);
                arrayList.add(itemWallpaper);

                cursor.moveToNext();
            }
            cursor.close();
        }

        return arrayList;
    }

    public ArrayList<ItemGIF> getGIFs() {
        ArrayList<ItemGIF> arrayList = new ArrayList<>();
        Cursor cursor = getData("select * from gif");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String gid = cursor.getString(cursor.getColumnIndex("gid"));
                String img = cursor.getString(cursor.getColumnIndex("image"));
                String views = cursor.getString(cursor.getColumnIndex("views"));
                String total_rate = cursor.getString(cursor.getColumnIndex("total_rate"));
                String avg_rate = cursor.getString(cursor.getColumnIndex("avg_rate"));
                String download = cursor.getString(cursor.getColumnIndex("total_download"));
                String tags = cursor.getString(cursor.getColumnIndex("tags"));
                String res = cursor.getString(cursor.getColumnIndex("res"));
                String size = cursor.getString(cursor.getColumnIndex("size"));

                ItemGIF itemGIF = new ItemGIF(gid, img, views, total_rate, avg_rate, download, tags);
                itemGIF.setResolution(res);
                itemGIF.setSize(size);
                arrayList.add(itemGIF);

                cursor.moveToNext();
            }
            cursor.close();
        }
        return arrayList;
    }

    public ArrayList<ItemCat> getCat() {
        ArrayList<ItemCat> arrayList = new ArrayList<>();

        Cursor cursor = getData("select * from cat");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String cid = cursor.getString(cursor.getColumnIndex("cid"));
                String cname = cursor.getString(cursor.getColumnIndex("cname"));
                String img = cursor.getString(cursor.getColumnIndex("img"));
                String tot_wall = cursor.getString(cursor.getColumnIndex("tot_wall"));

                ItemCat itemCategory = new ItemCat(cid, cname, img, "a", tot_wall);
                arrayList.add(itemCategory);

                cursor.moveToNext();
            }
            cursor.close();
        }
        return arrayList;
    }

    public Boolean isFav(String id) {
        String selectQuery = "SELECT  * FROM fav WHERE pid=" + "'" + id + "'";

        Cursor cursor = getData(selectQuery);
        return cursor != null && cursor.getCount() > 0;
    }

    public Boolean isFavGIF(String id) {
        String selectQuery = "SELECT * FROM gif WHERE gid=" + "'" + id + "'";

        Cursor cursor = getData(selectQuery);
        return cursor != null && cursor.getCount() > 0;
    }

    public void addtoFavorite(ItemWallpaper itemWallpaper) {
        dml("insert into fav (pid,cid,cname,img,img_thumb,views, total_rate, avg_rate, total_download, tags, type, res, size) values ('" + itemWallpaper.getId() + "','" + itemWallpaper.getCId() + "','" + itemWallpaper.getCName() + "','" + itemWallpaper.getImage() + "','" + itemWallpaper.getImageThumb() + "','" + itemWallpaper.getTotalViews() + "','" + itemWallpaper.getTotalRate() + "','" + itemWallpaper.getAverageRate() + "','" + itemWallpaper.getTotalDownloads() + "','" + itemWallpaper.getTags() + "','" + itemWallpaper.getType() + "','" + itemWallpaper.getResolution() + "','" + itemWallpaper.getSize() + "')");
    }

    public void removeFav(String id) {
        dml("delete from fav where pid = '" + id + "'");
    }

    public void addWallpaper(ItemWallpaper itemWallpaper, String table) {
        dml("insert into '" + table + "' (pid,cid,cname,img,img_thumb,views, total_rate, avg_rate, total_download, tags, type, res, size) values ('" + itemWallpaper.getId() + "','" + itemWallpaper.getCId() + "','" + itemWallpaper.getCName() + "','" + itemWallpaper.getImage() + "','" + itemWallpaper.getImageThumb() + "','" + itemWallpaper.getTotalViews() + "','" + itemWallpaper.getTotalRate() + "','" + itemWallpaper.getAverageRate() + "','" + itemWallpaper.getTotalDownloads() + "','" + itemWallpaper.getTags() + "','" + itemWallpaper.getType() + "','" + itemWallpaper.getResolution() + "','" + itemWallpaper.getSize() + "')");
    }

    public void removeAllWallpaper(String table) {
        dml("delete from '" + table + "'");
    }

    public void removeWallByCat(String table, String id) {
        dml("delete from '" + table + "' where cid= '" + id + "'");
    }

    public void addtoFavoriteGIF(ItemGIF itemGIF) {
        dml("insert into gif (gid,image,views, total_rate, avg_rate, total_download, tags, res, size) values ('" + itemGIF.getId() + "','" + itemGIF.getImage() + "','" + itemGIF.getTotalViews() + "','" + itemGIF.getTotalRate() + "','" + itemGIF.getAveargeRate() + "','" + itemGIF.getTotalDownload() + "','" + itemGIF.getTags() + "','" + itemGIF.getResolution() + "','" + itemGIF.getSize() + "')");
    }

    public void removeFavGIF(String id) {
        dml("delete from gif where gid = '" + id + "'");
    }

    public void addtoCatList(ItemCat itemCategory) {
        dml("insert into cat (cid,cname,img,tot_wall) values ('" + itemCategory.getId() + "','" + itemCategory.getName() + "','" + itemCategory.getImage() + "','" + itemCategory.getTotalWallpaper() + "')");
    }

    public void removeAllCat() {
        dml("delete from cat");
    }

    public void updateView(String id, String totview, String download, String reso, String size) {
        dml("update catlist set views = '" + totview + "', total_download = '" + download + "', res = '" + reso + "', size = '" + size + "'  where pid = '" + id + "'");
        dml("update fav set views = '" + totview + "', total_download = '" + download + "', res = '" + reso + "', size = '" + size + "' where pid = '" + id + "'");
        dml("update latest set views = '" + totview + "', total_download = '" + download + "', res = '" + reso + "', size = '" + size + "' where pid = '" + id + "'");
    }

    public void updateViewGIF(String id, String totview, String download, String reso, String size) {
        int views = Integer.parseInt(totview) + 1;
        dml("update gif set views = '" + String.valueOf(views) + "', total_download = '" + download + "', res = '" + reso + "', size = '" + size + "' where gid = '" + id + "'");
    }

    public void addtoAbout() {
        try {
            dml("delete from about");
            dml("insert into about (name,logo,version,author,contact,email,website,desc,developed,privacy, ad_pub, ad_banner, ad_inter, isbanner, isinter, click) values (" +
                    "'" + Constant.itemAbout.getAppName() + "','" + Constant.itemAbout.getAppLogo() + "','" + Constant.itemAbout.getAppVersion() + "'" +
                    ",'" + Constant.itemAbout.getAuthor() + "','" + Constant.itemAbout.getContact() + "','" + Constant.itemAbout.getEmail() + "'" +
                    ",'" + Constant.itemAbout.getWebsite() + "','" + Constant.itemAbout.getAppDesc() + "','" + Constant.itemAbout.getDevelopedby() + "'" +
                    ",'" + Constant.itemAbout.getPrivacy() + "','" + Constant.ad_publisher_id + "','" + Constant.ad_banner_id + "','" + Constant.ad_inter_id + "'" +
                    ",'" + Constant.isBannerAd + "','" + Constant.isInterAd + "','" + Constant.adShow + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean getAbout() {
        String selectQuery = "SELECT * FROM about";

        Cursor c = getData(selectQuery);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                String appname = c.getString(c.getColumnIndex("name"));
                String applogo = c.getString(c.getColumnIndex("logo"));
                String desc = c.getString(c.getColumnIndex("desc"));
                String appversion = c.getString(c.getColumnIndex("version"));
                String appauthor = c.getString(c.getColumnIndex("author"));
                String appcontact = c.getString(c.getColumnIndex("contact"));
                String email = c.getString(c.getColumnIndex("email"));
                String website = c.getString(c.getColumnIndex("website"));
                String privacy = c.getString(c.getColumnIndex("privacy"));
                String developedby = c.getString(c.getColumnIndex("developed"));

                Constant.ad_banner_id = c.getString(c.getColumnIndex("ad_banner"));
                Constant.ad_inter_id = c.getString(c.getColumnIndex("ad_inter"));
                Constant.isBannerAd = Boolean.parseBoolean(c.getString(c.getColumnIndex("isbanner")));
                Constant.isInterAd = Boolean.parseBoolean(c.getString(c.getColumnIndex("isinter")));
                Constant.ad_publisher_id = c.getString(c.getColumnIndex("ad_pub"));
                Constant.adShow = Integer.parseInt(c.getString(c.getColumnIndex("click")));

                Constant.itemAbout = new ItemAbout(appname, applogo, desc, appversion, appauthor, appcontact, email, website, privacy, developedby);
            }
            c.close();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("aaa", "upgrade");
        Log.e("aaa -oldVersion", "" + oldVersion);
        Log.e("aaa -newVersion", "" + newVersion);
        try {

            String myPath = "/data/data/" + context.getPackageName() + "/" + "databases/" + DB_NAME;
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            switch (oldVersion) {
                case 1:
                case 2:
                    db.execSQL("ALTER TABLE gif ADD 'total_rate' TEXT");
                    db.execSQL("ALTER TABLE gif ADD 'avg_rate' TEXT");
                    db.execSQL("ALTER TABLE gif ADD 'total_download' TEXT");
                    db.execSQL("ALTER TABLE gif ADD 'tags' TEXT");

                    db.execSQL("ALTER TABLE latest ADD 'total_rate' TEXT");
                    db.execSQL("ALTER TABLE latest ADD 'avg_rate' TEXT");
                    db.execSQL("ALTER TABLE latest ADD 'total_download' TEXT");
                    db.execSQL("ALTER TABLE latest ADD 'tags' TEXT");

                    db.execSQL("ALTER TABLE fav ADD 'total_rate' TEXT");
                    db.execSQL("ALTER TABLE fav ADD 'avg_rate' TEXT");
                    db.execSQL("ALTER TABLE fav ADD 'total_download' TEXT");
                    db.execSQL("ALTER TABLE fav ADD 'tags' TEXT");

                    db.execSQL("ALTER TABLE catlist ADD 'total_rate' TEXT");
                    db.execSQL("ALTER TABLE catlist ADD 'avg_rate' TEXT");
                    db.execSQL("ALTER TABLE catlist ADD 'total_download' TEXT");
                    db.execSQL("ALTER TABLE catlist ADD 'tags' TEXT");

                    db.execSQL("ALTER TABLE about ADD 'ad_pub' TEXT");
                    db.execSQL("ALTER TABLE about ADD 'ad_banner' TEXT");
                    db.execSQL("ALTER TABLE about ADD 'ad_inter' TEXT");
                    db.execSQL("ALTER TABLE about ADD 'isbanner' TEXT");
                    db.execSQL("ALTER TABLE about ADD 'isinter' TEXT");
                    db.execSQL("ALTER TABLE about ADD 'click' TEXT");
                case 3:

                    db.execSQL("ALTER TABLE latest ADD 'type' TEXT");
                    db.execSQL("ALTER TABLE latest ADD 'res' TEXT");
                    db.execSQL("ALTER TABLE latest ADD 'size' TEXT");

                    db.execSQL("ALTER TABLE fav ADD 'type' TEXT");
                    db.execSQL("ALTER TABLE fav ADD 'res' TEXT");
                    db.execSQL("ALTER TABLE fav ADD 'size' TEXT");

                    db.execSQL("ALTER TABLE catlist ADD 'type' TEXT");
                    db.execSQL("ALTER TABLE catlist ADD 'res' TEXT");
                    db.execSQL("ALTER TABLE catlist ADD 'size' TEXT");

                    db.execSQL("ALTER TABLE gif ADD 'res' TEXT");
                    db.execSQL("ALTER TABLE gif ADD 'size' TEXT");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}