package xyz.tanwb.airship.retrofit;

import retrofit2.Response;
import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * A version of {@link Observable#map(Func1)} which lets us trigger {@code onError} without having
 * to use {@link Observable#flatMap(Func1)} which breaks producer requests from propagating.
 */
final class RxJavaCallOperator<T> implements Operator<T, Response<T>> {

    private static final RxJavaCallOperator<Object> INSTANCE = new RxJavaCallOperator<>();

    // Safe because of erasure.
    static <R> RxJavaCallOperator<R> instance() {
        return (RxJavaCallOperator<R>) INSTANCE;
    }

    @Override
    public Subscriber<? super Response<T>> call(final Subscriber<? super T> child) {
        return new Subscriber<Response<T>>(child) {
            @Override
            public void onNext(Response<T> response) {
                if (response.isSuccessful()) {
                    child.onNext(response.body());
                } else {
                    child.onError(new HttpException(response));
                }
            }

            @Override
            public void onCompleted() {
                child.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }
        };
    }
}
