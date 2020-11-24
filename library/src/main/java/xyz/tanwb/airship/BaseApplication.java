package xyz.tanwb.airship;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

import xyz.tanwb.airship.utils.Log;

/**
 * 程序入口基类
 */
public class BaseApplication extends Application {

    private List<Activity> activityList;

    @Override
    public void onCreate() {
        super.onCreate();
        App.init(this);
        CrashHandler.getInstance().init(this);
    }

    /**
     * 获取最后一个打开的Activity
     */
    public Activity getLastActivity() {
        if (activityList != null && activityList.size() > 0) {
            return activityList.get(activityList.size() - 1);
        }
        return null;
    }

    /**
     * 添加Activity到记录List
     */
    public void addActivity(Activity activity) {
        Log.d("Activity Create：" + activity.getClass().getName());
        if (activityList == null) {
            activityList = new LinkedList<>();
        }
        activityList.add(activity);
    }

    /**
     * 从记录List移除Activity
     */
    public void removeActivity(Activity activity) {
        Log.d("Activity Remove for activity：" + activity.getClass().getName());
        if (activityList == null) return;
        activityList.remove(activity);
    }

    /**
     * 根据Activity名字移除记录
     */
    public void removeActivity(String activityName) {
        Log.d("Activity Remove for activityName：" + activityName);
        if (activityList == null) return;
        for (Activity activity : activityList) {
            if (activity.getClass().getName().equals(activityName)) {
                activity.finish();
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void exit() {
        if (activityList == null) return;
        if (activityList.size() > 0) {
            for (Activity activity : activityList) {
                Log.d("Activit Destroy：" + activity.getClass().getName());
                activity.finish();
            }
            activityList.clear();
        }
    }

    /**
     * 结束标识Activity外的所有Activity
     */
    public void exitOtherActivity(Activity activity) {
        if (activityList == null) {
            activityList = new LinkedList<>();
        }
        if (activityList.size() > 0) {
            for (Activity a : activityList) {
                if (a != activity) {
                    Log.d("Activity ExitOther：" + a.getClass().getName());
                    a.finish();
                }
            }
            activityList.clear();
        }
        activityList.add(activity);
    }

}
