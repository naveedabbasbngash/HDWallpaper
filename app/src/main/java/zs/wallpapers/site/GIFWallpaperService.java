package zs.wallpapers.site;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GIFWallpaperService extends WallpaperService {
    private File filePath;

    @Override
    public void onCreate() {
        super.onCreate();

    }




    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        String userID = intent.getStringExtra("check");
        Log.d("check",userID+"aaaaaaa");
        return START_STICKY;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public WallpaperService.Engine onCreateEngine() {
       Uri uri= Uri.parse("content://media/external/images/media/65335");
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        Movie movie = Movie.decodeStream(
                is);

        return new GIFWallpaperEngine(movie);
    }

    private class GIFWallpaperEngine extends WallpaperService.Engine {

        private final int frameDuration = 20;

        private SurfaceHolder holder;
        private Movie movie;
        private boolean visible;
        private Handler handler;

        public GIFWallpaperEngine(Movie movie) {
            this.movie = movie;
            handler = new Handler();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.holder = surfaceHolder;
        }

        private Runnable drawGIF = new Runnable() {
            public void run() {
                draw();
            }
        };


        private void draw() {
            if (visible) {
                Canvas canvas = holder.lockCanvas();
                canvas.save();
                // Adjust size and position so that
                // the image looks good on your screen
                canvas.scale(3f, 3f);
                movie.draw(canvas, -100, 0);
                canvas.restore();
                holder.unlockCanvasAndPost(canvas);
                movie.setTime((int) (System.currentTimeMillis() % movie.duration()));

                handler.removeCallbacks(drawGIF);
                handler.postDelayed(drawGIF, frameDuration);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawGIF);
            } else {
                handler.removeCallbacks(drawGIF);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawGIF);
        }
    }


}