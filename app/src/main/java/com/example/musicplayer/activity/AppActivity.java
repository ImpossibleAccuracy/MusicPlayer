package com.example.musicplayer.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.MusicApplication;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;

public abstract class AppActivity extends AppCompatActivity {
    public static final int MODE_NORMAL = 1;
    public static final int MODE_LIGHT = 2;
    public static final int MODE_TRANSPARENT = 3;

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_UNKNOWN = -1;

    private boolean activityExist;

    protected abstract void findUI();
    protected abstract void fillUI();

    public Player getPlayer() {
        return ((MusicApplication)getApplication()).getPlayer();
    }
    public MusicRepository getMusicRepository() {
        return ((MusicApplication)getApplication()).getMusicRepository();
    }

    public boolean activityExist() {
        return activityExist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityExist = true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityExist = false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        fillUI();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        findUI();
    }

    public void setWindowMode(int mode) {
        Window window = getWindow();

        if (mode == MODE_NORMAL) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                window.setNavigationBarColor(getResources().getColor(R.color.colorPrimary));
            }
        }
        else if (mode == MODE_LIGHT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.topBarColor));
                window.setNavigationBarColor(getResources().getColor(R.color.backgroundColor));
            }

            if (getDeviceTheme() == THEME_LIGHT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowInsetsController windowInsetsController = window.getInsetsController();

                    windowInsetsController.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                    windowInsetsController.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }
                else {
                    View decorView = window.getDecorView();
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                }
            }
        }
        else if (mode == MODE_TRANSPARENT) {
            if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
                setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true);
            }
            if (Build.VERSION.SDK_INT >= 19) {
                int visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                visibility = visibility | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                window.getDecorView().setSystemUiVisibility(visibility);
            }
            if (Build.VERSION.SDK_INT >= 21) {
                int windowManager = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                windowManager = windowManager | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                setWindowFlag(windowManager, false);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
        }
    }

    public int getDeviceTheme() {
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_NO:
                return THEME_LIGHT;
            case Configuration.UI_MODE_NIGHT_YES:
                return THEME_DARK;
        }
        return THEME_UNKNOWN;
    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public void setWindowFlag(final int bits, final boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static void SetViewMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
    public static void SetViewSize(View v, int w, int h) {
        v.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        v.requestLayout();
    }
}
