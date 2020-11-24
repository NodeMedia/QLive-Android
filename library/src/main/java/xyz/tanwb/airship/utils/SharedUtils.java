package xyz.tanwb.airship.utils;

import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.BaseConstants;

public class SharedUtils {

    private static SharedPreferences getSharedPreferences() {
        return App.app().getSharedPreferences(SharedUtils.class.getSimpleName(), 0);
    }

    private static SharedPreferences.Editor getEdit() {
        return getSharedPreferences().edit();
    }

    public static void put(String key, Object object) {
        SharedPreferences.Editor editor = getEdit();
        if (object instanceof String) {
            editor.putString(key, object.toString());
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, JSON.toJSONString(object));
        }
        editor.commit();
    }

    public static String getString(String key) {
        return getString(key, BaseConstants.NULL);
    }

    public static String getString(String key, String defaultObject) {
        return getSharedPreferences().getString(key, defaultObject);
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defaultObject) {
        return getSharedPreferences().getInt(key, defaultObject);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultObject) {
        return getSharedPreferences().getBoolean(key, defaultObject);
    }

    public static float getFloat(String key) {
        return getFloat(key, 0F);
    }

    public static float getFloat(String key, float defaultObject) {
        return getSharedPreferences().getFloat(key, defaultObject);
    }

    public static long getLong(String key) {
        return getLong(key, 0L);
    }

    public static long getLong(String key, long defaultObject) {
        return getSharedPreferences().getLong(key, defaultObject);
    }

    public static <T extends Object> T getSerialize(String key, Class<T> cls) {
        String jsonString = getString(key, null);
        if (jsonString != null) {
            return JSON.parseObject(jsonString, cls);
        } else {
            return null;
        }
    }

    public static Map<String, ?> getAll() {
        return getSharedPreferences().getAll();
    }

    public static boolean contains(String key) {
        return getSharedPreferences().contains(key);
    }

    public static void remove(String key) {
        SharedPreferences.Editor editor = getEdit();
        editor.remove(key);
        editor.commit();
    }

    public static void clear() {
        SharedPreferences.Editor editor = getEdit();
        editor.clear();
        editor.commit();
    }
}
