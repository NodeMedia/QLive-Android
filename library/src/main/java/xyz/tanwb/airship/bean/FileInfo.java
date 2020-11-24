package xyz.tanwb.airship.bean;

import java.io.Serializable;
import java.util.Map;

/**
 * 文件信息
 */
public class FileInfo implements Serializable {

    /**
     * 文件ID 如:10027
     */
    public long fileId;

    /**
     * 文件长度 如:178489
     */
    public long fileLength;

    /**
     * 文件名 如:apps/20160421/10027.jpg
     */
    public String fileName;

    /**
     * 原始名称 如:IMG_1461215326113.jpg
     */
    public String fileRawName;

    /**
     * 文件类型 如:jpg
     */
    public String fileType;
    
    /**
     * 原始文件宽度 如:1124
     */
    public int rawWidth;

    /**
     * 原始文件高度 如:1124
     */
    public int rawHeight;

    /**
     * 目标裁剪宽度 如:1124
     */
    public int scaleWidth;

    /**
     * 目标裁剪高度 如:1124
     */
    public int scaleHeight;

    /**
     * 待定 如:null
     */
    public Map<String, Object> result;

}
