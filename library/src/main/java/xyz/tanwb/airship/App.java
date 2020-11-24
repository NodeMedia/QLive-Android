package xyz.tanwb.airship;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import xyz.tanwb.airship.utils.FileUtils;

/**
 * 应用工具类
 */
public class App {

    private static Application app;
    private static boolean debug;

    public static void init(Application application) {
        app = application;
        debug = (app.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Application app() {
        if (app == null) {
            // 在IDE进行布局预览时使用
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    Class<?> renderActionClass = Class.forName("com.android.layoutlib.bridge.impl.RenderAction");
                    Method method = renderActionClass.getDeclaredMethod("getCurrentContext");
                    Context context = (Context) method.invoke(null);
                    app = new MockApplication(context);
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throwException();
                }
            } else {
                throwException();
            }
        }
        return app;
    }

    /**
     * 抛出自定义异常
     */
    private static void throwException() {
        throw new RuntimeException("please invoke app.init(app) on Application#onCreate() and register your Application in manifest.");
    }

    /**
     * 获取是否为Debug模式
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * 获取应用名称
     */
    public static String getAppName() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            int labelRes = packageInfo.applicationInfo.labelRes;
            return app().getResources().getString(labelRes);
        }
        return null;
    }

    /**
     * 获取App包名
     */
    public static String getPackageName() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            return packageInfo.packageName;
        }
        return null;
    }

    /**
     * 获取UID
     */
    public static int getPackageUID() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            return packageInfo.applicationInfo.uid;
        }
        return 0;
    }

    /**
     * 获取App版本
     */
    public static String getVersionName() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return null;
    }

    /**
     * 获取App版本号
     */
    public static int getVersionCode() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            return packageInfo.versionCode;
        }
        return 0;
    }

    /**
     * 获取应用包信息
     */
    private static PackageInfo getPackageInfo() {
        try {
            PackageManager e = app().getPackageManager();
            return e.getPackageInfo(app().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取手机信息 需要权限
     * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     */
    public static TelephonyManager getTelephonyInfo() {
        TelephonyManager tm = (TelephonyManager) app().getSystemService(Context.TELEPHONY_SERVICE);
        StringBuilder sb = new StringBuilder();
        sb.append("\nDeviceId(IMEI) = ").append(tm.getDeviceId());
        sb.append("\nDeviceSoftwareVersion = ").append(tm.getDeviceSoftwareVersion());
        sb.append("\nLine1Number = ").append(tm.getLine1Number());
        sb.append("\nNetworkCountryIso = ").append(tm.getNetworkCountryIso());
        sb.append("\nNetworkOperator = ").append(tm.getNetworkOperator());
        sb.append("\nNetworkOperatorName = ").append(tm.getNetworkOperatorName());
        sb.append("\nNetworkType = ").append(tm.getNetworkType());
        sb.append("\nPhoneType = ").append(tm.getPhoneType());
        sb.append("\nSimCountryIso = ").append(tm.getSimCountryIso());
        sb.append("\nSimOperator = ").append(tm.getSimOperator());
        sb.append("\nSimOperatorName = ").append(tm.getSimOperatorName());
        sb.append("\nSimSerialNumber = ").append(tm.getSimSerialNumber());
        sb.append("\nSimState = ").append(tm.getSimState());
        sb.append("\nSubscriberId(IMSI) = ").append(tm.getSubscriberId());
        sb.append("\nVoiceMailNumber = ").append(tm.getVoiceMailNumber());
        return tm;
    }

    /**
     * 获取手机可用的cpu数
     */
    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    private static ActivityManager.RunningAppProcessInfo getProcessInfo(String processName) {
        ActivityManager activityManager = (ActivityManager) app().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessList = activityManager.getRunningAppProcesses();
        if (appProcessList != null && appProcessList.size() > 0) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                if (appProcess.processName.equals(processName)) {
                    return appProcess;
                }
            }
        }
        return null;
    }

    /**
     * 判断某个应用当前是否正在运行
     *
     * @param processName 应用包名
     */
    public static boolean isNamedProcess(String processName) {
        ActivityManager.RunningAppProcessInfo appProcess = getProcessInfo(processName);
        return appProcess != null && appProcess.pid == android.os.Process.myPid();
    }

    /**
     * 判断程序是否在后台运行
     */
    public static boolean isAppInBackground() {
        ActivityManager.RunningAppProcessInfo appProcess = getProcessInfo(getPackageName());
        return appProcess != null && appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }

    /**
     * 判断服务是否运行
     *
     * @param serviceName 服务名称
     */
    public static boolean isServiceRunning(String serviceName) {
        ActivityManager activityManager = (ActivityManager) app().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfoList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (serviceInfoList != null && serviceInfoList.size() > 0) {
            for (ActivityManager.RunningServiceInfo serviceInfo : serviceInfoList) {
                if (serviceInfo.service.getClassName().equals(serviceName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查预调用的Action是否可用
     */
    public static boolean isServiceAvailable(Intent intent) {
        PackageManager packageManager = app().getPackageManager();
        //检索所有可用于给定的意图进行的活动。如果没有匹配的活动，则返回一个空列表。
        List<ResolveInfo> list = packageManager.queryIntentServices(intent, 0);
        return list.size() > 0;
    }

    /**
     * 判断一个activity是否在前台运行
     *
     * @param activityName activity的全路径名称
     */
    public static boolean isTopActivy(String activityName) {
        ActivityManager manager = (ActivityManager) app().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        String cmpNameTemp = null;
        if (runningTaskInfos != null) {
            cmpNameTemp = runningTaskInfos.get(0).topActivity.getShortClassName();
        }
        return cmpNameTemp != null && cmpNameTemp.endsWith(activityName);
    }

    /**
     * 安装apk
     *
     * @param filePath apk文件的绝对路径
     */
    public static void installAPK(String filePath) {
        installAPK(Uri.fromFile(new File(filePath)));
    }

    /**
     * 安装apk
     *
     * @param apkUri apk文件的Uri
     */
    public static void installAPK(Uri apkUri) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT < 23) {
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app().startActivity(install);
        } else {
            FileUtils.openFile(app(), apkUri.getPath());
        }
    }

    /**
     * 打开网络设置界面
     */
    public static void openNetSetting(Context context) {
        Intent intent;
        // 判断手机系统的版本 即API大于10 就是3.0或以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction(Intent.ACTION_VIEW);
        }
        context.startActivity(intent);
    }

    /**
     * 打开权限设置界面
     */
    public static void openPermissionSetting(Context context) {
        // 跳转到权限设置界面
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        context.startActivity(intent);
    }

    /**
     * 获取AndroidManifest.xml中<meta-data></meta-data>节点的值
     */
    public static <T> T getMetaData(String name) {
        try {
            ApplicationInfo ai = app().getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                return (T) ai.metaData.get(name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            System.out.print("Couldn't find meta-data: " + name);
        }
        return null;
    }

    /**
     * 判断当前线程是否为UI线程
     */
    public static boolean isUIThread() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }

    /**
     * 根据资源名称和类型,得到资源ID
     *
     * @param resName 资源名称
     * @param type    资源类型 id/attr/array/anim/bool/color/dimen/drawable/integer/layout/menu/mipmap/raw/string/style/styleable
     */
    public static int getResId(String resName, String type) {
        Resources resources = app().getResources();
        return resources.getIdentifier(resName, type, getPackageName());
    }

    /**
     * 根据资源ID获取对应URI
     */
    public static Uri getResIdToUri(int resId) {
        return Uri.parse("android.resource://" + getPackageName() + BaseConstants.SLASH + resId);
    }

    /**
     * 打开软键盘
     */
    public static void openKeybord(View view) {
        InputMethodManager imm = (InputMethodManager) app().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭虚拟键盘
     */
    public static void stopKeybord(View... views) {
        InputMethodManager imm = (InputMethodManager) app().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (views != null && views.length > 0) {
            for (View view : views) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * 获取应用公钥签名
     */
    public static Signature sign(Context context) {
        Signature sign = null;
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            sign = pi.signatures[0];
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return sign;
    }

    /**
     * 比较当前签名HashCode和预设的HashCode
     */
    public static boolean signCheckWithHashCode(Context context, int presetHashCode) {
        Signature signature = sign(context);
        return signature.hashCode() == presetHashCode;
    }

    /**
     * Application替身
     */
    private static class MockApplication extends Application {
        MockApplication(Context baseContext) {
            this.attachBaseContext(baseContext);
        }
    }
}
