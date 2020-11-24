package xyz.tanwb.airship.imgsel;

import java.util.ArrayList;
import java.util.List;

public class FolderInfo {

    public String name;
    public String path;
    public String cover;
    public boolean isSelecte;
    public List<ImageInfo> imageInfos = new ArrayList<>();

    public FolderInfo(String name) {
        this.name = name;
        this.isSelecte = true;
    }

    public FolderInfo(String path, String name, String cover) {
        this.path = path;
        this.name = name;
        this.cover = cover;
        this.isSelecte = false;
    }

    @Override
    public boolean equals(Object o) {
        try {
            FolderInfo other = (FolderInfo) o;
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