package xyz.tanwb.airship.imgsel;

import xyz.tanwb.airship.view.adapter.entity.MultiItemEntity;

public class ImageInfo extends MultiItemEntity {

    public String path;
    public String name;
    public long time;
    public boolean isSelecte;

    public ImageInfo() {
        this.itemType = 0;
    }

    public ImageInfo(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
        this.itemType = 1;
    }

    @Override
    public boolean equals(Object o) {
        try {
            ImageInfo other = (ImageInfo) o;
            return this.path.equalsIgnoreCase(other.path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}