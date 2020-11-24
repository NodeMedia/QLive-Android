package xyz.tanwb.airship.rxjava;

import rx.Observable;
import rx.schedulers.Schedulers;
import xyz.tanwb.airship.rxjava.schedulers.AndroidSchedulers;

public class RxSchedulers {
    public static <T> Observable.Transformer<T, T> ioToMain() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
