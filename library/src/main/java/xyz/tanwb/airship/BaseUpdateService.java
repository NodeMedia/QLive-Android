package xyz.tanwb.airship;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import xyz.tanwb.airship.rxjava.RxBus;
import xyz.tanwb.airship.utils.Log;
import xyz.tanwb.airship.utils.SharedUtils;

/**
 * 检查更新后台下载服务
 */
public abstract class BaseUpdateService extends Service {

    private static final String DOWNLOADURL = "downloadUrl";
    private static final String DOWNLOADID = "downloadID";

    /**
     * 安卓系统下载类
     **/
    private DownloadManager manager;

    /**
     * 接收下载完的广播
     **/
    private DownloadCompleteReceiver receiver;

    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture scheduledFuture;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkUpdate();
        return super.onStartCommand(intent, flags, startId);
    }

    public abstract void checkUpdate();

    /**
     * 下载
     *
     * @param downloadUrl 下载路径
     */
    public void download(String downloadUrl) {
        if (!canDownloadState()) {
            Toast.makeText(this, "下载服务不可用,请您启用", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:com.android.providers.downloads"));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            Log.e("开始执行下载服务");

            manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            receiver = new DownloadCompleteReceiver();
            registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            String oldDownloadUrl = SharedUtils.getString(DOWNLOADURL, BaseConstants.NULL);
            if (downloadUrl.equals(oldDownloadUrl)) {
                long downloadId = SharedUtils.getLong(DOWNLOADID, -1L);
                if (downloadId != -1L) {
                    int status = getDownloadInfo(downloadId)[0];
                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            Uri uri = manager.getUriForDownloadedFile(downloadId);
                            if (uri != null) {
                                if (compareApkInfo(uri.getPath())) {
                                    Log.e("Apk已经下载,直接安装.");
                                    App.installAPK(uri);
                                    return;
                                } else {
                                    manager.remove(downloadId);
                                }
                            }
                            Log.e("未获取到存储Uri,重新下载.");
                            break;
                        case DownloadManager.STATUS_FAILED:
                            manager.remove(downloadId);
                            Log.e("下载失败,重新下载.");
                            break;
                        case DownloadManager.STATUS_PAUSED:
                            manager.remove(downloadId);
                            Log.e("下载等待重试或恢复,重新下载.");
                            break;
                        default:
                            Log.e("下载中...");
                            return;
                    }
                }
            }
            SharedUtils.put(DOWNLOADURL, downloadUrl);
            startDownload(downloadUrl);
        }
    }

    /**
     * 判断DownloadManager是否可用
     */
    private boolean canDownloadState() {
        int state = this.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
        return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED);
    }

    /**
     * 获取下载状态
     *
     * @param downloadId 下载ID
     * @return int[]
     * <li>result[0] represents download status, This will initially be 16.</li>
     * <li>result[1] represents downloaded bytes, This will initially be -1.</li>
     * <li>result[2] represents total bytes, This will initially be -1.</li>
     * @see DownloadManager#STATUS_PENDING 等待中
     * @see DownloadManager#STATUS_PAUSED 等待重试或恢复
     * @see DownloadManager#STATUS_RUNNING 下载中
     * @see DownloadManager#STATUS_SUCCESSFUL 成功
     * @see DownloadManager#STATUS_FAILED 失败
     */
    public int[] getDownloadInfo(long downloadId) {
        int[] bytesAndStatus = new int[]{DownloadManager.STATUS_PENDING, -1, -1};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = manager.query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    bytesAndStatus[0] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    bytesAndStatus[2] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                }
            } finally {
                c.close();
            }
        }
        return bytesAndStatus;
    }

    /**
     * 下载的apk和当前程序版本比较
     *
     * @param apkPath apk文件路径
     * @return 如果当前应用版本小于apk的版本则返回true
     */
    private boolean compareApkInfo(String apkPath) {
        PackageManager packageManager = getPackageManager();
        PackageInfo apkInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (apkInfo != null) {
            if (apkInfo.packageName.equals(getPackageName())) {
                if (apkInfo.versionCode > App.getVersionCode()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 开始调用系统内置下载管理器下载文件
     *
     * @param uri 下载路径
     */
    public void startDownload(String uri) {
        // 初始化下载请求，设置下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
        // 设置允许使用的网络类型，有NETWORK_MOBILE、NETWORK_WIFI、NETWORK_BLUETOOTH三种及其组合可供选择
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // 移动网络情况下是否允许漫游
        // request.setAllowedOverRoaming(false);
        // 下载进行中和下载完成的通知栏是否显示
        // VISIBILITY_VISIBLE 表示下载中显示通知栏提示，默认
        // VISIBILITY_VISIBLE_NOTIFY_COMPLETED表示下载中和完成后均显示通知栏提示
        // VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION 表示下载完成后显示通知栏提示
        // VISIBILITY_HIDDEN表示不显示任何通知栏提示,添加权限android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 表示允许Downloads应用扫描到这个文件并管理，默认不允许。
        request.setVisibleInDownloadsUi(true);
        // 表示允许MediaScanner扫描到这个文件，默认不允许。
        // request.allowScanningByMediaScanner();

        // 设置文件的保存的位置[三种方式]
        // 第一种:file:///storage/emulated/0/Android/data/your-package/files/Download/appname.apk
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, App.getAppName() + ".apk");
        // 第二种:file:///storage/emulated/0/Download/appname.apk
        // req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, App.getAppName() + ".apk");
        // 第三种 自定义文件路径
        // req.setDestinationUri()

        // 设置下载中通知栏提示的标题
        request.setTitle(App.getAppName());
        //设置下载中通知栏提示的介绍
        request.setDescription("正在为您更新App,请稍候.");
        // 设置下载文件的mineType
        // req.setMimeType("application/vnd.android.package-archive");
        // 添加请求下载的网络链接的http头，比如User - Agent，gzip压缩等
        // request.addRequestHeader(String header, String value);

        // 将下载请求放入队列
        long downloadId = manager.enqueue(request);
        SharedUtils.put(DOWNLOADID, downloadId);
        getDownloadProgress();
    }

    /**
     * 获取下载进度
     */
    public void getDownloadProgress() {
        Log.e("getDownloadProgress");
        scheduledExecutorService = Executors.newScheduledThreadPool(3);
        Runnable command = new Runnable() {
            @Override
            public void run() {
                long downloadId = SharedUtils.getLong(DOWNLOADID, -1L);
                if (downloadId != -1L) {
                    int[] bytesAndStatus = getDownloadInfo(downloadId);
                    Log.e("getDownloadProgress>>status:" + bytesAndStatus[0]);
                    if (bytesAndStatus[0] == DownloadManager.STATUS_PENDING || bytesAndStatus[0] == DownloadManager.STATUS_RUNNING) {
                        Log.e("getDownloadProgress>>downloaded bytes:" + bytesAndStatus[1] + " total bytes:" + bytesAndStatus[2]);
                        RxBus.getInstance().post("UpdateService", JSON.toJSONString(bytesAndStatus));
                        return;
                    }
                }
                scheduledFuture.cancel(true);
                scheduledExecutorService.shutdown();
            }
        };
        // schedule(task,initDelay):安排所提交的Callable或Runnable任务在initDelay指定的时间后执行。
        // scheduleAtFixedRate()：安排所提交的Runnable任务按指定的间隔重复执行
        // scheduleWithFixedDelay()：安排所提交的Runnable任务在每次执行完后，等待delay所指定的时间后重复执行。
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(command, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // 注销下载广播
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    /**
     * 下载监听广播
     */
    class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //判断是否下载完成的广播
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                //获取下载的文件id
                long downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                long id = SharedUtils.getLong(DOWNLOADID, -1L);
                if (downloadApkId == id) {
                    Uri apkUri = manager.getUriForDownloadedFile(downloadApkId);
                    if (apkUri != null) {
                        App.installAPK(apkUri);
                        // android.os.Process.killProcess(android.os.Process.myPid());
                        // 如果不加上这句的话在apk安装完成之后点击单开会崩溃
                        //停止服务并关闭广播
                        BaseUpdateService.this.stopSelf();
                    } else {
                        String oldDownloadUrl = SharedUtils.getString(DOWNLOADURL, BaseConstants.NULL);
                        download(oldDownloadUrl);
                    }
                }
            }
        }
    }
}
