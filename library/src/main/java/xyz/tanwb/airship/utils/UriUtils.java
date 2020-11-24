package xyz.tanwb.airship.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.BaseConstants;

public class UriUtils {

    /**
     * 获取图片的Uri（适配Android7.0）
     * <provider
     * android:name="android.support.v4.content.FileProvider"
     * android:authorities="${applicationId}.provider"
     * android:exported="false"
     * android:grantUriPermissions="true">
     * <meta-data
     * android:name="android.support.FILE_PROVIDER_PATHS"
     * android:resource="@xml/provider_paths" />
     * </provider>
     */
    public static Uri getImageContentUri(Context mContext, File imageFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ", new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        } else {
            return Uri.fromFile(imageFile);
        }
    }

    /**
     * 判断当前Url是否标准的content://样式，如果不是，则返回绝对路径
     */
    public static String getAbsolutePathFromNoStandardUri(Uri uri) {
        String mUriString = Uri.decode(uri.toString());
        String pre1 = "file://sdcard/";
        String pre2 = "file://mnt/sdcard/";
        return mUriString.startsWith(pre1) ? FileUtils.getSDCardPath() + mUriString.substring(pre1.length())
                : (mUriString.startsWith(pre2) ? FileUtils.getSDCardPath() + mUriString.substring(pre2.length()) : null);
    }

    /**
     * 根据Uri获取文件路径
     */
    public static String getPhotoPathFromContentUri(Uri uri) {
        if (uri != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(App.app(), uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    String[] split = docId.split(BaseConstants.COLON);
                    if (split.length >= 2) {
                        String type = split[0];
                        if ("primary".equalsIgnoreCase(type)) {
                            return Environment.getExternalStorageDirectory() + BaseConstants.SLASH + split[1];
                        }
                    }
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    return getDataColumn(contentUri, null, null, null);
                } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String[] split = docId.split(BaseConstants.COLON);
                    if (split.length >= 2) {
                        String type = split[0];
                        Uri contentUris = null;
                        if ("image".equals(type)) {
                            contentUris = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            contentUris = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            contentUris = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }
                        String selection = MediaStore.Images.Media._ID + BaseConstants.EQUAL + BaseConstants.QUESTION_MARK;
                        String[] selectionArgs = new String[]{split[1]};
                        return getDataColumn(contentUris, selection, selectionArgs, null);
                    }
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            } else {
                return getDataColumn(uri, null, null, null);
            }
        }
        return null;
    }

    /**
     * 获取SD卡中最新图片路径
     */
    public static String getLatestImage() {
        return getDataColumn(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, MediaStore.Images.Media.DATE_ADDED + " desc");
    }

    /**
     * 从HandleResult返回的Data中获取图片路径
     */
    public static String getFilePathToHandleResult(Intent data) {
        if (data != null) {
            return getDataColumn(data.getData(), null, null, null);
        }
        return null;
    }

    /**
     * 根据条件查询Cursor,获取Data值
     */
    public static String getDataColumn(Uri uri, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            cursor = App.app().getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID}, selection, selectionArgs, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
