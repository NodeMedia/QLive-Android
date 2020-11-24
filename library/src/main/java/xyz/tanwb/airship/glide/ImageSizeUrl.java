package xyz.tanwb.airship.glide;

/**
 * 改变图片地址
 * Simple:
 * String baseImageUrl = "https://xxx/xxx/xxx.png";
 * ImageSizeUrl imageRequest = new ImageSizeUrl();
 * Glide.with(context).using(new ImageSizeUrlLoader(context)).load(imageRequest).into(imageView);
 */
public abstract class ImageSizeUrl {

    /**
     * 拼装请求地址 如：.append("-res-").append(width).append("-").append(height);
     *
     * @param width  图片宽度
     * @param height 图片高度
     */
    public abstract String requestCustomSizeUrl(int width, int height);

}
