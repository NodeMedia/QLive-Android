package xyz.tanwb.airship.utils;

import android.os.Environment;

import java.io.File;

import xyz.tanwb.airship.App;

/**
 * 清理缓冲
 */
public class CacheUtils {

    private static final String BASEPATH = "/data/data/";
    private static final String DBPATH = "/databases";
    private static final String SPPATH = "/shared_prefs";

    public static void cleanCache(String... filepath) {
        cleanCache();
        cleanFiles();
        cleanDatabases();
        cleanSharedPreference();
        cleanExternalCache();
        for (String filePath : filepath) {
            cleanCustomCache(filePath);
        }
    }

    public static void cleanCache() {
        deleteFilesByDirectory(App.app().getCacheDir());
    }

    public static String getCacheSize(String... filePath) {
        return null;
    }

    public static void cleanFiles() {
        deleteFilesByDirectory(App.app().getFilesDir());
    }

    public static void cleanDatabases() {
        deleteFilesByDirectory(new File(BASEPATH + App.getPackageName() + DBPATH));
    }

    public static void cleanSharedPreference() {
        deleteFilesByDirectory(new File(BASEPATH + App.getPackageName() + SPPATH));
    }

    public static void cleanExternalCache() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(App.app().getExternalCacheDir());
        }
    }

    public static void cleanCustomCache(String filePath) {
        deleteFilesByDirectory(new File(filePath));
    }

    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}
