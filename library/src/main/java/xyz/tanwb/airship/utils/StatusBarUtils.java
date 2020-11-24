package xyz.tanwb.airship.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import xyz.tanwb.airship.view.widget.StatusBarView;

/**
 * 状态栏设置
 * <p>
 * 设置状态栏全透明后，整个activity布局都会上移充满整个屏幕，如果你不想让布局上移的话就需要在根布局设置
 * android:fitsSystemWindows="true"
 * <p>
 * PS:SVN地址》https://github.com/laobie/StatusBarUtil
 * PS:可替代systembartint》https://github.com/jgilfelt/SystemBarTint
 * <p>
 */
public class StatusBarUtils {

    private static final int DEFAULT_STATUS_BAR_ALPHA = 112;

    /**
     * 设置状态栏黑色字体图标。
     * 适配4.4以上版本MIUIV、Flyme和6.0以上Android版本
     */
    public static void setStatusBarMode(Activity activity, boolean isDrak) {
        Window window = activity.getWindow();
        switch (RomUtils.getSystemType()) {
            case 2:
                miuiSetStatusBarLightMode(window, isDrak);
                break;
            case 3:
                flymeSetStatusBarLightMode(window, isDrak);
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    defSetStatusBarLightMode(window, isDrak);
                } else {
                    setColor(activity, Color.BLACK, DEFAULT_STATUS_BAR_ALPHA);
                }
                break;
        }
    }

    /**
     * 设置状态栏字体图标为深色，需要魅族系统
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param isDark 是否把状态栏字体及图标颜色设置为深色
     */
    public static boolean flymeSetStatusBarLightMode(Window window, boolean isDark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (isDark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    /**
     * 设置状态栏字体图标为深色，需要小米系统6.0以上
     *
     * @param window 需要设置的窗口
     * @param isDark 是否把状态栏字体及图标颜色设置为深色
     */
    public static boolean miuiSetStatusBarLightMode(Window window, boolean isDark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (isDark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    /**
     * 设置状态栏字体图标为深色，需要原生系统6.0以上
     *
     * @param window 需要设置的窗口
     * @param isDark 是否把状态栏字体及图标颜色设置为深色
     */
    public static boolean defSetStatusBarLightMode(Window window, boolean isDark) {
        if (isDark) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        return true;
    }

    /**
     * 设置状态栏颜色为半透明
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     */
    public static void setColorDefTranslucent(Activity activity, @ColorInt int color) {
        setColor(activity, color, DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 设置状态栏纯色为不透明
     *
     * @param activity 需要设置的 activity
     * @param color    状态栏颜色值
     */
    public static void setColorNoTranslucent(Activity activity, int color) {
        setColor(activity, color, 0);
    }

    /**
     * 设置状态栏颜色为全透明
     *
     * @param activity 需要设置的 activity
     */
    public static void setColorToTransparent(Activity activity) {
        setColor(activity, Color.TRANSPARENT, 255);
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity       需要设置的activity
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度
     */
    public static void setColor(Activity activity, @ColorInt int color, int statusBarAlpha) {
        Window window = activity.getWindow();
        int calculateStatusColor = calculateStatusColor(color, statusBarAlpha);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //添加flag:FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //清楚flag:FLAG_TRANSLUCENT_STATUS(透明状态栏) 使ContentView向下空出状态栏位置
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //设置状态栏颜色
            window.setStatusBarColor(calculateStatusColor);
            // ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            // View mChildView = mContentView.getChildAt(0);
            // if (mChildView != null) {
            // // 注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
            // ViewCompat.setFitsSystemWindows(mChildView, true);
            // }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //添加flag:FLAG_TRANSLUCENT_STATUS(透明状态栏),使ContentView向上填充
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View statusView;
            ViewGroup decorView = (ViewGroup) window.getDecorView();
            int count = decorView.getChildCount();
            if (count > 0 && decorView.getChildAt(count - 1) instanceof StatusBarView) {
                statusView = decorView.getChildAt(count - 1);
            } else {
                statusView = createStatusBarView(activity);
                decorView.addView(statusView);
            }
            statusView.setBackgroundColor(calculateStatusColor);
            setRootView(activity);
        }
    }

    /**
     * 计算状态栏颜色
     *
     * @param color        color值
     * @param transparency 透明度(0~255)
     * @return 最终的状态栏颜色
     */
    private static int calculateStatusColor(@ColorInt int color, int transparency) {
        if (transparency == 255) {
            return Color.TRANSPARENT;
        } else if (transparency == 0) {
            return color;
        } else {
            return Color.argb(transparency, Color.red(color), Color.green(color), Color.blue(color));
            // float a = 1 - transparency / 255f;
            // int red = color >> 16 & 0xff;
            // int green = color >> 8 & 0xff;
            // int blue = color & 0xff;
            // red = (int) (red * a + 0.5);
            // green = (int) (green * a + 0.5);
            // blue = (int) (blue * a + 0.5);
            // return 0xff << 24 | red << 16 | green << 8 | blue;
        }
    }

    /**
     * 生成一个和状态栏大小相同的矩形条
     *
     * @param activity 需要设置的activity
     * @return 状态栏矩形条
     */
    private static StatusBarView createStatusBarView(Activity activity) {
        StatusBarView statusBarView = new StatusBarView(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getStatusBarHeight());
        statusBarView.setLayoutParams(params);
        return statusBarView;
    }

    /**
     * 设置根布局参数
     */
    private static void setRootView(Activity activity) {
        ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
        for (int i = 0, count = parent.getChildCount(); i < count; i++) {
            View childView = parent.getChildAt(i);
            if (childView instanceof ViewGroup) {
                childView.setFitsSystemWindows(true);
                ((ViewGroup) childView).setClipToPadding(true);
            }
        }
    }

    /**
     * 使状态栏半透明
     * <p>
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity 需要设置的activity
     */
    public static void setTranslucent(Activity activity) {
        setTranslucent(activity, DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 使状态栏半透明
     * <p>
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity       需要设置的activity
     * @param statusBarAlpha 状态栏透明度
     */
    public static void setTranslucent(Activity activity, int statusBarAlpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        setTransparent(activity);
        addTranslucentView(activity, statusBarAlpha);
    }

    /**
     * 设置状态栏全透明
     *
     * @param activity 需要设置的activity
     */
    public static void setTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        transparentStatusBar(activity);
        setRootView(activity);
    }

    /**
     * 针对根布局是 CoordinatorLayout, 使状态栏半透明
     * <p>
     * 适用于图片作为背景的界面,此时需要图片填充到状态栏
     *
     * @param activity       需要设置的activity
     * @param statusBarAlpha 状态栏透明度
     */
    public static void setTranslucentForCoordinatorLayout(Activity activity, int statusBarAlpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        transparentStatusBar(activity);
        addTranslucentView(activity, statusBarAlpha);
    }

    /**
     * 使状态栏透明
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void transparentStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 添加半透明矩形条
     *
     * @param activity       需要设置的 activity
     * @param statusBarAlpha 透明值
     */
    private static void addTranslucentView(Activity activity, int statusBarAlpha) {
        View statusBarView;
        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        if (contentView.getChildCount() > 1 && contentView.getChildAt(1) instanceof StatusBarView) {
            statusBarView = contentView.getChildAt(1);
        } else {
            statusBarView = createStatusBarView(activity);
            contentView.addView(statusBarView);
        }
        statusBarView.setBackgroundColor(Color.argb(statusBarAlpha, 0, 0, 0));
    }

    /**
     * 为头部是 ImageView 的界面设置状态栏全透明
     *
     * @param activity       需要设置的activity
     * @param needOffsetView 需要向下偏移的 View
     */
    public static void setTransparentForImageView(Activity activity, View needOffsetView) {
        setTranslucentForImageView(activity, 0, needOffsetView);
    }

    /**
     * 为头部是 ImageView 的界面设置状态栏透明(使用默认透明度)
     *
     * @param activity       需要设置的activity
     * @param needOffsetView 需要向下偏移的 View
     */
    public static void setTranslucentForImageView(Activity activity, View needOffsetView) {
        setTranslucentForImageView(activity, DEFAULT_STATUS_BAR_ALPHA, needOffsetView);
    }

    /**
     * 为头部是 ImageView 的界面设置状态栏透明
     *
     * @param activity       需要设置的activity
     * @param statusBarAlpha 状态栏透明度
     * @param needOffsetView 需要向下偏移的 View
     */
    public static void setTranslucentForImageView(Activity activity, int statusBarAlpha, View needOffsetView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        setTransparentForWindow(activity);
        addTranslucentView(activity, statusBarAlpha);
        if (needOffsetView != null) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) needOffsetView.getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + ScreenUtils.getStatusBarHeight(), layoutParams.rightMargin, layoutParams.bottomMargin);
        }
    }

    /**
     * 为 fragment 头部是 ImageView 的设置状态栏透明
     *
     * @param activity       fragment 对应的 activity
     * @param needOffsetView 需要向下偏移的 View
     */
    public static void setTranslucentForImageViewInFragment(Activity activity, View needOffsetView) {
        setTranslucentForImageViewInFragment(activity, 0, needOffsetView);
    }

    /**
     * 为 fragment 头部是 ImageView 的设置状态栏透明
     *
     * @param activity       fragment 对应的 activity
     * @param statusBarAlpha 状态栏透明度
     * @param needOffsetView 需要向下偏移的 View
     */
    public static void setTranslucentForImageViewInFragment(Activity activity, int statusBarAlpha, View needOffsetView) {
        setTranslucentForImageView(activity, statusBarAlpha, needOffsetView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            int count = decorView.getChildCount();
            if (count > 0 && decorView.getChildAt(count - 1) instanceof StatusBarView) {
                decorView.removeViewAt(count - 1);
                ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
                rootView.setPadding(0, 0, 0, 0);
            }
        }
    }

    /**
     * 为滑动返回界面设置半透明状态栏颜色
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     */
    public static void setColorForSwipeBack(Activity activity, int color) {
        setColorForSwipeBack(activity, color, DEFAULT_STATUS_BAR_ALPHA);
    }

    /**
     * 为滑动返回界面设置状态栏颜色
     *
     * @param activity       需要设置的activity
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度
     */
    public static void setColorForSwipeBack(Activity activity, @ColorInt int color, int statusBarAlpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int calculateStatusColor = calculateStatusColor(color, statusBarAlpha);
            ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
            View rootView = contentView.getChildAt(0);
            if (rootView != null && rootView instanceof CoordinatorLayout) {
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) rootView;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    coordinatorLayout.setFitsSystemWindows(false);
                    contentView.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0);
                    contentView.setBackgroundColor(calculateStatusColor);
                } else {
                    coordinatorLayout.setStatusBarBackgroundColor(calculateStatusColor);
                }
            } else {
                contentView.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0);
                contentView.setBackgroundColor(calculateStatusColor);
            }
            setTransparentForWindow(activity);
        }
    }

    /**
     * 设置透明
     */
    private static void setTransparentForWindow(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
