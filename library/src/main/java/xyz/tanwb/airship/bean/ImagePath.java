package xyz.tanwb.airship.bean;

import java.io.Serializable;

public class ImagePath implements Serializable {

    /**
     * 图片Id
     */
    public long imageId;

    /**
     * 图片地址
     */
    public String imagePath;

    /**
     * 图片地址前缀
     */
    public String imagePrefix;

    /**
     * 图片地址后缀
     */
    public String imageSuffix;

    /**
     * 本地路径
     */
    public String imageLocalPath;

    /**
     * 是否为空图片
     */
    public boolean imageEmpty;

    /**
     * 宽度
     */
    public int imageWidth;

    /**
     * 高度
     */
    public int imageHeight;
}



