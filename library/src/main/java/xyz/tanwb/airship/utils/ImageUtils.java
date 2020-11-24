package xyz.tanwb.airship.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.R;

public class ImageUtils {

    /**
     * 得到默认的图片文件名
     */
    public static String getDefName() {
        return "IMG_" + System.currentTimeMillis() + ".jpg";
    }

    /**
     * 保存Image至/data/data/packagename/cache目录
     *
     * @param bitmap Bitmap
     * @return 图片路径
     */
    public static String saveImageToCache(Bitmap bitmap) {
        return saveImageToCache(getDefName(), bitmap);
    }

    /**
     * 保存Image至/data/data/packagename/cache目录
     *
     * @param imageName 指定文件名称
     * @param bitmap    Bitmap
     * @return 图片路径
     */
    public static String saveImageToCache(String imageName, Bitmap bitmap) {
        return saveImageToCache(imageName, bitmap, 75);
    }

    /**
     * 保存Image至/data/data/packagename/cache目录
     *
     * @param imageName 指定文件名称
     * @param bitmap    Bitmap
     * @param quality   图片质量
     * @return 图片路径
     */
    public static String saveImageToCache(String imageName, Bitmap bitmap, int quality) {
        if (!TextUtils.isEmpty(imageName) && bitmap != null) {
            String imagePath = FileUtils.getCachePath() + File.separator + imageName;
            return saveImage(imagePath, bitmap, quality);
        }
        return null;
    }

    /**
     * 保存Image至APP SD卡目录
     *
     * @param bitmap Bitmap
     * @return 图片路径
     */
    public static String saveImage(Bitmap bitmap) {
        String imagePath = FileUtils.getAppSdPath(FileUtils.PATH_CACHE) + File.separator + getDefName();
        return saveImage(imagePath, bitmap);
    }

    /**
     * 保存Image(默认图片质量为75)
     *
     * @param imagePath 指定文件保存路径
     * @param bitmap    Bitmap
     * @return 图片路径
     */
    public static String saveImage(String imagePath, Bitmap bitmap) {
        return saveImage(imagePath, bitmap, 75);
    }

    /**
     * 保存Image
     *
     * @param imagePath 指定文件保存路径
     * @param bitmap    Bitmap
     * @param quality   图片质量
     * @return 图片路径
     */
    public static String saveImage(String imagePath, Bitmap bitmap, int quality) {
        try {
            if (imagePath != null && bitmap != null) {
                FileOutputStream e = new FileOutputStream(imagePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
                byte[] bytes = stream.toByteArray();
                e.write(bytes);
                e.close();
                return imagePath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 让Gallery上能马上看到该图片
     */
    private static void scanPhoto(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(imagePath));
        mediaScanIntent.setData(contentUri);
        App.app().sendBroadcast(mediaScanIntent);
    }

    /**
     * 根据资源ID从资源文件夹下面获取图片(更节省内存)
     *
     * @param resId 资源ID
     */
    public static Bitmap getBitmapByRes(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return getBitmapByRes(resId, options);
    }

    /**
     * 根据资源ID从资源文件夹下面获取图片(更节省内存)
     *
     * @param resId   资源ID
     * @param options 属性设置
     */
    public static Bitmap getBitmapByRes(int resId, BitmapFactory.Options options) {
        if (resId > 0) {
            return BitmapFactory.decodeResource(App.app().getResources(), resId, options);
        }
        return null;
    }

    /**
     * 从图片文件路径下获取图片
     *
     * @param imagePath 图片文件路径
     */
    public static Bitmap getBitmapByPath(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return getBitmapByPath(imagePath, options);
    }

    /**
     * 从图片文件路径下获取图片
     *
     * @param imagePath 图片文件路径
     * @param options   属性设置
     */
    public static Bitmap getBitmapByPath(String imagePath, BitmapFactory.Options options) {
        return BitmapFactory.decodeFile(imagePath, options);
    }

    /**
     * 从Uri获取图片
     *
     * @param uri 图片Uri路径
     */
    public static Bitmap getBitmapByUri(Uri uri) {
        try {
            return BitmapFactory.decodeStream(App.app().getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据资源ID从资源文件夹下面获取图片大小
     *
     * @param resId 资源ID
     */
    public static int[] getImageRect(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(App.app().getResources(), resId, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    /**
     * 从图片文件路径下获取图片大小
     *
     * @param imagePath 图片路径
     */
    public static int[] getImageRect(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//设置bitmap不加载到内存
        BitmapFactory.decodeFile(imagePath, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    /**
     * 将drawable转化为bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        return bitmapDrawable.getBitmap();
    }

    /**
     * 将bitmap转化为drawable
     */
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(App.app().getResources(), bitmap);
    }

    /**
     * 获取图片的旋转角度
     *
     * @param imagePath 图片路径
     */
    public static int getBitmapRotateAngle(String imagePath) {
        int angle = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return angle;
    }

    /**
     * 旋转Bitmap(默认回收原始Bitmap)
     *
     * @param originBitmap 原始Bitmap
     * @param angle        旋转角度
     */
    public static Bitmap getRotateBitmap(Bitmap originBitmap, int angle) {
        return getRotateBitmap(originBitmap, angle, true);
    }

    /**
     * 旋转Bitmap
     *
     * @param originBitmap 原始Bitmap
     * @param angle        旋转角度
     * @param recycle      是否回收原始Bitmap
     */
    public static Bitmap getRotateBitmap(Bitmap originBitmap, int angle, boolean recycle) {
        if (originBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            Bitmap rotatedBitmap = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), originBitmap.getHeight(), matrix, true);
            if (recycle && !originBitmap.isRecycled()) {
                originBitmap.recycle();
            }
            return rotatedBitmap;
        }
        return null;
    }

    /**
     * 缩放Bitmap - 按缩放倍数(默认回收原始Bitmap)
     *
     * @param originBitmap 原始Bitmap
     * @param scaleX       横向比例
     * @param scaleY       纵向比例
     */
    public static Bitmap getZoomBitmap(Bitmap originBitmap, float scaleX, float scaleY) {
        return getZoomBitmap(originBitmap, scaleX, scaleY, true);
    }

    /**
     * 缩放Bitmap - 按缩放倍数
     *
     * @param originBitmap 原始Bitmap
     * @param scaleX       横向比例
     * @param scaleY       纵向比例
     * @param recycle      是否回收原始Bitmap
     */
    public static Bitmap getZoomBitmap(Bitmap originBitmap, float scaleX, float scaleY, boolean recycle) {
        if (originBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postScale(scaleX, scaleY);
            Bitmap scaledBitmap = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), originBitmap.getHeight(), matrix, true);
            if (recycle && originBitmap != null && !originBitmap.isRecycled()) {
                originBitmap.recycle();
            }
            return scaledBitmap;
        }
        return null;
    }

    /**
     * 缩放Bitmap - 缩放到目标大小(默认回收原始Bitmap)
     *
     * @param originBitmap 原始Bitmap
     * @param dstWidth     目标宽度
     * @param dstHeight    目标高度
     */
    public static Bitmap getZoomBitmap(Bitmap originBitmap, int dstWidth, int dstHeight) {
        return getZoomBitmap(originBitmap, dstWidth, dstHeight, true);
    }

    /**
     * 缩放Bitmap - 缩放到目标大小
     *
     * @param originBitmap 原始Bitmap
     * @param dstWidth     目标宽度
     * @param dstHeight    目标高度
     * @param recycle      是否回收原始Bitmap
     */
    public static Bitmap getZoomBitmap(Bitmap originBitmap, int dstWidth, int dstHeight, boolean recycle) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originBitmap, dstWidth, dstHeight, true);
        if (recycle && originBitmap != null && !originBitmap.isRecycled()) {
            originBitmap.recycle();
        }
        return scaledBitmap;
    }

    /**
     * 根据reqWidth, reqHeight计算最合适的inSampleSize
     *
     * @param imagePath 图片路径
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     */
    public static int[] getScaleImageSize(String imagePath, int maxWidth, int maxHeight) {
        int[] imageRect = getImageRect(imagePath);
        return getScaleImageSize(imageRect, new int[]{maxWidth, maxHeight}, true, true);
    }

    /**
     * 获缩放大小
     *
     * @param imageRect
     * @param squareRect
     * @param enlarge
     * @param narrow
     */
    public static int[] getScaleImageSize(int[] imageRect, int[] squareRect, boolean enlarge, boolean narrow) {
        if (imageRect != null && squareRect != null) {
            if (imageRect[0] == squareRect[0] && imageRect[1] == squareRect[1]) {
                return imageRect;
            } else if (!enlarge && imageRect[0] <= squareRect[0] && imageRect[1] <= squareRect[1]) {
                return imageRect;
            } else if (!narrow && imageRect[0] >= squareRect[0] && imageRect[1] >= squareRect[1]) {
                return imageRect;
            } else {
                double scaleWidth = (double) squareRect[0] / (double) imageRect[0];
                double scaleHeight = (double) squareRect[1] / (double) imageRect[1];
                return scaleWidth > scaleHeight ? new int[]{(int) ((double) imageRect[0] * scaleHeight), (int) ((double) imageRect[1] * scaleHeight)} : new int[]{(int) ((double) imageRect[0] * scaleWidth), (int) ((double) imageRect[1] * scaleWidth)};
            }
        } else {
            return null;
        }
    }

    /**
     * 获得带倒影的图片(默认回收原始Bitmap)
     *
     * @param originBitmap 原始Bitmap
     */
    public static Bitmap getReflectionImageWithOrigin(Bitmap originBitmap) {
        return getReflectionImageWithOrigin(originBitmap, true);
    }

    /**
     * 获得带倒影的图片
     *
     * @param originBitmap 原始Bitmap
     * @param recycle      是否回收原始Bitmap
     */
    public static Bitmap getReflectionImageWithOrigin(Bitmap originBitmap, boolean recycle) {
        if (originBitmap == null) {
            return null;
        }
        int reflectionGap = 4;
        int width = originBitmap.getWidth();
        int height = originBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(1F, -1F);
        Bitmap reflectionImage = Bitmap.createBitmap(originBitmap, 0, height / 2, width, height / 2, matrix, false);
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height + height / 2, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(originBitmap, 0, 0, null);
        Paint deafalutPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, originBitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

        if (recycle) {
            originBitmap.recycle();
            reflectionImage.recycle();
        }
        return bitmapWithReflection;
    }

    /**
     * 裁剪正方形Bitmap
     *
     * @param originBitmap 原始Bitmap
     */
    public static Bitmap getCropImage(Bitmap originBitmap) {
        int imageRect = Math.min(originBitmap.getWidth(), originBitmap.getHeight());
        return getCropImage(originBitmap, imageRect, imageRect);
    }

    /**
     * 裁剪Bitmap
     *
     * @param originBitmap 原始Bitmap
     * @param cropWidth    裁剪宽度
     * @param cropHeight   裁剪高度
     */
    public static Bitmap getCropImage(Bitmap originBitmap, int cropWidth, int cropHeight) {
        if (originBitmap != null && originBitmap.getWidth() >= cropWidth && originBitmap.getHeight() >= cropHeight) {
            int retX = (originBitmap.getWidth() - cropWidth) / 2;
            int retY = (originBitmap.getHeight() - cropHeight) / 2;
            return Bitmap.createBitmap(originBitmap, retX, retY, cropWidth, cropHeight, null, false);
        } else {
            return originBitmap;
        }
    }

    /**
     * 圆形Bitmap：居中裁剪(默认回收原始Bitmap)
     *
     * @param originBitmap 原始Bitmap
     */
    public static Bitmap getCircleBitmap(Bitmap originBitmap) {
        return getCircleBitmap(originBitmap, true);
    }

    /**
     * 圆形Bitmap：居中裁剪
     *
     * @param originBitmap 原始Bitmap
     * @param recycle      是否回收Bitmap
     */
    public static Bitmap getCircleBitmap(Bitmap originBitmap, boolean recycle) {
        if (originBitmap == null) {
            return null;
        }
        int minLength = Math.min(originBitmap.getWidth(), originBitmap.getHeight());

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        // paint.setColor(Color.WHITE);

        Bitmap circleBitmap = Bitmap.createBitmap(minLength, minLength, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(circleBitmap);
        // canvas.drawARGB(0, 0, 0, 0);
        float roundPx = (float) (minLength / 2);
        canvas.drawCircle(roundPx, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 居中显示
        int left = -(originBitmap.getWidth() - minLength) / 2;
        int top = -(originBitmap.getHeight() - minLength) / 2;
        canvas.drawBitmap(originBitmap, left, top, paint);

        // 是否回收原始Bitmap
        if (recycle && !originBitmap.isRecycled()) {
            originBitmap.recycle();
        }

        return circleBitmap;
    }

    /**
     * 圆角矩形Bitmap(默认半径为10)
     *
     * @param originBitmap 原始Bitmap
     */
    public static Bitmap getFilletBitmap(Bitmap originBitmap) {
        return getFilletBitmap(originBitmap, App.app().getResources().getDimension(R.dimen.dp_3));
    }

    /**
     * 圆角矩形Bitmap(默认回收原始Bitmap)
     *
     * @param originBitmap 原始Bitmap
     * @param radius       圆角半径
     */
    public static Bitmap getFilletBitmap(Bitmap originBitmap, float radius) {
        return getFilletBitmap(originBitmap, radius, true, true);
    }

    /**
     * 圆角Bitmap
     *
     * @param originBitmap 原始Bitmap
     * @param radius       圆角半径
     * @param square       是否剪切为正方形
     * @param recycle      是否回收Bitmap
     */
    public static Bitmap getFilletBitmap(Bitmap originBitmap, float radius, boolean square, boolean recycle) {
        if (originBitmap == null) {
            return null;
        }
        int width = originBitmap.getWidth();
        int height = originBitmap.getHeight();

        if (width != height && square) {
            originBitmap = getCropImage(originBitmap);
            return getFilletBitmap(originBitmap, radius, square, recycle);
        }

        // 准备画笔
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        // paint.setColor(Color.WHITE);

        // 准备裁剪的矩阵
        Bitmap roundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(roundBitmap);
        // canvas.drawARGB(0, 0, 0, 0);
        Rect rect = new Rect(0, 0, width, height);
        canvas.drawRoundRect(new RectF(new Rect(0, 0, width, height)), radius, radius, paint);

        // 关于Xfermode和SRC_IN请自行查阅 http://www.cnblogs.com/jacktu/archive/2012/01/02/2310326.html
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(originBitmap, rect, rect, paint);

        // Bitmap rounded = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // canvas = new Canvas(rounded);
        // canvas.drawBitmap(originBitmap, 0, 0, null);
        // canvas.drawBitmap(roundBitmap, 0, 0, paint);

        // 是否回收原始Bitmap
        if (recycle && !originBitmap.isRecycled()) {
            originBitmap.recycle();
        }

        return roundBitmap;
    }

    /**
     * 灰阶效果(黑白图)(默认回收原始Bitmap)
     *
     * @param originBitmap 原始Bitmap
     */
    public static Bitmap getGrayBitmap(Bitmap originBitmap) {
        return getGrayBitmap(originBitmap, true);
    }

    /**
     * 灰阶效果(黑白图)
     *
     * @param originBitmap 原始Bitmap
     * @param recycle      是否回收Bitmap
     */
    public static Bitmap getGrayBitmap(Bitmap originBitmap, boolean recycle) {
        if (originBitmap == null) {
            return null;
        }
        Bitmap grayBitmap = Bitmap.createBitmap(originBitmap.getWidth(), originBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(grayBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixColorFilter);
        canvas.drawBitmap(originBitmap, 0, 0, paint);
        if (recycle && !originBitmap.isRecycled()) {
            originBitmap.recycle();
        }
        return grayBitmap;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     */
    public static Bitmap getScreenSnapShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        if (bmp == null) {
            return null;
        }
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Bitmap bitmap = Bitmap.createBitmap(bmp, 0, statusBarHeight, bmp.getWidth(), bmp.getHeight() - statusBarHeight);
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }
}
