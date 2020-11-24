package xyz.tanwb.airship.utils;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.BaseConstants;

public class Log {

    private static String createTag() {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(BaseConstants.DOT) + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        return tag;
    }

    public static void d(String msg) {
        d(msg, (Throwable) null);
    }

    public static void d(String msg, Throwable tr) {
        d(createTag(), msg, tr);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (msg != null && App.isDebug()) {
            android.util.Log.d(tag, msg, tr);
        }
    }

    public static void e(String msg) {
        e(msg, (Throwable) null);
    }

    public static void e(String msg, Throwable tr) {
        e(createTag(), msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (msg != null && App.isDebug()) {
            android.util.Log.e(tag, msg, tr);
        }
    }

    public static void i(String msg) {
        i(msg, (Throwable) null);
    }

    public static void i(String msg, Throwable tr) {
        i(createTag(), msg, tr);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (msg != null && App.isDebug()) {
            android.util.Log.i(tag, msg, tr);
        }
    }

    public static void v(String msg) {
        v(msg, (Throwable) null);
    }

    public static void v(String msg, Throwable tr) {
        v(createTag(), msg, tr);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (msg != null && App.isDebug()) {
            android.util.Log.v(tag, msg, tr);
        }
    }
}
