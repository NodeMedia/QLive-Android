package xyz.tanwb.airship.imgsel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.fragment.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.Serializable;

public class ImgSelConfig implements Serializable {

    /**
     * 最多选择图片数,大于1表示可多选
     */
    public int maxNum = 9;

    /**
     * 第一个item是否显示相机
     */
    public boolean needCamera;

    /**
     * 是否需要裁剪
     */
    public boolean needCrop;

    /**
     * 状态栏颜色
     */
    public int statusBarColor = -1;

    /**
     * 状态栏文字黑色
     */
    public boolean statusBarTextToBlack;

    /**
     * 返回键图标资源
     */
    public int backResId = -1;

    /**
     * 标题
     */
    public String title;

    /**
     * 标题颜色
     */
    public int titleColor;

    /**
     * titlebar背景色
     */
    public int titleBgColor;

    /**
     * 确定按钮文字颜色
     */
    public int btnTextColor;

    /**
     * 确定按钮背景色
     */
    public int btnBgColor;

    /**
     * 图片列数
     */
    public int spanCount;

    /**
     * 自定义图片加载器
     */
    public ImageLoader loader;

    /**
     * 裁剪输出大小
     */
    public int aspectX = 1;
    public int aspectY = 1;
    public int outputX = 500;
    public int outputY = 500;

    public ImgSelConfig(Builder builder) {
        this.needCrop = builder.needCrop;
        this.maxNum = builder.maxNum;
        this.needCamera = builder.needCamera;
        this.statusBarColor = builder.statusBarColor;
        this.statusBarTextToBlack = builder.statusBarTextToBlack;
        this.backResId = builder.backResId;
        this.title = builder.title;
        this.titleBgColor = builder.titleBgColor;
        this.titleColor = builder.titleColor;
        this.btnBgColor = builder.btnBgColor;
        this.btnTextColor = builder.btnTextColor;
        this.spanCount = builder.spanCount;
        this.loader = builder.loader;
        this.aspectX = builder.aspectX;
        this.aspectY = builder.aspectY;
        this.outputX = builder.outputX;
        this.outputY = builder.outputY;
    }

    public static class Builder implements Serializable {

        private boolean needCrop;
        private int aspectX;
        private int aspectY;
        private int outputX;
        private int outputY;
        private int maxNum;
        private boolean needCamera;
        private int statusBarColor;
        private boolean statusBarTextToBlack;
        private int backResId;
        private String title;
        private int titleColor;
        private int titleBgColor;
        private int btnTextColor;
        private int btnBgColor;
        private int spanCount;
        private ImageLoader loader;

        public Builder() {
            this(new ImageLoader() {
                @Override
                public void displayImage(Context context, String path, ImageView imageView) {
                    Glide.with(context).load(new File(path)).centerCrop().thumbnail(0.1F).into(imageView);
                }
            });
        }

        public Builder(ImageLoader loader) {
            this.loader = loader;
            spanCount = 4;
            btnBgColor = Color.TRANSPARENT;
            btnTextColor = Color.TRANSPARENT;
            titleBgColor = Color.TRANSPARENT;
            titleColor = Color.TRANSPARENT;
            title = "图片";
            backResId = -1;
            statusBarColor = -1;
            statusBarTextToBlack = false;
            needCamera = true;
            maxNum = 9;
            outputY = 400;
            outputX = 400;
            aspectY = 1;
            aspectX = 1;
            needCrop = false;
        }

        public Builder needCrop(boolean needCrop) {
            this.needCrop = needCrop;
            return this;
        }

        public Builder maxNum(int maxNum) {
            if (maxNum < 1) {
                maxNum = 1;
            }
            this.maxNum = maxNum;
            return this;
        }

        public Builder needCamera(boolean needCamera) {
            this.needCamera = needCamera;
            return this;
        }

        public Builder statusBarColor(int statusBarColor) {
            this.statusBarColor = statusBarColor;
            return this;
        }

        public Builder statusBarTextToBlack(boolean statusBarTextToBlack) {
            this.statusBarTextToBlack = statusBarTextToBlack;
            return this;
        }

        public Builder backResId(int backResId) {
            this.backResId = backResId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder titleColor(int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder titleBgColor(int titleBgColor) {
            this.titleBgColor = titleBgColor;
            return this;
        }

        public Builder btnTextColor(int btnTextColor) {
            this.btnTextColor = btnTextColor;
            return this;
        }

        public Builder btnBgColor(int btnBgColor) {
            this.btnBgColor = btnBgColor;
            return this;
        }

        public Builder spanCount(int spanCount) {
            this.spanCount = spanCount;
            return this;
        }

        public Builder cropSize(int aspectX, int aspectY, int outputX, int outputY) {
            this.aspectX = aspectX;
            this.aspectY = aspectY;
            this.outputX = outputX;
            this.outputY = outputY;
            return this;
        }

        public void build(Activity activity, int requestCode) {
            Intent intent = new Intent(activity, ImgSelActivity.class);
            ImgSelActivity.config = new ImgSelConfig(this);
            activity.startActivityForResult(intent, requestCode);
        }

        public void build(Fragment fragment, int requestCode) {
            Intent intent = new Intent(fragment.getActivity(), ImgSelActivity.class);
            ImgSelActivity.config = new ImgSelConfig(this);
            fragment.startActivityForResult(intent, requestCode);
        }

    }
}
