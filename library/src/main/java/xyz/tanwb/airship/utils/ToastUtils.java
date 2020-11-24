package xyz.tanwb.airship.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Toast工具类
 */
public class ToastUtils {

    public static void show(Context context, CharSequence message, int duration) {
        if (context != null && !TextUtils.isEmpty(message)) {
            Toast.makeText(context, message, duration).show();
        }
    }

    public static void show(Context context, int resId, int duration) {
        if (context != null) {
            Toast.makeText(context, resId, duration).show();
        }
    }

    public static void show(Context context, CharSequence message) {
        show(context, message, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, int resId) {
        show(context, resId, Toast.LENGTH_SHORT);
    }

    public static void showLong(Context context, CharSequence message) {
        show(context, message, Toast.LENGTH_LONG);
    }

    public static void showLong(Context context, int resId) {
        show(context, resId, Toast.LENGTH_LONG);
    }
}
