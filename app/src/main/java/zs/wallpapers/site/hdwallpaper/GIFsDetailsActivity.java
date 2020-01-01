package zs.wallpapers.site.hdwallpaper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import zs.wallpapers.site.GIFWallpaperService;
import zs.wallpapers.site.R;
import zs.wallpapers.site.adapter.AdapterTags;
import zs.wallpapers.site.asyncTask.GetRating;
import zs.wallpapers.site.asyncTask.LoadRating;



import zs.wallpapers.site.interfaces.GetRatingListener;
import zs.wallpapers.site.interfaces.InterAdListener;
import zs.wallpapers.site.interfaces.RatingListener;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.JSONParser;
import zs.wallpapers.site.utils.Methods;
import zs.wallpapers.site.utils.RecyclerItemClickListener;
import zs.wallpapers.site.utils.SharedPref;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GIFsDetailsActivity extends AppCompatActivity {

    DBHelper dbHelper;
    Toolbar toolbar;
    Methods methods;
    ViewPager viewpager;
    RecyclerView rv_tags;
    AdapterTags adapterTags;
    ArrayList<String> arrayListTags;
    LinearLayoutManager llm;
    int position;
    LinearLayout ll_download, ll_share, ll_rate, ll_fav, ll_setas;
    LikeButton button_fav, button_fav_wall;
    TextView tv_views, tv_downloads, tv_res, tv_size, tv_cat;
    LoadRating loadRating;
    Dialog dialog_rate;
    RatingBar ratingBar;
    RelativeLayout coordinatorLayout;
    SharedPref sharedPref;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    private Uri myuri;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_details);

        dbHelper = new DBHelper(this);
        sharedPref = new SharedPref(this);
        methods = new Methods(this, new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                switch (type) {
                    case "download":
                        new SaveTask("save").execute(Constant.arrayListGIF.get(viewpager.getCurrentItem()).getImage());
                        break;
                    case "share":
                        new SaveTask("share").execute(Constant.arrayListGIF.get(viewpager.getCurrentItem()).getImage());
                        break;
                    case "set":
                        new SaveTask("set").execute(Constant.arrayList.get(viewpager.getCurrentItem()).getImage());
                        break;
                }
            }
        });
        methods.forceRTLIfSupported(getWindow());


        toolbar = this.findViewById(R.id.toolbar_wall_details);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = 55;
            toolbar.setLayoutParams(params);
        }
        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        position = getIntent().getIntExtra("pos", 0);

        coordinatorLayout = findViewById(R.id.rl);
        button_fav_wall = findViewById(R.id.button_wall_fav);
        button_fav = findViewById(R.id.button_gif_fav);
        ll_download = findViewById(R.id.ll_download);
        ll_share = findViewById(R.id.ll_share);
        ll_rate = findViewById(R.id.ll_rate);
        ll_fav = findViewById(R.id.ll_fav);
        ll_setas = findViewById(R.id.ll_setas);
        tv_views = findViewById(R.id.tv_wall_details_views);
        tv_cat = findViewById(R.id.tv_details_cat);
        tv_res = findViewById(R.id.tv_details_resolution);
        tv_size = findViewById(R.id.tv_details_size);
        tv_downloads = findViewById(R.id.tv_wall_details_downloads);
        ratingBar = findViewById(R.id.rating_wall_details);
        loadViewed(position);

        ll_fav.setVisibility(View.VISIBLE);
        ll_setas.setVisibility(View.GONE);
        button_fav_wall.setVisibility(View.GONE);

        rv_tags = findViewById(R.id.rv_tags);
        llm = new LinearLayoutManager(GIFsDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rv_tags.setLayoutManager(llm);
        rv_tags.setItemAnimator(new DefaultItemAnimator());

        arrayListTags = new ArrayList<>(Arrays.asList(Constant.arrayListGIF.get(position).getTags().split(",")));
        adapterTags = new AdapterTags(arrayListTags);
        rv_tags.setAdapter(adapterTags);

        setTotalView(Constant.arrayListGIF.get(position).getTotalViews());
        ratingBar.setRating(Float.parseFloat(Constant.arrayListGIF.get(position).getAveargeRate()));

        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewpager = findViewById(R.id.vp_wall_details);
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(position);

        ll_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPer()) {
                    methods.showInter(0, "download");
                }
            }
        });

        ll_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPer()) {
                    methods.showInter(0, "share");
                }
            }
        });
        ll_setas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                /*methods.showInter(0, "set");*/
               /* Intent intent = new Intent(
                        WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra("check","yess");
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(GIFsDetailsActivity.this, GIFWallpaperService.class));
                startActivity(intent);*/
               /* Log.d("my uri", String.valueOf(myuri));
                Toast.makeText(GIFsDetailsActivity.this, "Set As Clicked", Toast.LENGTH_SHORT).show();*/
            }
        });

        button_fav.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                dbHelper.addtoFavoriteGIF(Constant.arrayListGIF.get(viewpager.getCurrentItem()));
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                dbHelper.removeFavGIF(Constant.arrayListGIF.get(viewpager.getCurrentItem()).getId());
            }
        });

        ll_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRateDialog();
            }
        });

        checkFav();

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                position = viewpager.getCurrentItem();
                checkFav();
                ratingBar.setRating(Float.parseFloat(Constant.arrayListGIF.get(position).getAveargeRate()));
                setTotalView(Constant.arrayListGIF.get(position).getTotalViews());
                tv_res.setText(Constant.arrayListGIF.get(position).getResolution());
                tv_size.setText(Constant.arrayListGIF.get(position).getSize());
                loadViewed(position);

                arrayListTags.clear();
                arrayListTags.addAll(Arrays.asList(Constant.arrayListGIF.get(position).getTags().split(",")));
                adapterTags.notifyDataSetChanged();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int position) {
            }

            @Override
            public void onPageScrollStateChanged(int position) {
            }
        });

        rv_tags.addOnItemTouchListener(new RecyclerItemClickListener(GIFsDetailsActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Constant.search_item = arrayListTags.get(position);
                Intent intent = new Intent(GIFsDetailsActivity.this, SearchGIFActivity.class);
                startActivity(intent);
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wallpaper, menu);
        menu.findItem(R.id.menu_setwall).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    public void checkFav() {

        button_fav.setLiked(dbHelper.isFavGIF(Constant.arrayListGIF.get(viewpager.getCurrentItem()).getId()));
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return Constant.arrayListGIF.size();

        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {

            View imageLayout = inflater.inflate(R.layout.layout_vp_gif, container, false);
            assert imageLayout != null;
            final SimpleDraweeView imageView = imageLayout.findViewById(R.id.imagegif);
            Picasso.get().load(Constant.arrayListGIF.get(position).getImage()).into(imageView);

            new AsyncTask<String, String, String>() {
                float aspect_ratio;

                @Override
                protected String doInBackground(String... strings) {
                    if (Constant.arrayListGIF.get(Integer.parseInt(strings[0])).getResolution().equals("")) {
                        final Bitmap image;
                        try {
                            image = Picasso.get().load(Constant.arrayListGIF.get(Integer.parseInt(strings[0])).getImage().replace(" ", "%20")).get();
                            float width = image.getWidth();
                            float height = image.getHeight();
                            aspect_ratio = width / height;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String[] a = Constant.arrayListGIF.get(Integer.parseInt(strings[0])).getResolution().split("X");
                        float width = Float.parseFloat(a[0]);
                        float height = Float.parseFloat(a[1]);
                        aspect_ratio = width / height;
                    }
                    return strings[0];
                }

                @Override
                protected void onPostExecute(String s) {
                    RelativeLayout.LayoutParams params;
                    if (aspect_ratio > 1) {
                        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    } else {
                        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    }
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    imageView.setLayoutParams(params);
                    imageView.setAspectRatio(aspect_ratio);
                    Uri uri = Uri.parse(Constant.arrayListGIF.get(Integer.parseInt(s)).getImage().replace(" ", "%20"));
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setUri(uri)
                            .setAutoPlayAnimations(true)
                            .build();
                    imageView.setController(controller);
                    super.onPostExecute(s);
                }
            }.execute(String.valueOf(position));

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GIFsDetailsActivity.this, GIFsViewActivity.class);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
            });

            container.addView(imageLayout, 0);
            return imageLayout;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    public class SaveTask extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;
        URL myFileUrl;
        String option;
        Bitmap bmImg = null;
        File file;

        SaveTask(String option) {
            this.option = option;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(GIFsDetailsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            if (option.equals("save")) {
                pDialog.setMessage(getResources().getString(R.string.downloading_gif));
            } else {
                pDialog.setMessage(getResources().getString(R.string.please_wait));
            }
            pDialog.setIndeterminate(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {
            try {
                myFileUrl = new URL(args[0]);
                String path = myFileUrl.getPath();
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                File dir = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/GIFs");
                dir.mkdirs();
                file = new File(dir, fileName);

                if (!file.exists()) {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte data[] = new byte[4096];
                        int count;
                        while ((count = is.read(data)) != -1) {
                            if (isCancelled()) {
                                is.close();
                                return null;
                            }
                            fos.write(data, 0, count);
                        }
                        fos.flush();
                        fos.close();

                        if (option.equals("save")) {
                            MediaScannerConnection.scanFile(GIFsDetailsActivity.this, new String[]{file.getAbsolutePath()},
                                    null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        @Override
                                        public void onScanCompleted(String path, Uri uri) {
                                            myuri=uri;
                                            Log.d("file path",path+" uri: "+uri);


                                        }
                                    });
                        }
                        return "1";
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "0";
                    }
                } else {
                    if (option.equals("set")) {
                        bmImg = BitmapFactory.decodeFile(file.getAbsolutePath());
                        Log.d("path",file.getAbsolutePath());
                    }
                    return "2";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "0";
            }
        }

        @Override
        protected void onPostExecute(String args) {
            if (args.equals("1") || args.equals("2")) {
                switch (option) {
                    case "save":
                        if (args.equals("2")) {
                            methods.showSnackBar(coordinatorLayout, getResources().getString(R.string.gif_already_saved));
                        } else {
                            if (methods.isNetworkAvailable()) {
                                new MyTask("download").execute(Constant.URL_GIF_DOWNLOAD + Constant.arrayListGIF.get(viewpager.getCurrentItem()).getId(), String.valueOf(viewpager.getCurrentItem()));
                            }
                            methods.showSnackBar(coordinatorLayout, getResources().getString(R.string.gif_saved));
                        }
                        break;
                    default:
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("image/gif");
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
                        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.get_more_gif) + "\n" + getString(R.string.app_name) + " - " + "https://play.google.com/store/apps/details?id=" + getPackageName());
                        startActivity(Intent.createChooser(share, getResources().getString(R.string.share_wallpaper)));
                        pDialog.dismiss();
                        break;
                }
            } else {
                methods.showSnackBar(coordinatorLayout, getResources().getString(R.string.please_try_again));
            }
            pDialog.dismiss();
        }
    }

    private void loadViewed(int pos) {
        if (methods.isNetworkAvailable()) {
            new MyTask("").execute(Constant.URL_GIF + Constant.arrayListGIF.get(pos).getId(), String.valueOf(pos));
        }
    }

    private class MyTask extends AsyncTask<String, Void, String> {
        String downloads = "", type = "", res = "", size = "";

        MyTask(String type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            String json = JSONParser.okhttpGET(params[0]);
            try {
                JSONObject jOb = new JSONObject(json);
                JSONArray jsonArray = jOb.getJSONArray(Constant.TAG_ROOT);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject objJson = jsonArray.getJSONObject(i);
                    downloads = objJson.getString(Constant.TAG_WALL_DOWNLOADS);
                    res = objJson.getString(Constant.TAG_RESOLUTION);
                    size = objJson.getString(Constant.TAG_SIZE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return params[1];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            int p = Integer.parseInt(result);
            Constant.arrayListGIF.get(p).setTotalDownload(downloads);
            setTotalDownloads(downloads);

            if (!type.equals("download")) {
                int tot = Integer.parseInt(Constant.arrayListGIF.get(p).getTotalViews());
                Constant.arrayListGIF.get(p).setTotalViews("" + (tot + 1));
            }

            tv_res.setText(res);
            tv_size.setText(size);
            Constant.arrayListGIF.get(p).setResolution(res);
            Constant.arrayListGIF.get(p).setSize(size);

            dbHelper.updateViewGIF(Constant.arrayListGIF.get(p).getId(), Constant.arrayListGIF.get(p).getTotalViews(), Constant.arrayListGIF.get(p).getTotalDownload(), Constant.arrayListGIF.get(p).getResolution(), Constant.arrayListGIF.get(p).getSize());
        }
    }

    private void openRateDialog() {
        dialog_rate = new Dialog(GIFsDetailsActivity.this);
        dialog_rate.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_rate.setContentView(R.layout.layout_rating);

        final ImageView iv_close = dialog_rate.findViewById(R.id.iv_rate_close);
        final RatingBar ratingBar = dialog_rate.findViewById(R.id.rating_add);
        ratingBar.setRating(1);
        final Button button = dialog_rate.findViewById(R.id.button_submit_rating);
        final TextView textView = dialog_rate.findViewById(R.id.tv_rate_dialog);

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (Constant.arrayListGIF.get(viewpager.getCurrentItem()).getUserRating().equals("0")) {
            new GetRating(new GetRatingListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onEnd(String success, String message, float rating) {
                    if (rating != 0 && success.equals("true")) {
                        ratingBar.setRating(rating);
                        textView.setText(getString(R.string.thanks_for_rating));
                    } else {
                        textView.setText(getString(R.string.rate_this_gif));
                    }
                }
            }).execute(Constant.URL_GET_GIF_RATING_1 + Constant.arrayListGIF.get(viewpager.getCurrentItem()).getId() + Constant.URL_GET_GIF_RATING_2 + deviceId);
        } else {
            textView.setText(getString(R.string.thanks_for_rating));
            ratingBar.setRating(Integer.parseInt(Constant.arrayList.get(viewpager.getCurrentItem()).getUserRating()));
        }

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_rate.dismiss();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratingBar.getRating() != 0) {
                    if (methods.isNetworkAvailable()) {
                        loadRatingApi(String.valueOf(ratingBar.getRating()));
                    } else {
                        methods.showSnackBar(coordinatorLayout, getResources().getString(R.string.internet_not_connected));
                    }
                } else {
                    Toast.makeText(GIFsDetailsActivity.this, getString(R.string.enter_rating), Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog_rate.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog_rate.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog_rate.show();
        Window window = dialog_rate.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void loadRatingApi(String rate) {
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(GIFsDetailsActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.loading));

        loadRating = new LoadRating(new RatingListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String message, float rating) {
                if (success.equals("true")) {
                    methods.showSnackBar(coordinatorLayout, message);

                    if (!message.contains("already")) {
                        Constant.arrayListGIF.get(viewpager.getCurrentItem()).setAveargeRate(String.valueOf(rating));
                        Constant.arrayListGIF.get(viewpager.getCurrentItem()).setUserRating(String.valueOf(rating));
                        ratingBar.setRating(rating);
                    }
                } else {
                    methods.showSnackBar(coordinatorLayout, getResources().getString(R.string.server_no_conn));
                }
                dialog_rate.dismiss();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        loadRating.execute(Constant.URL_RATING_GIF_1 + Constant.arrayListGIF.get(viewpager.getCurrentItem()).getId() + Constant.URL_RATING_GIF_2 + deviceId + Constant.URL_RATING_GIF_3 + rate);
    }

    private void setTotalView(String views) {
        try {
            tv_views.setText(methods.format(Double.parseDouble(views)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTotalDownloads(String downloads) {
        try {
            tv_downloads.setText(methods.format(Double.parseDouble(downloads)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean checkPer() {
        if ((ContextCompat.checkSelfPermission(GIFsDetailsActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }
}