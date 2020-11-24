package xyz.tanwb.airship.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 文件上传信息
 */
public class UploadInfo implements Serializable {

    /**
     * 上传结果
     */
    public boolean success;

    /**
     * 上传结果描述
     */
    public String message;

    /**
     * 文件信息List
     */
    public List<FileInfo> returnTargets;

    // {
    // "message":"file upload success",
    // "returnTargets":[
    // {
    // "fileId":10009,
    // "fileName":"user/20150715/10009.jpg",
    // "fileRawName":"1436946896225.jpg",
    // "rawHeight":0,
    // "rawWidth":0,
    // "result":null,
    // "scaleHeight":0,
    // "scaleWidth":0,
    // "upLoadedFilePath":""
    // }
    // ],
    // "success":true
    // }

}
