package xyz.tanwb.airship.utils;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.R;

public class ScreenUtils {

    private static final String SBH = "status_bar_height";
    private static final String NBH = "navigation_bar_height";
    private static final String DIMEN = "dimen";
    private static final String ANDROID = "android";

    private static float density = -1F;
    private static float scaledDensity = -1F;
    private static int widthPixels = -1;
    private static int heightPixels = -1;
    private static int statusBarHeight = -1;
    private static int actionBarHeight = -1;
    private static int navigationBarHeight = -1;

    public static float getDensity() {
        if (density <= 0F) {
            density = App.app().getResources().getDisplayMetrics().density;
        }
        return density;
    }

    public static float getScaledDensity() {
        if (scaledDensity <= 0F) {
            scaledDensity = App.app().getResources().getDisplayMetrics().scaledDensity;
        }
        return scaledDensity;
    }

    /**
     * 根据手机的分辨率从dp的单位转成为px(像素)
     */
    public static int dp2px(float dpValue) {
        // return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
        return (int) (dpValue * getDensity() + 0.5F);
    }

    /**
     * 根据手机的分辨率从sp的单位转成为px(像素)
     */
    public static int sp2px(float spValue) {
        // return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue,context.getResources().getDisplayMetrics());
        return (int) (spValue * getScaledDensity() + 0.5F);
    }

    /**
     * 根据手机的分辨率从px(像素)的单位转成为dp
     */
    public static float px2dp(float pxValue) {
        return pxValue / getDensity() + 0.5f;
    }

    /**
     * 根据手机的分辨率从px(像素)的单位转成为sp
     */
    public static float px2sp(float pxValue) {
        return pxValue / getScaledDensity() + 0.5f;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth() {
        if (widthPixels <= 0) {
            widthPixels = App.app().getResources().getDisplayMetrics().widthPixels;
        }
        return widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight() {
        if (heightPixels <= 0) {
            heightPixels = App.app().getResources().getDisplayMetrics().heightPixels;
        }
        return heightPixels;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight() {
        if (statusBarHeight <= 0) {
            int resourceId = App.app().getResources().getIdentifier(SBH, DIMEN, ANDROID);
            if (resourceId > 0) {
                statusBarHeight = App.app().getResources().getDimensionPixelSize(resourceId);
            }
        }
        return statusBarHeight;
    }

    /**
     * 获取ActionBar高度
     */
    public static int getActionBarHeight() {
        if (actionBarHeight <= 0) {
            TypedValue tv = new TypedValue();
            if (App.app().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, App.app().getResources().getDisplayMetrics());
            }
        }
        return actionBarHeight;
    }

    /**
     * 获取actionbar的像素高度，默认使用android官方兼容包做actionbar兼容
     *
     * @return
     */
    public static int getActionBarHeight(Context context) {
        if (actionBarHeight > 0) {
            return actionBarHeight;
        }
        if (context instanceof AppCompatActivity && ((AppCompatActivity) context).getSupportActionBar() != null) {
            actionBarHeight = ((AppCompatActivity) context).getSupportActionBar().getHeight();
        } else if (context instanceof Activity && ((Activity) context).getActionBar() != null) {
            actionBarHeight = ((Activity) context).getActionBar().getHeight();
        } else if (context instanceof ActivityGroup) {
            if (((ActivityGroup) context).getCurrentActivity() instanceof AppCompatActivity && ((AppCompatActivity) ((ActivityGroup) context).getCurrentActivity()).getSupportActionBar() != null) {
                actionBarHeight = ((AppCompatActivity) ((ActivityGroup) context).getCurrentActivity()).getSupportActionBar().getHeight();
            } else if (((ActivityGroup) context).getCurrentActivity() instanceof Activity && ((Activity) ((ActivityGroup) context).getCurrentActivity()).getActionBar() != null) {
                actionBarHeight = ((Activity) ((ActivityGroup) context).getCurrentActivity()).getActionBar().getHeight();
            }
        }
        if (actionBarHeight > 0) {
            return actionBarHeight;
        }
        final TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        } else {
            if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        }
        return actionBarHeight;
    }

    /**
     * 获取NavigationBar高度
     */
    public static int getNavigationBarHeight() {
        if (navigationBarHeight <= 0) {
            int resourceId = App.app().getResources().getIdentifier(NBH, DIMEN, ANDROID);
            if (resourceId > 0) {
                navigationBarHeight = App.app().getResources().getDimensionPixelSize(resourceId);
            }
        }
        return navigationBarHeight;
    }

    /**
     * 判断NavigationBar是否存在
     */
    public static boolean navigationBarExist(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        }

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    /**
     * 设置view margin
     */
    public static void setViewMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
}
