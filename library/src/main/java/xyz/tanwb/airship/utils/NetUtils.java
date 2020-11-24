package xyz.tanwb.airship.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.BaseConstants;

public class NetUtils {

    public static final String NETWORK_TYPE_WIFI = "wifi";
    public static final String NETWORK_TYPE_3G = "3g";
    public static final String NETWORK_TYPE_2G = "2g";
    public static final String NETWORK_TYPE_WAP = "wap";
    public static final String NETWORK_TYPE_DISCONNECT = "disconnect";
    public static final String NETWORK_TYPE_UNKNOWN = "unknown";

    public static NetworkInfo getNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) App.app().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm == null ? null : cm.getActiveNetworkInfo();
    }

    /**
     * 判断是否已经连接或正在连接
     */
    public static boolean isActiveNetwork() {
        NetworkInfo info = getNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    /**
     * 判断网络是否已经连接
     */
    public static boolean isConnected() {
        NetworkInfo info = getNetworkInfo();
        return info != null && info.isConnected() && info.getState() == NetworkInfo.State.CONNECTED;
    }

    /**
     * 获取网络类型
     */
    public static int getNetworkType() {
        NetworkInfo networkInfo = getNetworkInfo();
        return networkInfo == null ? -1 : networkInfo.getType();
    }

    /**
     * 获取网络类型名称
     */
    public static String getNetworkTypeName() {
        int netType = getNetworkType();
        switch (netType) {
            case ConnectivityManager.TYPE_MOBILE:
                String proxyHost = android.net.Proxy.getDefaultHost();
                return TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork() ? NETWORK_TYPE_3G : NETWORK_TYPE_2G) : NETWORK_TYPE_WAP;
            case ConnectivityManager.TYPE_WIFI:
                return NETWORK_TYPE_WIFI;
            case -1:
                return NETWORK_TYPE_DISCONNECT;
            default:
                return NETWORK_TYPE_UNKNOWN;
        }
    }

    /**
     * 是否连接快速移动网络
     */
    private static boolean isFastMobileNetwork() {
        TelephonyManager telephonyManager = (TelephonyManager) App.app().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true;
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    return true;
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return true;
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return true;
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return true;
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    return false;
                default:
                    return false;
            }
        }
        return false;
    }

    private static WifiInfo getWifiInfo() {
        WifiManager wifiManager = (WifiManager) App.app().getSystemService(Context.WIFI_SERVICE);
        return wifiManager == null ? null : wifiManager.getConnectionInfo();
    }

    /**
     * 获取设备的mac地址
     * 这里要特别说明一下,mac地址不是一定能获取的到的,你可能要更优先使用设备ID
     */
    public static String mac() {
        String result = null;
        try {
            String path = "sys/class/net/wlan0/address";
            if ((new File(path)).exists()) {
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[8192];
                int byteCount = fis.read(buffer);
                if (byteCount > 0) {
                    result = new String(buffer, 0, byteCount, BaseConstants.UTF8);
                }
            }
            if (TextUtils.isEmpty(result)) {
                path = "sys/class/net/eth0/address";
                FileInputStream fisName = new FileInputStream(path);
                byte[] bufferName = new byte[8192];
                int byteCountName = fisName.read(bufferName);
                if (byteCountName > 0) {
                    result = new String(bufferName, 0, byteCountName, BaseConstants.UTF8);
                }
            }
            if (TextUtils.isEmpty(result)) {
                result = getWifiInfo().getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取WIFI名称
     */
    public static String getWifiName() {
        WifiInfo wifiInfo = getWifiInfo();
        String wifiName = wifiInfo.getSSID();
        if (wifiName != null) {
            if (!wifiName.contains("<unknown ssid>") && wifiName.length() > 2) {
                if (wifiName.startsWith(BaseConstants.DOUBLE_QUOTES) && wifiName.endsWith(BaseConstants.DOUBLE_QUOTES)) {
                    wifiName = wifiName.subSequence(1, wifiName.length() - 1).toString();
                }
                return wifiName;
            }
        }
        return null;
    }

    /**
     * 获取WIFI IP地址
     */
    public static String getWifiIpAddress() {
        WifiInfo localWifiInfo = getWifiInfo();
        return localWifiInfo != null ? convertIntToIp(localWifiInfo.getIpAddress()) : null;
    }

    /**
     * 格式化IP地址
     */
    private static String convertIntToIp(int paramInt) {
        return (paramInt & 0xFF) + BaseConstants.DOT + (0xFF & paramInt >> 8) + BaseConstants.DOT + (0xFF & paramInt >> 16) + BaseConstants.DOT + (0xFF & paramInt >> 24);
    }

}
