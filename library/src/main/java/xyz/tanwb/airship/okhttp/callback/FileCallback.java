package xyz.tanwb.airship.okhttp.callback;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;
import xyz.tanwb.airship.okhttp.OkHttpManager;
import xyz.tanwb.airship.utils.FileUtils;

/**
 * 文件的回调下载进度监听
 */
public abstract class FileCallback extends AbsCallback<File> {

    /**
     * 目标文件存储的文件夹路径
     */
    private String destFileDir;
    /**
     * 目标文件存储的文件名
     */
    private String destFileName;

    public FileCallback(String destFileName) {
        this(FileUtils.getAppSdPath(FileUtils.PATH_FILE), destFileName);
    }

    /**
     * @param destFileDir  要保存的目标文件夹
     * @param destFileName 要保存的文件名
     */
    public FileCallback(@NonNull String destFileDir, @NonNull String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
    }

    @Override
    public File parseNetworkResponse(Response response) throws Exception {
        try {
            return saveFile(response);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File saveFile(Response response) throws IOException {
        File dir = new File(destFileDir);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, destFileName);
        if (file.exists()) file.delete();

        long lastRefreshUiTime = 0;  //最后一次刷新的时间
        long lastWriteBytes = 0;     //最后一次写入字节数据

        InputStream is = null;
        byte[] buf = new byte[2048];
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();
            long sum = 0;
            int len;
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;

                long curTime = System.currentTimeMillis();
                //每200毫秒刷新一次数据
                if (curTime - lastRefreshUiTime >= 200 || finalSum == total) {
                    //计算下载速度
                    long diffTime = (curTime - lastRefreshUiTime) / 1000;
                    if (diffTime == 0) diffTime += 1;
                    long diffBytes = finalSum - lastWriteBytes;
                    final long networkSpeed = diffBytes / diffTime;
                    OkHttpManager.getInstance().getDelivery().post(new Runnable() {
                        @Override
                        public void run() {
                            downloadProgress(finalSum, total, finalSum * 1.0f / total, networkSpeed);   //进度回调的方法
                        }
                    });

                    lastRefreshUiTime = System.currentTimeMillis();
                    lastWriteBytes = finalSum;
                }
            }
            fos.flush();
            return file;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 通过 ‘？’ 和 ‘/’ 判断文件名
     */
    private String getUrlFileName(String url) {
        int index = url.lastIndexOf('?');
        String filename;
        if (index > 1) {
            filename = url.substring(url.lastIndexOf('/') + 1, index);
        } else {
            filename = url.substring(url.lastIndexOf('/') + 1);
        }
        return filename;
    }
}
