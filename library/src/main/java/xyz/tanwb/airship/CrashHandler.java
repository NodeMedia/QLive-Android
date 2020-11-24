package xyz.tanwb.airship;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import xyz.tanwb.airship.utils.FileUtils;
import xyz.tanwb.airship.utils.Log;
import xyz.tanwb.airship.utils.ScreenUtils;

/**
 * 全局异常捕获
 * 在Application中调用
 * CrashHandler crashHandler = CrashHandler.getInstance();
 * crashHandler.init(INSTANCE);
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    //CrashHandler实例
    private static CrashHandler instance;

    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
    private LinkedHashMap<String, String> infos = new LinkedHashMap<String, String>();
    //用于格式化日期,作为日志文件名的一部分
    // private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",Locale.getDefault());

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);
        if (mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }
        // else {
        // Intent intent = new Intent(mContext.getApplicationContext(), LauncherActivity.class);
        // PendingIntent restartIntent = PendingIntent.getActivity(mContext.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        // //退出程序
        // AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        // mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
        // android.os.Process.killProcess(android.os.Process.myPid());
        // }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //收集设备参数信息
        collectDeviceInfo();
        //保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     */
    public void collectDeviceInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                infos.put("VersionName", pi.versionName);
                infos.put("VersionCode", String.valueOf(pi.versionCode));
                infos.put("AndroidVersion", String.valueOf(Build.VERSION.SDK_INT));
                infos.put("Screen", ScreenUtils.getScreenHeight() + "*" + ScreenUtils.getScreenWidth());
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("an error occured when collect package info", e);
        }
        //获取手机所有类型信息 getDeclaredFields是获取所有申明信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e("an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {
        StringBuilder logMsg = new StringBuilder();
        //遍历HashMap
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            logMsg.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\r\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        logMsg.append(writer.toString()).append("\n");

        // Log.e(logMsg.toString());

        String filePath = FileUtils.getAppSdPath(FileUtils.PATH_CACHE) + File.separator + "log.text";
        try {
            FileUtils.writeFile(filePath, logMsg.toString(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
