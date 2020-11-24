package xyz.tanwb.airship.glide;

import android.content.Context;
import androidx.annotation.IntDef;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Glide API
 * 图片路径说明:http://域名(IP)[:端口]/应用路径/image/图片路径/图片名称.jpg
 * 图片剪切:https://github.com/wasabeef/glide-transformations
 */
public class GlideManager {

    // 圆角图片
    public static final int IMAGE_TYPE_ROUND = 1;
    // 圆形图片
    public static final int IMAGE_TYPE_CIRCLE = 2;
    // 模糊图片
    public static final int IMAGE_TYPE_BLUR = 3;
    // 缩放图像让它填充到 ImageView 界限内并且裁剪额外的部分
    public static final int IMAGE_TYPE_CENTERCROP = 4;
    // 缩放图像让图像都测量出来等于或小于 ImageView 的边界范围
    public static final int IMAGE_TYPE_FITCENTER = 5;

    @IntDef({IMAGE_TYPE_ROUND, IMAGE_TYPE_CIRCLE, IMAGE_TYPE_BLUR, IMAGE_TYPE_CENTERCROP, IMAGE_TYPE_FITCENTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TransformType {
    }

    private Context context;
    private DrawableRequestBuilder requestBuilder;

    GlideManager(Context context, String string) {
        this.context = context;
        requestBuilder = Glide.with(context).load(string);
    }

    GlideManager(Context context, File file) {
        this.context = context;
        requestBuilder = Glide.with(context).load(file);
    }

    GlideManager(Context context, ImageSizeUrl imageSizeUrl) {
        this.context = context;
        requestBuilder = Glide.with(context).using(new ImageSizeUrlLoader(context)).load(imageSizeUrl);
    }

    public static GlideManager load(Context context, String string) {
        return new GlideManager(context, string);
    }

    public static GlideManager load(Context context, File file) {
        return new GlideManager(context, file);
    }

    public static GlideManager load(Context context, ImageSizeUrl imageSizeUrl) {
        return new GlideManager(context, imageSizeUrl);
    }

    /**
     * 设置图片转换
     */
    public GlideManager setTransform(@TransformType int transform) {
        switch (transform) {
            case IMAGE_TYPE_CIRCLE:
                requestBuilder.transform(new TransformToCircle(context));
                break;
            case IMAGE_TYPE_ROUND:
                requestBuilder.transform(new TransformToRound(context));
                break;
            case IMAGE_TYPE_BLUR:
                requestBuilder.transform(new TransformToBlur(context));
                break;
            case IMAGE_TYPE_CENTERCROP:
                requestBuilder.centerCrop();
                break;
            case IMAGE_TYPE_FITCENTER:
                requestBuilder.fitCenter();
                break;
            default:
                break;
        }
        return this;
    }

    /**
     * 设置加载占位符
     */
    public GlideManager placeholder(int resourceId) {
        requestBuilder.placeholder(resourceId);
        return this;
    }

    /**
     * 设置加载错误图
     */
    public GlideManager error(int resourceId) {
        requestBuilder.error(resourceId);
        return this;
    }

    /**
     * 调整图像的尺寸（像素）
     *
     * @param width  宽度
     * @param height 高度
     */
    public GlideManager override(int width, int height) {
        requestBuilder.override(width, height);
        return this;
    }

    /**
     * 设置不启用加载动画
     */
    public GlideManager dontAnimate() {
        requestBuilder.dontAnimate();
        return this;
    }

    /**
     * 设置图片加载动画
     */
    public GlideManager crossFade() {
        requestBuilder.crossFade();
        return this;
    }

    /**
     * 设置图片加载动画
     */
    public GlideManager crossFade(int duration) {
        requestBuilder.crossFade(duration);
        return this;
    }

    /**
     * 设置图片加载动画
     */
    public GlideManager crossFade(int animationId, int duration) {
        requestBuilder.crossFade(animationId, duration);
        return this;
    }

    /**
     * 设置图片加载动画
     * <p>
     * new ViewPropertyAnimation.Animator() {
     *
     * @Override public void animate(View view) {
     * // if it's a custom view class, cast it here then find subviews and do the animations here, we just use the entire view for the fade animation
     * view.setAlpha(0f);
     * ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
     * fadeAnim.setDuration(2500);
     * fadeAnim.start();
     * }
     * }
     * </p>
     */
    private GlideManager setAnimate(ViewPropertyAnimation.Animator animate) {
        requestBuilder.animate(animate);
        return this;
    }

    /**
     * 设置缓存
     * DiskCacheStrategy.NONE 什么都不缓存
     * DiskCacheStrategy.SOURCE 仅仅只缓存原来的全分辨率的图像
     * DiskCacheStrategy.RESULT 仅仅缓存最终的图像.即降低分辨率后的（或者是转换后的）
     * DiskCacheStrategy.ALL 缓存所有版本的图像（默认行为）
     */
    public GlideManager setCache(boolean isMemory, DiskCacheStrategy strategy) {
        //跳过内存缓存 默认false
        requestBuilder.skipMemoryCache(isMemory);
        //设置磁盘缓存类型
        requestBuilder.diskCacheStrategy(strategy);
        return this;
    }

    /**
     * 设置请求优先级
     * Priority.LOW 低
     * Priority.NORMAL 正常
     * Priority.HIGH 高
     * Priority.IMMEDIATE 立即
     */
    public GlideManager setPriority(Priority priority) {
        requestBuilder.priority(priority);
        return this;
    }

    /**
     * 设置缩略图
     *
     * @param sizeMultiplier 大小的倍数
     */
    public GlideManager setThumbnail(float sizeMultiplier) {
        requestBuilder.thumbnail(sizeMultiplier);
        return this;
    }

    /**
     * 设置缩略图
     */
    public GlideManager setThumbnail(DrawableRequestBuilder<?> thumbnailRequest) {
        requestBuilder.thumbnail(thumbnailRequest);
        return this;
    }

    /**
     * 设置监听,调试和错误处理.
     * <p>
     * 获得 Glide 的调试日志
     * adb shell setprop log.tag.GenericRequest DEBUG
     * debug 的优先级 VERBOSE DEBUG INFO WARN ERROR
     * </p>
     * onException() 若需显示一个错误的占位符等情况的话,需返回false.
     */
    private GlideManager setListener(RequestListener requestListener) {
        requestBuilder.listener(requestListener);
        return this;
    }

    /**
     * 设置Target
     */
    public Target intoTargets(ImageView view) {
        return requestBuilder.into(new DrawableImageViewTarget(view) {

        });
    }

    /**
     * 设置显示的ImageView
     */
    public Target into(ImageView view) {
        return requestBuilder.into(view);
    }

    /**
     * 设置显示的Target
     */
    public Target into(Target target) {
        return requestBuilder.into(target);
    }
}
