package xyz.tanwb.airship.rxjava.schedulers;

import android.os.Looper;

import java.util.concurrent.atomic.AtomicReference;

import rx.Scheduler;
import rx.annotations.Experimental;
import xyz.tanwb.airship.rxjava.plugins.RxAndroidPlugins;
import xyz.tanwb.airship.rxjava.plugins.RxAndroidSchedulersHook;

/**
 * Android-specific Schedulers.
 */
public final class AndroidSchedulers {

    private static final AtomicReference<AndroidSchedulers> INSTANCE = new AtomicReference<>();

    private final Scheduler mainThreadScheduler;

    private AndroidSchedulers() {
        RxAndroidSchedulersHook hook = RxAndroidPlugins.getInstance().getSchedulersHook();

        Scheduler main = hook.getMainThreadScheduler();
        if (main != null) {
            mainThreadScheduler = main;
        } else {
            mainThreadScheduler = new LooperScheduler(Looper.getMainLooper());
        }
    }

    private static AndroidSchedulers getInstance() {
        while (true) {
            AndroidSchedulers current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new AndroidSchedulers();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    /**
     * A {@link Scheduler} which executes actions on the Android UI thread.
     */
    public static Scheduler mainThread() {
        return getInstance().mainThreadScheduler;
    }

    /**
     * A {@link Scheduler} which executes actions on {@code looper}.
     */
    public static Scheduler from(Looper looper) {
        if (looper == null) throw new NullPointerException("looper == null");
        return new LooperScheduler(looper);
    }

    /**
     * Resets the current {@link AndroidSchedulers} instance.
     * This will re-init the cached schedulers on the next usage,
     * which can be useful in testing.
     */
    @Experimental
    public static void reset() {
        INSTANCE.set(null);
    }
}
