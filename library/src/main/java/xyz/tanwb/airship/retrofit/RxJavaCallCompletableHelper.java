package xyz.tanwb.airship.retrofit;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import rx.Completable;
import rx.Completable.CompletableOnSubscribe;
import rx.Completable.CompletableSubscriber;
import rx.Scheduler;
import rx.Subscription;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import xyz.tanwb.airship.rxjava.schedulers.AndroidSchedulers;

final class RxJavaCallCompletableHelper {

    static CallAdapter<Completable> createCallAdapter(Scheduler scheduler) {
        return new CompletableCallAdapter(scheduler);
    }

    private static final class CompletableCallOnSubscribe implements CompletableOnSubscribe {
        private final Call originalCall;

        CompletableCallOnSubscribe(Call originalCall) {
            this.originalCall = originalCall;
        }

        @Override
        public void call(CompletableSubscriber subscriber) {
            // Since Call is a one-shot type, clone it for each new subscriber.
            final Call call = originalCall.clone();

            // Attempt to cancel the call if it is still in-flight on unsubscription.
            Subscription subscription = Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    call.cancel();
                }
            });
            subscriber.onSubscribe(subscription);

            try {
                Response response = call.execute();
                if (!subscription.isUnsubscribed()) {
                    if (response.isSuccessful()) {
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new HttpException(response));
                    }
                }
            } catch (IOException t) {
                Exceptions.throwIfFatal(t);
                if (!subscription.isUnsubscribed()) {
                    subscriber.onError(t);
                }
            }
        }
    }

    static class CompletableCallAdapter implements CallAdapter<Completable> {
        private final Scheduler scheduler;

        CompletableCallAdapter(Scheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public Type responseType() {
            return Void.class;
        }

        @Override
        public Completable adapt(Call call) {
            Completable completable = Completable.create(new CompletableCallOnSubscribe(call));
            if (scheduler != null) {
                return completable.subscribeOn(scheduler).observeOn(AndroidSchedulers.mainThread());
            }
            return completable;
        }
    }
}
