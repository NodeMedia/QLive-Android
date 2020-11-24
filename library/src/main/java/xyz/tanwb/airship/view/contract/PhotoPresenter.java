package xyz.tanwb.airship.view.contract;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;

import xyz.tanwb.airship.utils.FileUtils;
import xyz.tanwb.airship.utils.ToastUtils;
import xyz.tanwb.airship.utils.UriUtils;
import xyz.tanwb.airship.view.BaseView;

public abstract class PhotoPresenter<T extends BaseView> extends PermissionsPresenter<T> {

    public static final int REQUEST_CODE_IMAGE = 9001;//相册图片
    public static final int REQUEST_CODE_CAMERA = 9002;//拍照图片
    public static final int REQUEST_CODE_PHOTOCUT = 9003;//剪切图片

    private static final String IMAGETYPE = "image/*";

    protected File photoFile;

    private int actionType;//操作类型 0:拍照 1:选择相册图片
    private boolean isPhotoCut;

    private int aspectX = 1;
    private int aspectY = 1;
    private float outputX = 256;
    private float outputY = 256;

    public void startAction(int actionType) {
        startAction(actionType, true);
    }

    public void startAction(int actionType, boolean isPhotoCut) {
        this.actionType = actionType;
        this.isPhotoCut = isPhotoCut;
        questPermissions();
    }

    public void setPhotoCut(boolean photoCut) {
        this.isPhotoCut = photoCut;
    }

    public void setCutRect(int aspectX, int aspectY, float outputX, float outputY) {
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        this.outputX = outputX;
        this.outputY = outputY;
    }

    public void questPermissions() {
        questPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    public void onPermissionsSuccess(String[] permissions) {
        if (actionType == 1) {
            openImageChoice();
        } else {
            openSystemCamera();
        }
    }

    @Override
    public void onPermissionsFailure(String strMsg) {
        super.onPermissionsFailure(strMsg);
        ToastUtils.show(mContext, "请开启拍照和SD卡读写权限后重试!");
    }

    protected void openImageChoice() {
        Intent intentFromGallery = new Intent();
        intentFromGallery.setType(IMAGETYPE); // 设置文件类型
        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        mActivity.startActivityForResult(intentFromGallery, REQUEST_CODE_IMAGE);
    }

    protected void openSystemCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            photoFile = getSysDCIM();
            if (photoFile != null) {
                // Uri photoURI = FileProvider.getUriForFile(mContext, App.getPackageName() + ".provider", photoFile);
                // Uri photoURI = UriUtils.getImageContentUri(mContext, photoFile);
                Uri photoURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, photoFile.getAbsolutePath());
                    photoURI = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                } else {
                    photoURI = Uri.fromFile(photoFile);
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
            mActivity.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
        } else {
            ToastUtils.show(mContext, "未检索到系统照相机程序.");
        }
    }

    protected File openPhotoCut(File file) {
        if (file != null) {
            Intent intent = new Intent("com.android.camera.action.CROP");
            // Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            // intent.setDataAndType(Uri.fromFile(new File(imagePath)), IMAGETYPE);
            intent.setDataAndType(UriUtils.getImageContentUri(mContext, file), IMAGETYPE);
            // 设置裁剪
            intent.putExtra("crop", "true");
            if (outputX > 0 && outputY > 0) {
                // aspectX aspectY 是宽高的比例
                intent.putExtra("aspectX", aspectX);
                intent.putExtra("aspectY", aspectY);
                // outputX outputY 是裁剪图片宽高
                intent.putExtra("outputX", outputX);
                intent.putExtra("outputY", outputY);
            }
            intent.putExtra("scale", true);// 去黑边
            /**
             * 此方法返回的图片只能是小图片（sumsang测试为高宽160px的图片）
             * 故将图片保存在Uri中，调用时将Uri转换为Bitmap，此方法还可解决miui系统不能return data的问题
             */
            //intent.putExtra("return-data", true);
            File uritempFile = getSysDCIM();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(uritempFile));
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true); //没有人脸检测

            mActivity.startActivityForResult(intent, REQUEST_CODE_PHOTOCUT);

            return uritempFile;
        } else {
            ToastUtils.show(mContext, "未获取到图片信息.");
        }
        return null;
    }

    protected File getSysDCIM() {
        // ContentValues values = new ContentValues();
        // values.put(MediaStore.Images.Media.TITLE, "cm_" + System.currentTimeMillis());
        // photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return new File(FileUtils.getAppSdPath(FileUtils.PATH_CACHE) + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg");
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_IMAGE:
                    onRequest(data);
                    break;
                case REQUEST_CODE_CAMERA:
                    onRequest(data);
                    break;
                case REQUEST_CODE_PHOTOCUT:
                    onPhotoSuccess(actionType, photoFile);
                    break;
            }
        }
    }

    protected void onRequest(Intent data) {
        if (data != null && data.getData() != null) {
            photoFile = new File(UriUtils.getPhotoPathFromContentUri(data.getData()));
        }
        if (isPhotoCut) {
            photoFile = openPhotoCut(photoFile);
        } else {
            onPhotoSuccess(actionType, photoFile);
        }
    }

    public abstract void onPhotoSuccess(int photoType, File photoFile);
}
