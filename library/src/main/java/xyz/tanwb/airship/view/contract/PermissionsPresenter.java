package xyz.tanwb.airship.view.contract;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xyz.tanwb.airship.BaseConstants;
import xyz.tanwb.airship.utils.Log;
import xyz.tanwb.airship.view.BasePresenter;
import xyz.tanwb.airship.view.BaseView;

/**
 * 权限申请
 * <p>
 * 不需要手动申请的权限
 * android.permission.ACCESS_LOCATION_EXTRA_COMMANDS
 * android.permission.ACCESS_NETWORK_STATE
 * android.permission.ACCESS_NOTIFICATION_POLICY
 * android.permission.ACCESS_WIFI_STATE
 * android.permission.ACCESS_WIMAX_STATE
 * android.permission.BLUETOOTH
 * android.permission.BLUETOOTH_ADMIN
 * android.permission.BROADCAST_STICKY
 * android.permission.CHANGE_NETWORK_STATE
 * android.permission.CHANGE_WIFI_MULTICAST_STATE
 * android.permission.CHANGE_WIFI_STATE
 * android.permission.CHANGE_WIMAX_STATE
 * android.permission.DISABLE_KEYGUARD
 * android.permission.EXPAND_STATUS_BAR
 * android.permission.FLASHLIGHT
 * android.permission.GET_ACCOUNTS
 * android.permission.GET_PACKAGE_SIZE
 * android.permission.INTERNET
 * android.permission.KILL_BACKGROUND_PROCESSES
 * android.permission.MODIFY_AUDIO_SETTINGS
 * android.permission.NFC
 * android.permission.READ_SYNC_SETTINGS
 * android.permission.READ_SYNC_STATS
 * android.permission.RECEIVE_BOOT_COMPLETED
 * android.permission.REORDER_TASKS
 * android.permission.REQUEST_INSTALL_PACKAGES
 * android.permission.SET_TIME_ZONE
 * android.permission.SET_WALLPAPER
 * android.permission.SET_WALLPAPER_HINTS
 * android.permission.SUBSCRIBED_FEEDS_READ
 * android.permission.TRANSMIT_IR
 * android.permission.USE_FINGERPRINT
 * android.permission.VIBRATE
 * android.permission.WAKE_LOCK
 * android.permission.WRITE_SYNC_SETTINGS
 * com.android.alarm.permission.SET_ALARM
 * com.android.launcher.permission.INSTALL_SHORTCUT
 * com.android.launcher.permission.UNINSTALL_SHORTCUT
 * <p>
 * Android 6.0 需要申请的权限
 * android.permission_group.CAMERA
 * CAMERA 请求访问使用照相设备
 * android.permission_group.MICROPHONE
 * RECORD_AUDIO 允许程序录制音频
 * android.permission-group.STORAGE
 * READ_EXTERNAL_STORAGE 程序可以读取设备外部存储空间的文件(内置SDcard和外置SDCard).
 * WRITE_EXTERNAL_STORAGE 允许程序写入外部存储(内置SDcard和外置SDCard).
 * android.permission_group.LOCATION
 * ACCESS_FINE_LOCATION 通过GPS芯片接收卫星的定位信息,定位精度达10米以内.
 * ACCESS_COARSE_LOCATION 访问CellID或WiFi,只要当前设备可以接收到基站的服务信号,便可获得位置信息.
 * android.permission_group.CALENDAR
 * READ_CALENDAR 允许程序读取用户日历数据
 * WRITE_CALENDAR 允许程序写入但不读取用户日历数据
 * READ_CONTACTS 允许程序读取用户联系人数据
 * WRITE_CONTACTS 允许程序写入但不读取用户联系人数据
 * GET_ACCOUNTS 访问一个帐户列表在Accounts Service中
 * android.permission_group.PHONE
 * READ_PHONE_STATE 允许程序访问电话状态
 * CALL_PHONE 允许程序从非系统拨号器里输入电话号码
 * READ_CALL_LOG 允许程序读取用户的联系人数据
 * WRITE_CALL_LOG 允许程序写入(但是不能读)用户的联系人数据
 * ADD_VOICEMAIL 允许应用程序添加语音邮件系统
 * USE_SIP 允许程序使用SIP视频服务
 * PROCESS_OUTGOING_CALLS 允许程序监视、修改有关播出电话
 * android.permission_group.SENSORS
 * BODY_SENSORS 允许人体传感器
 * android.permission-group.SMS
 * SEND_SMS 允许程序发送短信
 * RECEIVE_SMS 允许程序接收短信
 * READ_SMS 允许程序读取短信内容
 * RECEIVE_WAP_PUSH 允许程序接收WAP PUSH信息
 * RECEIVE_MMS 允许程序接收彩信
 * <p>
 */
public abstract class PermissionsPresenter<T extends BaseView> extends BasePresenter<T> {

    private static final int REQUEST_PERMISSIONS = 60;
    private static final Map<String, String> PERMISSIONSHINT = new HashMap<>();
    private static final Set<String> PERMISSIONS = new HashSet<>(1);

    static {
        PERMISSIONSHINT.put(Manifest.permission.CAMERA, "使用照相机");
        PERMISSIONSHINT.put(Manifest.permission.RECORD_AUDIO, "录制音频");
        PERMISSIONSHINT.put(Manifest.permission.READ_EXTERNAL_STORAGE, "读取SD卡");
        PERMISSIONSHINT.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入SD卡");
        PERMISSIONSHINT.put(Manifest.permission.ACCESS_FINE_LOCATION, "使用GPS定位");
        PERMISSIONSHINT.put(Manifest.permission.ACCESS_COARSE_LOCATION, "使用网络定位");
        PERMISSIONSHINT.put(Manifest.permission.READ_CALENDAR, "读取日历");
        PERMISSIONSHINT.put(Manifest.permission.WRITE_CALENDAR, "添加日历");
        PERMISSIONSHINT.put(Manifest.permission.READ_CONTACTS, "读取联系人");
        PERMISSIONSHINT.put(Manifest.permission.WRITE_CONTACTS, "添加联系人");
        PERMISSIONSHINT.put(Manifest.permission.GET_ACCOUNTS, "访问账户列表");
        PERMISSIONSHINT.put(Manifest.permission.READ_PHONE_STATE, "访问电话状态");
        PERMISSIONSHINT.put(Manifest.permission.CALL_PHONE, "拨打电话");
        PERMISSIONSHINT.put(Manifest.permission.READ_CALL_LOG, "读取通讯记录");
        PERMISSIONSHINT.put(Manifest.permission.WRITE_CALL_LOG, "添加通讯记录");
        PERMISSIONSHINT.put(Manifest.permission.ADD_VOICEMAIL, "添加语音邮件");
        PERMISSIONSHINT.put(Manifest.permission.USE_SIP, "使用SIP视频服务");
        PERMISSIONSHINT.put(Manifest.permission.PROCESS_OUTGOING_CALLS, "监视或修改有关播出电话");
        PERMISSIONSHINT.put(Manifest.permission.BODY_SENSORS, "访问人体传感器");
        PERMISSIONSHINT.put(Manifest.permission.SEND_SMS, "发送短信");
        PERMISSIONSHINT.put(Manifest.permission.RECEIVE_SMS, "接收短信");
        PERMISSIONSHINT.put(Manifest.permission.READ_SMS, "读取短信内容");
        PERMISSIONSHINT.put(Manifest.permission.RECEIVE_WAP_PUSH, "接收WAP PUSH信息");
        PERMISSIONSHINT.put(Manifest.permission.RECEIVE_MMS, "接收彩信");
//        PERMISSIONSHINT.put(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, "允许挂载和反挂载文件系统可移动存储");
//        PERMISSIONSHINT.put(Manifest.permission.CHANGE_COMPONENT_ENABLED_STATE, "允许改变组件");

        Field[] fields = Manifest.permission.class.getFields();
        for (Field field : fields) {
            String name = null;
            try {
                name = (String) field.get("");
            } catch (IllegalAccessException e) {
                Log.e("Could not access field", e);
            }
            PERMISSIONS.add(name);
        }
    }

    private List<String> permissionsList = new ArrayList<>();

    @Override
    public void onStart() {
    }

    public synchronized void questManifestPermissions() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("A problem occurred when retrieving permissions", e);
        }
        if (packageInfo != null) {
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions != null && permissions.length > 0) {
                questPermissions(permissions);
                return;
            }
        }
        onPermissionsSuccess(null);
    }

    public void questPermissions(String[] permissions) {
        permissionsList.clear();
        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            Log.d("Manifest contained permission: " + permission);
            // if (PERMISSIONS.contains(permission)) {
            if (PERMISSIONSHINT.containsKey(permission)) {
                // 判断是否有权限
                if (!checkSelfPermission(permission)) {
                    permissionsList.add(permission);
                    // 判断是否需要向用户解释为什么要申请该权限
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                        permissionsNeeded.add(permission);
                    }
                }
            }
        }

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                StringBuilder message = new StringBuilder("程序需要您授予以下权限:");
                for (int i = 0; i < permissionsNeeded.size(); i++) {
                    if (i > 0) {
                        message.append(BaseConstants.COMMA).append(BaseConstants.SPACE);
                    }
                    String permission = permissionsNeeded.get(i);
                    if (PERMISSIONSHINT.containsKey(permission)) {
                        message.append(PERMISSIONSHINT.get(permission));
                    } else {
                        message.append(permission.substring(permission.lastIndexOf(BaseConstants.DOT), permission.length()));
                    }
                }
                showMessageOKCancel(message.toString());
            } else {
                ActivityCompat.requestPermissions(mActivity, permissionsList.toArray(new String[permissionsList.size()]), REQUEST_PERMISSIONS);
            }
        } else {
            onPermissionsSuccess(permissions);
        }
    }

    public boolean checkSelfPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void showMessageOKCancel(String message) {
        // ToastUtils.show(mContext, message);
        new AlertDialog.Builder(mContext).setMessage(message).
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(mActivity, permissionsList.toArray(new String[permissionsList.size()]), REQUEST_PERMISSIONS);
                    }
                }).
                setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPermissionsFailure(null);
                    }
                }).
                setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onPermissionsFailure(null);
                    }
                }).create().show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                boolean result = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        result = false;
                    }
                }
                if (result) {
                    onPermissionsSuccess(permissions);
                } else {
                    onPermissionsFailure("程序所需权限被拒绝.");
                }
                break;
        }
    }

    public abstract void onPermissionsSuccess(String[] permissions);

    public void onPermissionsFailure(String strMsg) {
        Log.e(strMsg);
    }
}
