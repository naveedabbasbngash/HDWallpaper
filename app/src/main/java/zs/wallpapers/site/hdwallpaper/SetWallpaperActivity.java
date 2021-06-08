package zs.wallpapers.site.hdwallpaper;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;

import zs.wallpapers.site.R;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.Methods;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetWallpaperActivity extends AppCompatActivity {

    Methods methods;
    CropImageView imageView;
    FloatingActionButton button;
    Bitmap bmImg;
    ProgressDialog progressDialog;
    BottomSheetDialog dialog_desc;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setwallpaper);

        methods = new Methods(this);
        methods.setStatusColor(getWindow());
        methods.forceRTLIfSupported(getWindow());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));

        ImageView imageView_back = findViewById(R.id.iv_back_crop);
        imageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageView = findViewById(R.id.iv_crop);
        button = findViewById(R.id.button_setwallpaper);

        imageView.setImageUriAsync(Constant.uri_setwall);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class SetWall extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
            bmImg = imageView.getCroppedImage();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            try {
                myWallpaperManager.setWallpaperOffsetSteps(0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    String locktype = strings[0];
                    switch (locktype) {
                        case "home":
                            myWallpaperManager.setBitmap(bmImg, null, true, WallpaperManager.FLAG_SYSTEM);
                            break;
                        case "lock":
                            myWallpaperManager.setBitmap(bmImg, null, true, WallpaperManager.FLAG_LOCK);
                            break;
                        case "all":
                            myWallpaperManager.setBitmap(bmImg);
                            break;
                    }
                } else {
                    myWallpaperManager.setBitmap(bmImg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "0";
            }
            return "1";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("1")) {
                Toast.makeText(SetWallpaperActivity.this, getString(R.string.wallpaper_set), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(SetWallpaperActivity.this, getString(R.string.err_set_wall), Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
            super.onPostExecute(s);
        }
    }

    public void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_set_wall_op, null);

        dialog_desc = new BottomSheetDialog(this);
        dialog_desc.setContentView(view);
        dialog_desc.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog_desc.show();

        TextView textView_home = dialog_desc.findViewById(R.id.tv_set_home);
        TextView textView_lock = dialog_desc.findViewById(R.id.tv_set_lock);
        TextView textView_all = dialog_desc.findViewById(R.id.tv_set_all);

        textView_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SetWall().execute("home");
            }
        });

        textView_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SetWall().execute("lock");
            }
        });

        textView_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SetWall().execute("all");
            }
        });
    }
}