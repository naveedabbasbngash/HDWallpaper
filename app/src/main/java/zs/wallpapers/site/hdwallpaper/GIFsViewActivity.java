package zs.wallpapers.site.hdwallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.squareup.picasso.Picasso;

import zs.wallpapers.site.R;
import zs.wallpapers.site.utils.Constant;
import zs.wallpapers.site.utils.DBHelper;
import zs.wallpapers.site.utils.Methods;

import java.io.IOException;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GIFsViewActivity extends AppCompatActivity {

    DBHelper dbHelper;
    Toolbar toolbar;
    Methods methods;
    int position = 0;
    SimpleDraweeView imageView;
    CircularProgressBar progressBar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_view);

        toolbar = findViewById(R.id.toolbar_gif_view);
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

        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());
        methods.setStatusColor(getWindow());

        toolbar.setTitle("");
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        position = getIntent().getIntExtra("position", 0);

        progressBar = findViewById(R.id.pb_gif_view);
        imageView = findViewById(R.id.iv_gif_view);

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

                ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(imageRequest.getSourceUri())
                        .setControllerListener(controllerListener)
                        .setAutoPlayAnimations(true).build();

                imageView.setController(controller);
                super.onPostExecute(s);
            }
        }.execute(String.valueOf(position));
    }

    ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
            if (anim != null) {
                anim.start();
            }
        }
    };


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
}