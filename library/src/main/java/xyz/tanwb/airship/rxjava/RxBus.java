package xyz.tanwb.airship.rxjava;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import xyz.tanwb.airship.rxjava.schedulers.AndroidSchedulers;

public class RxBus {

    private static RxBus instance;

    private ConcurrentHashMap<Object, CopyOnWriteArrayList<Subject>> subjectMapper = new ConcurrentHashMap<>();

    public static synchronized RxBus getInstance() {
        if (null == instance) {
            instance = new RxBus();
        }
        return instance;
    }

    /**
     * 订阅事件源
     */
    public RxBus onEvent(Observable<?> mObservable, Action1<Object> mAction1) {
        mObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(mAction1, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return getInstance();
    }

    /**
     * 注册事件源
     */
    public <T> Observable<T> register(@NonNull Object tag) {
        CopyOnWriteArrayList<Subject> subjectList = subjectMapper.get(tag);
        if (null == subjectList) {
            subjectList = new CopyOnWriteArrayList<Subject>();
            subjectMapper.put(tag, subjectList);
        }
        Subject<T, T> subject = PublishSubject.create();
        subjectList.add(subject);
        return subject;
    }

    /**
     * 取消监听
     */
    public void unregister(@NonNull Object tag) {
        List<Subject> subjects = subjectMapper.get(tag);
        if (isEmpty(subjects)) {
            subjectMapper.remove(tag);
        }
    }

    /**
     * 取消监听
     */
    public RxBus unregister(@NonNull Object tag, @NonNull Observable<?> observable) {
        List<Subject> subjects = subjectMapper.get(tag);
        if (!isEmpty(subjects)) {
            subjects.remove(observable);
            if (isEmpty(subjects)) {
                subjectMapper.remove(tag);
            }
        } else {
            subjectMapper.remove(tag);
        }
        return getInstance();
    }

    /**
     * 触发事件
     */
    public void post(@NonNull Object o) {
        post(o, o);
    }

    /**
     * 触发事件
     */
    public void post(@NonNull Object tag, @NonNull Object content) {
        List<Subject> subjects = subjectMapper.get(tag);
        if (!isEmpty(subjects)) {
            for (Subject subject : subjects) {
                subject.onNext(content);
            }
        }
    }

    public static boolean isEmpty(Collection<Subject> collection) {
        return null == collection || collection.size() == 0;
    }
}
