package zs.wallpapers.site.hdwallpaper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import zs.bosong.frescozoomablelib.zoomable.DoubleTapGestureListener;
import zs.bosong.frescozoomablelib.zoomable.ZoomableController;
import zs.bosong.frescozoomablelib.zoomable.ZoomableDraweeView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.image.ImageInfo;


import zs.wallpapers.site.R;
import zs.wallpapers.site.interfaces.InterAdListener;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WallPaperViewActivity extends AppCompatActivity {

    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    DBHelper dbHelper;
    Toolbar toolbar;
    Methods methods;
    int position = 0;
    ZoomableDraweeView imageView;
    CircularProgressBar progressBar;
    int height = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_view);

        toolbar = findViewById(R.id.toolbar_wall_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = 55;
            toolbar.setLayoutParams(params);
        }

        dbHelper = new DBHelper(this);

        methods = new Methods(this, new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                new SaveTask("set").execute(Constant.arrayList.get(position).getImage());
            }
        });
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        height = methods.getScreenHeight();

        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        position = getIntent().getIntExtra("position", 0);

        progressBar = findViewById(R.id.pb_wall_view);
        imageView = findViewById(R.id.iv_wall_view);
        imageView.setTapListener(new DoubleTapGestureListener(imageView));
        Uri uri = Uri.parse(Constant.arrayList.get(position).getImage());
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setCallerContext("ZoomableApp-MyPagerAdapter")
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        progressBar.setVisibility(View.GONE);
                        super.onFinalImageSet(id, imageInfo, animatable);
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        progressBar.setVisibility(View.GONE);
                        super.onFailure(id, throwable);
                    }
                })
                .build();
        imageView.setController(controller);

        imageView.setSwipeDownListener(new ZoomableController.OnSwipeDownListener() {
            @Override
            public void onSwipeDown(float translateY) {

            }

            @Override
            public void onSwipeRelease(float translateY) {
                int a = (int) ((translateY / height) * 100);
                Log.e("aaa", a + "");
                if (a > 25) {
                    onBackPressed();
                    overridePendingTransition(0,android.R.anim.fade_out);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wallpaper, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_setwall:
                if (checkPer()) {
                    methods.showInter(position, "set");
                }
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    public Boolean checkPer() {
        if ((ContextCompat.checkSelfPermission(WallPaperViewActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {
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

    public class SaveTask extends AsyncTask<String, String, String> {
        URL myFileUrl;
        String option;
        File file;
        private ProgressDialog pDialog;


        SaveTask(String option) {
            this.option = option;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(WallPaperViewActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            if (option.equals("save")) {
                pDialog.setMessage(getResources().getString(R.string.downloading_wallpaper));
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
                File dir = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/Wallpapers");
                dir.mkdirs();
                file = new File(dir, fileName);

                if (!file.exists()) {
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
                        MediaScannerConnection.scanFile(WallPaperViewActivity.this, new String[]{file.getAbsolutePath()},
                                null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {

                                    }
                                });
                    }
                    return "1";
                } else {
                    return "2";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "0";
            }
        }

        @Override
        protected void onPostExecute(String args) {

            Constant.file = file;
            Constant.uri_setwall = Uri.fromFile(file);
            Intent intent = new Intent(WallPaperViewActivity.this, SetWallpaperActivity.class);
            startActivity(intent);
            pDialog.dismiss();
        }
    }
}