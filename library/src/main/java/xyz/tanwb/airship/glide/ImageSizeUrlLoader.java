package xyz.tanwb.airship.glide;

import android.content.Context;

import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

import java.io.InputStream;

public class ImageSizeUrlLoader extends BaseGlideUrlLoader<ImageSizeUrl> {

    public ImageSizeUrlLoader(Context context) {
        super(context);
    }

    @Override
    protected String getUrl(ImageSizeUrl model, int width, int height) {
        return model.requestCustomSizeUrl(width, height);
    }

    public static class Factory implements ModelLoaderFactory<ImageSizeUrl, InputStream> {

        @Override
        public ModelLoader<ImageSizeUrl, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new ImageSizeUrlLoader(context);
        }

        @Override
        public void teardown() {
        }
    }

}
