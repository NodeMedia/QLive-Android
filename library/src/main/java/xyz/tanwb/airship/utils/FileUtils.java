package xyz.tanwb.airship.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import androidx.annotation.IntDef;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.BaseConstants;

/**
 * 文件操作
 */
public class FileUtils {

    public static final int PATH_CACHE = 0X00000001;
    public static final int PATH_FILE = 0X00000002;
    public static final int PATH_DB = 0X00000003;

    @IntDef({PATH_CACHE, PATH_FILE, PATH_DB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PathType {
    }

    /**
     * 获取App默认缓存路径
     */
    public static String getCachePath() {
        return getAppDefPath(PATH_CACHE);
    }

    /**
     * 获取App默认文件路径
     */
    public static String getFilePath() {
        return getAppDefPath(PATH_FILE);
    }

    /**
     * 获取App默认文件路径
     * FileUtils.FILE_CACHE /data/data/packageName/cache
     * FileUtils.FILE_FILE /data/data/packageName/files
     */
    private static String getAppDefPath(@PathType int fileType) {
        File result = null;
        switch (fileType) {
            case PATH_CACHE:
                result = App.app().getCacheDir();
                break;
            case PATH_FILE:
                result = App.app().getFilesDir();
                break;
        }

        if (result != null) {
            return result.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * 判断是否存在SD卡
     */
    public static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录地址
     *
     * @return 根目录
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 获取App SD卡文件路径
     * 注意:Android 6.0 以上需要申请权限
     */
    public static String getAppSdPath(@PathType int fileType) {
        if (isSDCardEnable()) {
            String fileName = null;
            switch (fileType) {
                case PATH_CACHE:
                    fileName = "cache";
                    break;
                case PATH_FILE:
                    fileName = "file";
                    break;
                case PATH_DB:
                    fileName = "database";
                    break;
            }
            File result = createDirectory(getSDCardPath() + App.getAppName() + File.separator + fileName);

            if (isFileExists(result)) {
                return result.getAbsolutePath();
            }
        }
        return getAppDefPath(fileType);
    }

    /**
     * 判断文件或文件夹是否存在
     *
     * @param filePath 文件或文件夹路径
     */
    public static boolean isFileExists(String filePath) {
        return !TextUtils.isEmpty(filePath) && isFileExists(new File(filePath));
    }

    /**
     * 判断文件或文件夹是否存在
     *
     * @param file 文件或文件夹对象
     */
    public static boolean isFileExists(File file) {
        return file != null && file.exists();
    }

    /**
     * 创建文件夹
     *
     * @param directoryPath 文件夹路径
     */
    public static File createDirectory(String directoryPath) {
        if (!TextUtils.isEmpty(directoryPath)) {
            return createDirectory(new File(directoryPath));
        }
        return null;
    }

    /**
     * 创建文件夹
     *
     * @param dirFile File对象
     */
    public static File createDirectory(File dirFile) {
        if (dirFile != null) {
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            return dirFile;
        }
        return null;
    }

    /**
     * 创建文件
     *
     * @param dFile    文件所在文件夹对象
     * @param fileName 文件名称
     */
    public static File createFile(File dFile, String fileName) {
        return createFile(new File(dFile, fileName));
    }

    /**
     * 创建文件
     *
     * @param fileName 文件路径
     */
    public static File createFile(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            return createFile(new File(fileName));
        }
        return null;
    }

    /**
     * 创建文件
     *
     * @param file 文件对象
     */
    public static File createFile(File file) {
        if (file != null) {
            if (file.exists()) {
                return file;
            }

            //判断目标文件所在的目录是否存在
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    return null;
                }
            }

            try {
                if (file.createNewFile()) {
                    return file;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 从文件路径获取文件夹路径
     */
    public static String getFolderName(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            int separatorIndex = filePath.lastIndexOf(File.separator);
            return (separatorIndex == -1) ? null : filePath.substring(0, separatorIndex);
        }
        return null;
    }

    /**
     * 从文件路径中获取文件名
     *
     * @param filePath 文件路径
     * @param isSuffix 是否带后缀
     */
    public static String getFileName(String filePath, boolean isSuffix) {
        if (!TextUtils.isEmpty(filePath)) {
            int separatorIndex = filePath.lastIndexOf(File.separator) + 1;
            if (!isSuffix) {
                int dotIndex = filePath.lastIndexOf(BaseConstants.DOT);
                if (dotIndex > -1) {
                    return filePath.substring(separatorIndex, dotIndex);
                }
            }
            return filePath.substring(separatorIndex);
        }
        return null;
    }

    /**
     * 从文件路径中获取文件后缀
     *
     * @param filePath 文件路径
     * @param isDot    是否带'.'
     */
    public static String getFileSuffix(String filePath, boolean isDot) {
        if (!TextUtils.isEmpty(filePath)) {
            return filePath.substring(filePath.lastIndexOf(BaseConstants.DOT) + (isDot ? 0 : 1));
        }
        return null;
    }

    /**
     * 根据文件后缀获取文件MIME类型
     *
     * @param fileSuffix 文件后缀名
     * @param isDot      是否带'.'
     */
    public static String getMIMEType(String fileSuffix) {
        Map<String, String> mimeMap = new HashMap<>();
        mimeMap.put(".3gp", "video/3gpp");
        mimeMap.put(".apk", "application/vnd.android.package-archive");
        mimeMap.put(".asf", "video/x-ms-asf");
        mimeMap.put(".avi", "video/x-msvideo");
        mimeMap.put(".bin", "application/octet-stream");
        mimeMap.put(".bmp", "image/bmp");
        mimeMap.put(".c", "text/plain");
        mimeMap.put(".class", "application/octet-stream");
        mimeMap.put(".conf", "text/plain");
        mimeMap.put(".cpp", "text/plain");
        mimeMap.put(".doc", "application/msword");
        mimeMap.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeMap.put(".xls", "application/vnd.ms-excel");
        mimeMap.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimeMap.put(".exe", "application/octet-stream");
        mimeMap.put(".gif", "image/gif");
        mimeMap.put(".gtar", "application/x-gtar");
        mimeMap.put(".gz", "application/x-gzip");
        mimeMap.put(".h", "text/plain");
        mimeMap.put(".htm", "text/html");
        mimeMap.put(".html", "text/html");
        mimeMap.put(".jar", "application/java-archive");
        mimeMap.put(".java", "text/plain");
        mimeMap.put(".jpeg", "image/jpeg");
        mimeMap.put(".jpg", "image/jpeg");
        mimeMap.put(".js", "application/x-javascript");
        mimeMap.put(".log", "text/plain");
        mimeMap.put(".m3u", "audio/x-mpegurl");
        mimeMap.put(".m4a", "audio/mp4a-latm");
        mimeMap.put(".m4b", "audio/mp4a-latm");
        mimeMap.put(".m4p", "audio/mp4a-latm");
        mimeMap.put(".m4u", "video/vnd.mpegurl");
        mimeMap.put(".m4v", "video/x-m4v");
        mimeMap.put(".mov", "video/quicktime");
        mimeMap.put(".mp2", "audio/x-mpeg");
        mimeMap.put(".mp3", "audio/x-mpeg");
        mimeMap.put(".mp4", "video/mp4");
        mimeMap.put(".mpc", "application/vnd.mpohun.certificate");
        mimeMap.put(".mpe", "video/mpeg");
        mimeMap.put(".mpeg", "video/mpeg");
        mimeMap.put(".mpg", "video/mpeg");
        mimeMap.put(".mpg4", "video/mp4");
        mimeMap.put(".mpga", "audio/mpeg");
        mimeMap.put(".msg", "application/vnd.ms-outlook");
        mimeMap.put(".ogg", "audio/ogg");
        mimeMap.put(".pdf", "application/pdf");
        mimeMap.put(".png", "image/png");
        mimeMap.put(".pps", "application/vnd.ms-powerpoint");
        mimeMap.put(".ppt", "application/vnd.ms-powerpoint");
        mimeMap.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mimeMap.put(".prop", "text/plain");
        mimeMap.put(".rc", "text/plain");
        mimeMap.put(".rmvb", "audio/x-pn-realaudio");
        mimeMap.put(".rtf", "application/rtf");
        mimeMap.put(".sh", "text/plain");
        mimeMap.put(".tar", "application/x-tar");
        mimeMap.put(".tgz", "application/x-compressed");
        mimeMap.put(".txt", "text/plain");
        mimeMap.put(".wav", "audio/x-wav");
        mimeMap.put(".wma", "audio/x-ms-wma");
        mimeMap.put(".wmv", "audio/x-ms-wmv");
        mimeMap.put(".wps", "application/vnd.ms-works");
        mimeMap.put(".xml", "text/plain");
        mimeMap.put(".z", "application/x-compress");
        mimeMap.put(".zip", "application/x-zip-compressed");
        mimeMap.put(BaseConstants.NULL, "*/*");

        if (!fileSuffix.startsWith(BaseConstants.DOT)) {
            fileSuffix = BaseConstants.DOT + fileSuffix;
        }
        if (mimeMap.containsKey(fileSuffix)) {
            return mimeMap.get(fileSuffix);
        }
        return mimeMap.get(BaseConstants.NULL);
    }

    /**
     * 打开文件
     *
     * @param filePath 文件路径
     */
    public static boolean openFile(Context context, String filePath) {
        return !TextUtils.isEmpty(filePath) && openFile(context, new File(filePath));
    }

    /**
     * 打开文件
     *
     * @param file 文件对象
     */
    public static boolean openFile(Context context, File file) {
        if (isFileExists(file) && file.isFile()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), getMIMEType(getFileSuffix(file.getAbsolutePath(), true)));
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * 获取文件系统相关描述信息
     */
    private static StatFs getStatFs() {
        if (isSDCardEnable()) {
            return new StatFs(getSDCardPath());
        }
        return null;
    }

    /**
     * 获取SD卡总空间
     */
    public static long getSDCardTotalSpace() {
        StatFs statFs = getStatFs();
        if (statFs != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return statFs.getBlockSizeLong() * statFs.getBlockCountLong();
            } else {
                return statFs.getBlockSize() * statFs.getBlockCount();
            }
        }
        return 0L;
    }

    /**
     * 获取SD卡剩余空间
     */
    public static long getSDCardAvailSpace() {
        StatFs statFs = getStatFs();
        if (statFs != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
            } else {
                return statFs.getBlockSize() * statFs.getAvailableBlocks();
            }
        }
        return 0L;
    }

    /**
     * 获取文件总大小
     */
    public static long getFileSize(File file) {
        long dirSize = 0L;
        if (isFileExists(file)) {
            if (file.isFile()) {
                dirSize = file.length();
            } else {
                File[] files = file.listFiles();
                // 文件夹被删除时, 子文件正在被写入, 文件属性异常返回null.
                if (files != null) {
                    for (File f : files) {
                        dirSize += getFileSize(f);
                    }
                }
            }
        }
        return dirSize;
    }

    /**
     * 获取文件夹下文件总个数
     */
    public long getFileCount(File file) {
        int count = 0;
        if (isFileExists(file) && file.isFile()) {
            if (file.isFile()) {
                count = 1;
            } else {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        count += getFileCount(f);
                    }
                }
            }
        }
        return count;
    }

    /**
     * 格式化文件大小
     *
     * @param fileSize 文件大小
     */
    public static String formatFileSize(long fileSize) {
        DecimalFormat df = new DecimalFormat("##.##");
        String fileSizeString;
        if (fileSize < 1024L) {
            fileSizeString = df.format((double) fileSize) + "B";
        } else if (fileSize < 1048576L) {
            fileSizeString = df.format((double) fileSize / 1024.0D) + "KB";
        } else if (fileSize < 1073741824L) {
            fileSizeString = df.format((double) fileSize / 1048576.0D) + "MB";
        } else {
            fileSizeString = df.format((double) fileSize / 1.073741824E9D) + "G";
        }
        return fileSizeString;
    }

    /**
     * 文件拷贝
     *
     * @param srcPath 源路径
     * @param dstPath 目标路径
     */
    public static void copyFile(String srcPath, String dstPath) throws Exception {
        if (TextUtils.isEmpty(srcPath)) {
            throw new RuntimeException("srcPath is Null.");
        }
        if (TextUtils.isEmpty(dstPath)) {
            throw new RuntimeException("dstPath is Null.");
        }
        copyFile(new File(srcPath), new File(dstPath));
    }

    /**
     * 文件拷贝
     *
     * @param src source 源文件
     * @param dst destination 目标文件
     */
    public static void copyFile(File src, File dst) throws Exception {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);
        FileChannel inChannel = in.getChannel();
        FileChannel outChannel = out.getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            outChannel.close();
        }
        in.close();
        out.close();
    }

    /**
     * 移动文件
     *
     * @param srcPath 源路径
     * @param dstPath 目标路径
     */
    public static void moveFile(String srcPath, String dstPath) throws Exception {
        if (TextUtils.isEmpty(srcPath)) {
            throw new RuntimeException("srcPath is Null.");
        }
        if (TextUtils.isEmpty(dstPath)) {
            throw new RuntimeException("dstPath is Null.");
        }
        moveFile(new File(srcPath), new File(dstPath));
    }

    /**
     * 移动文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     */
    public static void moveFile(File srcFile, File destFile) throws Exception {
        if (!srcFile.renameTo(destFile)) {
            copyFile(srcFile, destFile);
            deleteFile(srcFile);
        }
    }

    /**
     * 删除文件或目录
     */
    public static boolean deleteFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            SecurityManager se = new SecurityManager();
            se.checkDelete(filePath);
            return deleteFile(new File(filePath));
        }
        return false;
    }

    /**
     * 删除文件或目录
     */
    public static boolean deleteFile(File file) {
        if (!isFileExists(file)) {
            return true;
        } else if (file.isFile()) {
            return file.delete();
        } else {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteFile(f);
            }
            return file.delete();
        }
    }

    /**
     * 对文件设置root权限
     *
     * @param filePath 文件路径
     */
    public static void upgradeRootPermission(String filePath) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            throw new RuntimeException("filePath is Null.");
        }
        String cmd = "chmod 777 " + filePath;
        Process process = Runtime.getRuntime().exec("su"); //切换到root帐号
        DataOutputStream os = new DataOutputStream(process.getOutputStream());
        os.writeBytes(cmd + "\n");
        os.writeBytes("exit\n");
        os.flush();
        process.waitFor();
        os.close();
        process.destroy();
    }

    /**
     * 将字符串写入文件
     *
     * @param filePath 文件路径
     * @param content  字符串内容
     * @param append   设置为true,字节将被写入到文件的结尾,而不是开始
     */
    public static void writeFile(String filePath, String content, boolean append) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            throw new RuntimeException("filePath is Null.");
        }
        writeFile(new File(filePath), content, append);
    }

    /**
     * 将字符串写入文件
     *
     * @param file    文件对象
     * @param content 字符串内容
     * @param append  设置为true,字节将被写入到文件的结尾,而不是开始
     */
    public static void writeFile(File file, String content, boolean append) throws Exception {
        if (TextUtils.isEmpty(content)) {
            throw new RuntimeException("content is Null.");
        }
        if (file == null) {
            throw new RuntimeException("file is Null.");
        }
        FileWriter fileWriter = new FileWriter(file, append);
        fileWriter.write(content);
        fileWriter.close();
    }

    /**
     * 将字符串列表写入文件
     *
     * @param filePath    文件路径
     * @param contentList 字符串列表
     * @param append      设置为true,字节将被写入到文件的结尾,而不是开始
     */
    public static void writeFile(String filePath, List<String> contentList, boolean append) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            throw new RuntimeException("filePath is Null.");
        }
        writeFile(new File(filePath), contentList, append);
    }

    /**
     * 将字符串列表写入文件
     *
     * @param file        文件对象
     * @param contentList 字符串列表
     * @param append      设置为true,字节将被写入到文件的结尾,而不是开始
     */
    public static void writeFile(File file, List<String> contentList, boolean append) throws Exception {
        if (contentList == null || contentList.size() == 0) {
            throw new RuntimeException("contentList is Null.");
        }
        if (file == null) {
            throw new RuntimeException("file is Null.");
        }
        FileWriter fileWriter = new FileWriter(file, append);
        for (String line : contentList) {
            fileWriter.write(line);
        }
        fileWriter.close();
    }

    /**
     * 将输入流写入文件
     *
     * @param filePath 文件路径
     * @param stream   输入流
     * @param append   设置为true,字节将被写入到文件的结尾,而不是开始
     */
    public static void writeFile(String filePath, InputStream stream, boolean append) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            throw new RuntimeException("filePath is Null.");
        }
        writeFile(new File(filePath), stream, append);
    }

    /**
     * 将输入流写入文件
     *
     * @param file   文件对象
     * @param stream 输入流
     * @param append 设置为true,字节将被写入到文件的结尾,而不是开始
     */
    public static void writeFile(File file, InputStream stream, boolean append) throws Exception {
        if (file == null) {
            throw new RuntimeException("file is Null.");
        }
        FileOutputStream fos = new FileOutputStream(file, append);
        byte[] data = new byte[1024];
        int length;
        while ((length = stream.read(data)) != -1) {
            fos.write(data, 0, length);
        }
        fos.flush();
        fos.close();
        stream.close();
    }

    /**
     * 读取文件
     *
     * @param filePath    文件路径
     * @param charsetName The name of a supported {@link java.nio.charset.Charset <code>charset</code>}
     */
    public static StringBuilder readFile(String filePath, String charsetName) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            throw new RuntimeException("filePath is Null.");
        }
        return readFile(new File(filePath), charsetName);
    }

    /**
     * 读取文件
     *
     * @param file        文件对象
     * @param charsetName The name of a supported {@link java.nio.charset.Charset <code>charset</code>}
     */
    public static StringBuilder readFile(File file, String charsetName) throws Exception {
        if (file != null && file.isFile()) {
            StringBuilder fileContent = new StringBuilder();

            BufferedReader reader = null;
            try {
                InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
                reader = new BufferedReader(is);

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!fileContent.toString().equals(BaseConstants.NULL)) {
                        fileContent.append("\r\n");
                    }
                    fileContent.append(line);
                }

                return fileContent;
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        return null;
    }

    /**
     * 读取文件到字符串列表，列表中的元素是一行
     *
     * @param filePath    文件路径
     * @param charsetName The name of a supported {@link java.nio.charset.Charset <code>charset</code>}
     */
    public static List<String> readFileToList(String filePath, String charsetName) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            throw new RuntimeException("filePath is Null.");
        }
        return readFileToList(new File(filePath), charsetName);
    }

    /**
     * 读取文件到字符串列表，列表中的元素是一行
     *
     * @param file        文件对象
     * @param charsetName The name of a supported {@link java.nio.charset.Charset <code>charset</code>}
     */
    public static List<String> readFileToList(File file, String charsetName) throws Exception {
        if (file != null && file.isFile()) {
            List<String> fileContent = new ArrayList<>();

            BufferedReader reader = null;
            try {
                InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
                reader = new BufferedReader(is);

                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.add(line);
                }

                return fileContent;
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        return null;
    }

    /**
     * 从输入流读取数据
     *
     * @param inStream 输入流
     */
    public static String readISToString(InputStream inStream) throws IOException {
        return new String(readISToByte(inStream), BaseConstants.UTF8);
    }

    /**
     * 从输入流读取数据
     *
     * @param inStream 输入流
     */
    public static byte[] readISToByte(InputStream inStream) throws IOException {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }
}
