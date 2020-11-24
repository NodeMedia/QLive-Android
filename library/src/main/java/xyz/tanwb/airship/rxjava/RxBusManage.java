package xyz.tanwb.airship.rxjava;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import xyz.tanwb.airship.rxjava.schedulers.AndroidSchedulers;

public class RxBusManage {

    private Map<String, Observable<?>> mObservables = new HashMap<>();// 管理观察者
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();// 管理订阅者者

    public void on(String eventName, Action1<Object> action1) {
        Observable<?> mObservable = RxBus.getInstance().register(eventName);
        mObservables.put(eventName, mObservable);
        mCompositeSubscription.add(mObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(action1, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        }));
    }

    public boolean isOn(String eventName) {
        return mObservables.containsKey(eventName);
    }

    public void post(Object o) {
        RxBus.getInstance().post(o, o);
    }

    public void post(Object tag, Object content) {
        RxBus.getInstance().post(tag, content);
    }

    public void add(Subscription m) {
        mCompositeSubscription.add(m);
    }

    public void clear() {
        mCompositeSubscription.unsubscribe();// 取消订阅
        for (Map.Entry<String, Observable<?>> entry : mObservables.entrySet()) {
            RxBus.getInstance().unregister(entry.getKey(), entry.getValue());// 移除观察
        }
        mObservables.clear();
    }

}
