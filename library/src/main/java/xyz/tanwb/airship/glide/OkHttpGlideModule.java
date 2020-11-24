package xyz.tanwb.airship.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;

import xyz.tanwb.airship.okhttp.OkHttpManager;

/**
 * A {@link GlideModule} implementation to replace Glide's default
 * {@link java.net.HttpURLConnection} based {@link com.bumptech.glide.load.model.ModelLoader}
 * with an OkHttp based {@link com.bumptech.glide.load.model.ModelLoader}.
 * <p/>
 * <p> If you're using gradle, you can include this module simply by depending on the aar, the
 * module will be merged in by manifest merger. For other build systems or for more more
 * information, see {@link GlideModule}. </p>
 * <li>
 * <meta-data
 * android:name="xyz.tanwb.airship.glide.OkHttpGlideModule"
 * android:value="GlideModule" />
 * </li>
 */
public class OkHttpGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

        // ViewTarget.setTagId(R.id.glide_tag);//全局设置ViewTaget的tagId

        // builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);// 改变图片质量.默认 PREFER_RGB_565

        // 设置内存缓存.（从默认内存管理中获取后改变）
        // MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        // builder.setMemoryCache(new LruResourceCache((int) (1.2 * calculator.getMemoryCacheSize())));
        // builder.setBitmapPool(new LruBitmapPool((int) (1.2 * calculator.getBitmapPoolSize())));

        // 定义磁盘缓冲的大小为100M
        // int cacheSize100MegaBytes = 104857600;
        // builder.setDiskCache(new InternalCacheDiskCacheFactory(context, cacheSize100MegaBytes));// 设置磁盘缓存到应用的内部目录 默认
        // builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, cacheSize100MegaBytes));// 设置磁盘缓存到外部存储
        // builder.setDiskCache(new DiskLruCacheFactory(context.getCacheDir().getPath(), cacheSize100MegaBytes));// 设置磁盘缓存到指定的目录

        // setDiskCacheService(ExecutorService service) // 自定义缓存实现
        // setResizeService(ExecutorService service)
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(OkHttpManager.getInstance().getOkHttpClient()));
    }
}
