package xyz.tanwb.airship.utils;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * 上色渲染
 */
public final class DrawableUtils {

    public static Drawable tint(Drawable originDrawable, int color) {
        return tint(originDrawable, ColorStateList.valueOf(color), null);
    }

    public static Drawable tint(Drawable originDrawable, ColorStateList colorStateList) {
        return tint(originDrawable, colorStateList, null);
    }

    public static Drawable tint(Drawable originDrawable, int color, PorterDuff.Mode tintMode) {
        return tint(originDrawable, ColorStateList.valueOf(color), tintMode);
    }

    public static Drawable tint(Drawable originDrawable, ColorStateList colorStateList, PorterDuff.Mode tintMode) {
        Drawable tintDrawable = DrawableCompat.wrap(originDrawable);
        if (tintMode != null) {
            DrawableCompat.setTintMode(tintDrawable, tintMode);
        }
        DrawableCompat.setTintList(tintDrawable, colorStateList);
        return tintDrawable;
    }

}
