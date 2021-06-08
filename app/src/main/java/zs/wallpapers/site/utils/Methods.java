package zs.wallpapers.site.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.snackbar.Snackbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import zs.wallpapers.site.R;
import zs.wallpapers.site.interfaces.InterAdListener;

import java.text.DecimalFormat;

public class Methods {

    private Context context;
    private InterstitialAd mInterstitial;
    private InterAdListener interAdListener;


    // constructor
    public Methods(Context context) {
        this.context = context;
    }

    // constructor
    public Methods(Context context, InterAdListener interAdListener) {
        this.context = context;
        this.interAdListener = interAdListener;
//        loadInter();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    public int getScreenHeight() {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        point.y = display.getHeight();

        return point.y;
    }

    public void forceRTLIfSupported(Window window) {
        if (context.getResources().getString(R.string.isRTL).equals("true")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }
    }

//    private void loadInter() {
//        mInterstitial = new InterstitialAd(context);
//        mInterstitial.setAdUnitId(Constant.ad_inter_id);
//        mInterstitial.loadAd(new AdRequest.Builder().build());
//    }

    public void showInter(final int pos, final String type) {
        if (Constant.isInterAd) {
            Constant.adCount = Constant.adCount + 1;
            if (Constant.adCount % Constant.adShow == 0) {
//                if (mInterstitial.isLoaded()) {
                final InterstitialAd interstitialAd = new InterstitialAd(context);
                AdRequest adRequest;
                if (ConsentInformation.getInstance(context).getConsentStatus() == ConsentStatus.PERSONALIZED) {
                    adRequest = new AdRequest.Builder()
                            .build();
                } else {
                    Bundle extras = new Bundle();
                    extras.putString("npa", "1");
                    adRequest = new AdRequest.Builder()
                            .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                            .build();
                }
                interstitialAd.setAdUnitId(Constant.ad_inter_id);
                interstitialAd.loadAd(adRequest);
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        interstitialAd.show();
                    }

                    public void onAdClosed() {
                        interAdListener.onClick(pos, type);
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        interAdListener.onClick(pos, type);
                        super.onAdFailedToLoad(i);
                    }

                });
//                } else {
//                    interAdListener.onClick(pos, type);
//                }
            } else {
                interAdListener.onClick(pos, type);
            }
        } else {
            interAdListener.onClick(pos, type);
        }
    }

    private void showPersonalizedAds(LinearLayout linearLayout) {
        if (Constant.isBannerAd) {
            AdView adView = new AdView(context);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdUnitId(Constant.ad_banner_id);
            adView.setAdSize(AdSize.BANNER);
            linearLayout.addView(adView);
            adView.loadAd(adRequest);
        }
    }

    private void showNonPersonalizedAds(LinearLayout linearLayout) {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        if (Constant.isBannerAd) {
            AdView adView = new AdView(context);
            AdRequest adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            adView.setAdUnitId(Constant.ad_banner_id);
            adView.setAdSize(AdSize.BANNER);
            linearLayout.addView(adView);
            adView.loadAd(adRequest);
        }
    }

    public void showBannerAd(LinearLayout linearLayout) {
        if (isNetworkAvailable()) {
            if (ConsentInformation.getInstance(context).getConsentStatus() == ConsentStatus.NON_PERSONALIZED) {
                showNonPersonalizedAds(linearLayout);
            } else {
                showPersonalizedAds(linearLayout);
            }
        }
    }

    public void showSnackBar(View linearLayout, String message) {
        Snackbar snackbar = Snackbar.make(linearLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundResource(R.drawable.bg_grt_toolbar);
        snackbar.show();
    }

    public void setStatusColor(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public String format(Number number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    public String getImageThumbSize(String imagePath, String type) {
        if (type.equals(context.getString(R.string.portrait))) {
            imagePath = imagePath.replace("&size=300x300", "&size=200x350");
        } else if (type.equals(context.getString(R.string.landscape))) {
            imagePath = imagePath.replace("&size=300x300", "&size=350x200");
        } else if (type.equals(context.getString(R.string.square))) {
            imagePath = imagePath.replace("&size=300x300", "&size=300x300");
        } else if (type.equals(context.getString(R.string.details))) {
            imagePath = imagePath.replace("&size=300x300", "&size=500x500");
        } else if (type.equals(context.getString(R.string.home))) {
            imagePath = imagePath.replace("&size=300x300", "&size=500x500");
        }
        return imagePath;
    }

    public int getColumnWidth(int column, int grid_padding) {
        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, grid_padding, r.getDisplayMetrics());
        return (int) ((getScreenWidth() - ((column + 1) * padding)) / column);
    }

    public int getWallTypePos(String wallType) {
        if (wallType.equals("")) {
            return 0;
        } else if (wallType.equals(context.getString(R.string.portrait))) {
            return 1;
        } else if (wallType.equals(context.getString(R.string.landscape))) {
            return 2;
        } else if (wallType.equals(context.getString(R.string.square))) {
            return 3;
        } else {
            return -1;
        }
    }
}