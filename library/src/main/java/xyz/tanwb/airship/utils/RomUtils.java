package xyz.tanwb.airship.utils;

import android.os.Build;
import android.os.Environment;
import androidx.annotation.StringDef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Properties;

public class RomUtils {

    public static final String KEY_VERSION_ID = "ro.build.display.id";

    public static final String KEY_EMUI_VERSION_CODE = "ro.build.hw_emui_api_level";
    public static final String KEY_EMUI_VERSION_NAME = "ro.build.version.emui";

    public static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    public static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    public static final String KEY_MIUI_HANDY_MODE_SF = "ro.miui.has_handy_mode_sf";
    public static final String KEY_MIUI_REAL_BLUR = "ro.miui.has_real_blur";

    public static final String KEY_FLYME_PUBLISHED = "ro.flyme.published";
    public static final String KEY_FLYME_FLYME = "ro.meizu.setupwizard.flyme";
    public static final String KEY_FLYME_ICON = "persist.sys.use.flyme.icon";

    public static final String KEY_OPPO_VERSION = "ro.build.version.opporom";

    public static final String KEY_SMARTISAN_VERSION = "ro.smartisan.version";

    public static final String KEY_VIVO_VERSION = "ro.vivo.os.version";
    public static final String KEY_VIVO_OS_NAME = "ro.vivo.os.name";
    public static final String KEY_VIVO_ROM_VERSION = "ro.vivo.rom.version";

    public static final String KEY_GIONEE_VERSION = "ro.gn.sv.version";
    public static final String KEY_LENOVO_VERSION = "ro.lenovo.lvp.version";

    public static final String ROM_UNKNOWN = "UNKNOWN";//未知Rom
    public static final String ROM_EMUI = "EMUI";//华为
    public static final String ROM_MIUI = "MIUI";//小米
    public static final String ROM_FLYME = "FLYME";//魅族
    public static final String ROM_OPPO = "OPPO";//oppo
    public static final String ROM_VIVO = "VIVO";//Vivo
    public static final String ROM_SMARTISAN = "SMARTISAN";//锤子
    public static final String ROM_QIHU = "QIHU";//奇虎
    public static final String ROM_LENOVO = "LENOVO";//联想
    public static final String ROM_SAMSUNG = "SAMSUNG";//三星

    @StringDef({ROM_UNKNOWN, ROM_EMUI, ROM_MIUI, ROM_FLYME, ROM_OPPO, ROM_SMARTISAN, ROM_VIVO, ROM_QIHU, ROM_LENOVO, ROM_SAMSUNG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RomType {
    }

    private static Properties properties;
    private static String romType;
    private static int systemType = -1;

    private static Properties getProperties() {
        if (properties == null) {
            try {
                properties = new Properties();
                properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));// new File("/system/build.prop"))
            } catch (IOException e) {
                return null;
            }
        }
        return properties;
    }

    public static String getPropertyToWayOne(String key) {
        Properties properties = getProperties();
        if (properties != null && properties.containsKey(key)) {
            // for (Map.Entry<Object, Object> e : properties.entrySet()) {
            // Log.e(e.getKey() + " " + e.getValue());
            // }
            return properties.getProperty(key, Build.UNKNOWN);
        }
        return null;
    }

    public static String getPropertyToWayTwo(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            // Log.e("Unable to read prop " + name, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    /**
     * 获取系统类型
     *
     * @return 0:未知系统 1:EMUI 2:MIUI 3:FLYME 4:OPPO 5:VIVO 6:Smartisan
     */
    public static int getSystemType() {
        if (systemType < 0) {
            initRomInfo();
        }
        return systemType;
    }

    public static String getRomType() {
        if (romType == null) {
            initRomInfo();
        }
        return romType;
    }

    /**
     * 初始化系统信息
     */
    private static void initRomInfo() {
        if (isEMUI()) {
            systemType = 1;
            romType = ROM_EMUI;
        } else if (isMIUI()) {
            systemType = 2;
            romType = ROM_MIUI;
        } else if (isFlyme()) {
            systemType = 3;
            romType = ROM_FLYME;
        } else if (isOPPO()) {
            systemType = 4;
            romType = ROM_OPPO;
        } else if (isVIVO()) {
            systemType = 5;
            romType = ROM_VIVO;
        } else if (isSmartisan()) {
            systemType = 6;
            romType = ROM_SMARTISAN;
        } else {
            systemType = 0;
            romType = ROM_UNKNOWN;
        }
    }

    /**
     * 判断是否是华为系统 EMUI...
     */
    public static boolean isEMUI() {
        String property = getPropertyToWayOne(KEY_EMUI_VERSION_NAME);
        if (property != null) {
            return true;
        }
        property = getPropertyToWayOne(KEY_EMUI_VERSION_CODE);
        if (property != null) {
            return true;
        }
        property = getPropertyToWayOne(KEY_VERSION_ID);
        if (property != null) {
            if (property.contains(ROM_EMUI) || property.toUpperCase().contains(ROM_EMUI)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是小米系统
     */
    public static boolean isMIUI() {
        String property = getPropertyToWayOne(KEY_MIUI_VERSION_NAME);
        if (property != null) {
            return true;
        }
        property = getPropertyToWayOne(KEY_MIUI_VERSION_CODE);
        return property != null;
    }

    /**
     * 判断是否是魅族系统
     */
    public static boolean isFlyme() {
        try {
            Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (Exception ignored) {
        }
        String property = getPropertyToWayOne(KEY_VERSION_ID);//or ro.meizu.product.model
        if (property != null) {
            if (property.contains(ROM_FLYME) || property.toLowerCase().contains(ROM_FLYME)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是OppO系统
     */
    public static boolean isOPPO() {
        String property = getPropertyToWayOne(KEY_OPPO_VERSION);
        if (property != null) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是Vivo系统
     */
    public static boolean isVIVO() {
        String property = getPropertyToWayOne(KEY_VIVO_VERSION);
        if (property != null) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是锤子系统
     */
    public static boolean isSmartisan() {
        String property = getPropertyToWayOne(KEY_SMARTISAN_VERSION);
        if (property != null) {
            return true;
        }
        return false;
    }

    /**
     * 获取设备硬件制造商
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER.toUpperCase();
    }

}