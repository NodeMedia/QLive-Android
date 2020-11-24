package xyz.tanwb.airship.rxjava;

import android.os.HandlerThread;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import xyz.tanwb.airship.rxjava.schedulers.AndroidSchedulers;

public class RxJavaManagerImpl implements RxJavaManager {

    @Override
    public void create() {
        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();

        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                // 根据观察者索引对象向观察者发送数据
                subscriber.onNext("发送测试消息");
                // 数据发送完成
                subscriber.onCompleted();
                // 解绑观察者
                if (!subscriber.isUnsubscribed()) {
                    subscriber.unsubscribe();
                }
            }
        }).subscribeOn(AndroidSchedulers.from(backgroundThread.getLooper())).observeOn(AndroidSchedulers.mainThread());

        observable.subscribe(new Subscriber<String>() {

            @Override
            public void onCompleted() {
                // Log.e("Sequence complete");
            }

            @Override
            public void onError(Throwable error) {
                // Log.e("Error encountered: " + error.getMessage());
            }

            @Override
            public void onNext(String s) {
                // Log.e(s);
            }
        });
    }

    @Override
    public void from() {
        Integer[] items = {0, 1, 2, 3, 4, 5};
        Observable<Integer> observable = Observable.from(items);

        observable.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer item) {
                // Log.e(item);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                // Log.e("Error encountered: " + error.getMessage());
            }
        }, new Action0() {
            @Override
            public void call() {
                // Log.e("Sequence complete");
            }
        });
    }

    @Override
    public void just() {
        Observable.just(1, 2, 3).subscribe(new Subscriber<Integer>() {
            @Override
            public void onNext(Integer item) {
                // Log.e(item);
            }

            @Override
            public void onError(Throwable error) {
                // Log.e("Error: " + error.getMessage());
            }

            @Override
            public void onCompleted() {
                // Log.e("Sequence complete.");
            }
        });
    }

    @Override
    public void defer() {
        Observable<Integer> deferObservable = Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.just(1);
            }
        });

        deferObservable.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer item) {
                // Log.e(item);
            }
        });
    }

    @Override
    public void timer() {
        Observable.timer(2, TimeUnit.SECONDS).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                // Log.e("Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                // Log.e("error:" + e.getMessage());
            }

            @Override
            public void onNext(Long aLong) {
                // Log.e("Next:" + aLong.toString());
            }
        });
    }

    @Override
    public void interval() {
        Observable.interval(2, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                // Log.e("Next:" + aLong.toString());
            }
        });
    }

    @Override
    public void range() {
        Observable.range(3, 10).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                // Log.e("Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                // Log.e("error:" + e.getMessage());
            }

            @Override
            public void onNext(Integer i) {
                //输出3、4、5…12的一组数字
                // Log.e("Next:" + i.toString());
            }
        });
    }

    @Override
    public void repeat() {
        Observable.range(3, 3).repeat(2).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                // Log.e("Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                // Log.e("error:" + e.getMessage());
            }

            @Override
            public void onNext(Integer i) {
                ///连续输出两组(3,4,5)的数字
                // Log.e("Next:" + i.toString());
            }
        });
    }

    @Override
    public void start() {
    }

    @Override
    public void emptyOrNeverOrThrow() {
        Observable.empty().subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {

            }
        });

        Observable.never().subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {

            }
        });
    }

    // //repeatWhen操作符是对某一个Observable，有条件地重新订阅从而产生多次结果
    // private void repeatWhenUse() {
    // Observable.range(3, 3).repeatWhen(new Func1<Observable<Integer>,
    // Observable<Integer>>() {
    //
    // @Override
    // public Observable<Integer> call(Observable<Integer> integer) {
    // return Observable.just(integer);
    // }
    // }).subscribe(new Action1<Integer>() {
    // @Override
    // public void call(Integer integer) {
    // // Log.e("Next:" + integer.toString());
    // }
    // });
    // }
    //
    // //buffer操作符周期性地收集源Observable产生的结果到列表中，并把这个列表提交给订阅者，订阅者处理后，清空buffer列表，同时接收下一次收集的结果并提交给订阅者，周而复始。
    // private void bufferUse() {
    // //定义邮件内容
    // final String[] mails = new String[]{"Here is an email!", "Another
    // email!", "Yet another email!"};
    // //每隔1秒就随机发布一封邮件
    // Observable<String> endlessMail = Observable.create(new
    // Observable.OnSubscribe<String>() {
    // @Override
    // public void call(Subscriber<? super String> subscriber) {
    // try {
    // if (subscriber.isUnsubscribed()) return;
    // Random random = new Random();
    // while (true) {
    // String mail = mails[random.nextInt(mails.length)];
    // subscriber.onNext(mail);
    // Thread.sleep(1000);
    // }
    // } catch (Exception ex) {
    // subscriber.onError(ex);
    // }
    // }
    // }).subscribeOn(Schedulers.io());
    // //把上面产生的邮件内容缓存到列表中，并每隔3秒通知订阅者
    // endlessMail.buffer(3, TimeUnit.SECONDS).subscribe(new
    // Action1<List<String>>() {
    // @Override
    // public void call(List<String> list) {
    // // Log.e(String.format("You've got %d new messages! Here they
    // are!", list.size()));
    // for (int i = 0; i < list.size(); i++)
    // // Log.e("**" + list.get(i).toString());
    // }
    // });
    // }
    //
    // //flatMap操作符是把Observable产生的结果转换成多个Observable，然后把这多个Observable“扁平化”成一个Observable，并依次提交产生的结果给订阅者。
    // private void flatMap(Context context) {
    // Observable.just(context.getExternalCacheDir())
    // .flatMap(new Func1<File, Observable<File>>() {
    // @Override
    // public Observable<File> call(File file) {
    // //参数file是just操作符产生的结果，这里判断file是不是目录文件，如果是目录文件，则递归查找其子文件flatMap操作符神奇的地方在于，返回的结果还是一个Observable，而这个Observable其实是包含多个文件的Observable的，输出应该是ExternalCacheDir下的所有文件
    // if (file.isDirectory()) {
    // return Observable.from(file.listFiles()).flatMap(new Func1<File,
    // Observable<File>>() {
    // @Override
    // public Observable<File> call(File file) {
    // return Observable.just(file);
    // }
    // });
    // } else {
    // return Observable.just(file);
    // }
    // }
    // })
    // .subscribe(new Action1<File>() {
    // @Override
    // public void call(File file) {
    // // Log.e(file.getAbsolutePath());
    // }
    // });
    // }
    //
    // //concatMap都是把Observable产生的结果转换成多个Observable，然后把这多个Observable“扁平化”成一个Observable，并依次提交产生的结果给订阅者。
    // private void cnncatMap(Context context) {
    // Observable.just(context.getExternalCacheDir())
    // .concatMap(new Func1<File, Observable<File>>() {
    // @Override
    // public Observable<File> call(File file) {
    // //参数file是just操作符产生的结果，这里判断file是不是目录文件，如果是目录文件，则递归查找其子文件flatMap操作符神奇的地方在于，返回的结果还是一个Observable，而这个Observable其实是包含多个文件的Observable的，输出应该是ExternalCacheDir下的所有文件
    // if (file.isDirectory()) {
    // return Observable.from(file.listFiles()).flatMap(new Func1<File,
    // Observable<File>>() {
    // @Override
    // public Observable<File> call(File file) {
    // return Observable.just(file);
    // }
    // });
    // } else {
    // return Observable.just(file);
    // }
    // }
    // })
    // .subscribe(new Action1<File>() {
    // @Override
    // public void call(File file) {
    // // Log.e(file.getAbsolutePath());
    // }
    // });
    // }
    //
    // //switchMap操作符与flatMap操作符类似，都是把Observable产生的结果转换成多个Observable，然后把这多个Observable“扁平化”成一个Observable，并依次提交产生的结果给订阅者。
    // //与flatMap操作符不同的是，switchMap操作符会保存最新的Observable产生的结果而舍弃旧的结果，举个例子来说，比如源Observable产生A、B、C三个结果，通过switchMap的自定义映射规则，映射后应该会产生A1、A2、B1、B2、C1、C2，但是在产生B2的同时，C1已经产生了，这样最后的结果就变成A1、A2、B1、C1、C2，B2被舍弃掉了！
    // private void switchMapUse() {
    // //flatMap操作符的运行结果
    // Observable.just(10, 20, 30).flatMap(new Func1<Integer,
    // Observable<Integer>>() {
    // @Override
    // public Observable<Integer> call(Integer integer) {
    // //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
    // int delay = 200;
    // if (integer > 10)
    // delay = 180;
    //
    // return Observable.from(new Integer[]{integer, integer / 2}).delay(delay,
    // TimeUnit.MILLISECONDS);
    // }
    // }).observeOn(AndroidSchedulers.mainThread()).subscribe(new
    // Action1<Integer>() {
    // @Override
    // public void call(Integer integer) {
    // // Log.e("flatMap Next:" + integer);
    // }
    // });
    //
    // //concatMap操作符的运行结果
    // Observable.just(10, 20, 30).concatMap(new Func1<Integer,
    // Observable<Integer>>() {
    // @Override
    // public Observable<Integer> call(Integer integer) {
    // //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
    // int delay = 200;
    // if (integer > 10)
    // delay = 180;
    //
    // return Observable.from(new Integer[]{integer, integer / 2}).delay(delay,
    // TimeUnit.MILLISECONDS);
    // }
    // }).observeOn(AndroidSchedulers.mainThread()).subscribe(new
    // Action1<Integer>() {
    // @Override
    // public void call(Integer integer) {
    // // Log.e("concatMap Next:" + integer);
    // }
    // });
    //
    // //switchMap操作符的运行结果
    // Observable.just(10, 20, 30).switchMap(new Func1<Integer,
    // Observable<Integer>>() {
    // @Override
    // public Observable<Integer> call(Integer integer) {
    // //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
    // int delay = 200;
    // if (integer > 10)
    // delay = 180;
    //
    // return Observable.from(new Integer[]{integer, integer / 2}).delay(delay,
    // TimeUnit.MILLISECONDS);
    // }
    // }).observeOn(AndroidSchedulers.mainThread()).subscribe(new
    // Action1<Integer>() {
    // @Override
    // public void call(Integer integer) {
    // // Log.e("switchMap Next:" + integer);
    // }
    // });
    //
    //// 运行结果如下：
    //// flatMap Next:20
    //// flatMap Next:10
    //// flatMap Next:30
    //// flatMap Next:15
    //// flatMap Next:10
    //// flatMap Next:5
    //// switchMap Next:30
    //// switchMap Next:15
    //// concatMap Next:10
    //// concatMap Next:5
    //// concatMap Next:20
    //// concatMap Next:10
    //// concatMap Next:30
    //// concatMap Next:15
    // }
    //
    // //groupBy操作符是对源Observable产生的结果进行分组，形成一个类型为GroupedObservable的结果集，GroupedObservable中存在一个方法为getKey()，可以通过该方法获取结果集的Key值（类似于HashMap的key)。
    // private void groupByUse() {
    // Observable.interval(1, TimeUnit.SECONDS).take(10).groupBy(new Func1<Long,
    // Long>() {
    // @Override
    // public Long call(Long value) {
    // //按照key为0,1,2分为3组
    // return value % 3;
    // }
    // }).subscribe(new Action1<GroupedObservable<Long, Long>>() {
    // @Override
    // public void call(final GroupedObservable<Long, Long> result) {
    // result.subscribe(new Action1<Long>() {
    // @Override
    // public void call(Long value) {
    // // Log.e("key:" + result.getKey() + ", value:" + value);
    // }
    // });
    // }
    // });
    //
    //// 运行结果如下：
    //// key:0, value:0
    //// key:1, value:1
    //// key:2, value:2
    //// key:0, value:3
    //// key:1, value:4
    //// key:2, value:5
    //// key:0, value:6
    //// key:1, value:7
    //// key:2, value:8
    //// key:0, value:9
    // }

    /**
     * map操作符 把源Observable产生的结果，通过映射规则转换成另一个结果集，并提交给订阅者进行处理。
     */
    private void mapUse() {
        Observable.just(1, 2, 3, 4, 5, 6).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                // 对源Observable产生的结果，都统一乘以3处理
                return integer * 3;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                // Log.e("next:" + integer);
            }
        });
    }

    /**
     * cast操作符
     * 类似于map操作符，不同的地方在于map操作符可以通过自定义规则，把一个值A1变成另一个值A2，A1和A2的类型可以一样也可以不一样；
     * 而cast操作符主要是做类型转换的，传入参数为类型class，如果源Observable产生的结果不能转成指定的class，
     * 则会抛出ClassCastException运行时异常。
     */
    private void castMap() {
        Observable.just(1, 2, 3, 4, 5, 6).cast(Integer.class).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer value) {
                // Log.e("next:" + value);
            }
        });
    }

    // //scan操作符通过遍历源Observable产生的结果，依次对每一个结果项按照指定规则进行运算，计算后的结果作为下一个迭代项参数，每一次迭代项都会把计算结果输出给订阅者。
    // private void scanMap() {
    // Observable.just(1, 2, 3, 4, 5).scan(new Func2<Integer, Integer,
    // Integer>() {
    // @Override
    // public Integer call(Integer sum, Integer item) {
    // //参数sum就是上一次的计算结果
    // return sum + item;
    // }
    // }).subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    //// 运行结果如下：
    //// Next: 1
    //// Next: 3
    //// Next: 6
    //// Next: 10
    //// Next: 15
    // }
    //
    // //window操作符非常类似于buffer操作符，区别在于buffer操作符产生的结果是一个List缓存，而window操作符产生的结果是一个Observable，订阅者可以对这个结果Observable重新进行订阅处理。
    // private void windowUse() {
    // Observable.interval(1, TimeUnit.SECONDS).take(12)
    // .window(3, TimeUnit.SECONDS)
    // .subscribe(new Action1<Observable<Long>>() {
    // @Override
    // public void call(Observable<Long> observable) {
    // // Log.e("subdivide begin......");
    // observable.subscribe(new Action1<Long>() {
    // @Override
    // public void call(Long aLong) {
    // // Log.e("Next:" + aLong);
    // }
    // });
    // }
    // });
    // }
    //
    // //debounce操作符对源Observable每产生一个结果后，如果在规定的间隔时间内没有别的结果产生，则把这个结果提交给订阅者处理，否则忽略该结果。
    // private void debounceUse() {
    // Observable.create(new Observable.OnSubscribe<Integer>() {
    // @Override
    // public void call(Subscriber<? super Integer> subscriber) {
    // if (subscriber.isUnsubscribed()) return;
    // try {
    // //产生结果的间隔时间分别为100、200、300...900毫秒
    // for (int i = 1; i < 10; i++) {
    // subscriber.onNext(i);
    // Thread.sleep(i * 100);
    // }
    // subscriber.onCompleted();
    // } catch (Exception e) {
    // subscriber.onError(e);
    // }
    // }
    // }).subscribeOn(Schedulers.newThread())
    // .debounce(400, TimeUnit.MILLISECONDS) //超时时间为400毫秒
    // .subscribe(
    // new Action1<Integer>() {
    // @Override
    // public void call(Integer integer) {
    // // Log.e("Next:" + integer);
    // }
    // }, new Action1<Throwable>() {
    // @Override
    // public void call(Throwable throwable) {
    // // Log.e("Error:" + throwable.getMessage());
    // }
    // }, new Action0() {
    // @Override
    // public void call() {
    // // Log.e("completed!");
    // }
    // });
    // }
    //
    // //distinct操作符对源Observable产生的结果进行过滤，把重复的结果过滤掉，只输出不重复的结果给订阅者，非常类似于SQL里的distinct关键字。
    // private void distinctUse() {
    // Observable.just(1, 2, 1, 1, 2, 3)
    // .distinct()
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //elementAt操作符在源Observable产生的结果中，仅仅把指定索引的结果提交给订阅者，索引是从0开始的。
    // private void elementAtUse() {
    // Observable.just(1, 2, 3, 4, 5, 6).elementAt(2)
    // .subscribe(
    // new Action1<Integer>() {
    // @Override
    // public void call(Integer integer) {
    // // Log.e("Next:" + integer);
    // }
    // }, new Action1<Throwable>() {
    // @Override
    // public void call(Throwable throwable) {
    // // Log.e("Error:" + throwable.getMessage());
    // }
    // }, new Action0() {
    // @Override
    // public void call() {
    // // Log.e("completed!");
    // }
    // });
    // }
    //
    // //filter操作符是对源Observable产生的结果按照指定条件进行过滤，只有满足条件的结果才会提交给订阅者
    // private void filterUse() {
    // Observable.just(1, 2, 3, 4, 5)
    // .filter(new Func1<Integer, Boolean>() {
    // @Override
    // public Boolean call(Integer item) {
    // return (item < 4);
    // }
    // }).subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //ofType操作符类似于filter操作符，区别在于ofType操作符是按照类型对结果进行过滤
    // private void opTypeUse() {
    // Observable.just(1, "hello world", true, 200L, 0.23f)
    // .ofType(Float.class)
    // .subscribe(new Subscriber<Object>() {
    // @Override
    // public void onNext(Object item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //first操作符是把源Observable产生的结果的第一个提交给订阅者，first操作符可以使用elementAt(0)和take(1)替代。
    // private void firstUse() {
    // Observable.just(1, 2, 3, 4, 5, 6, 7, 8)
    // .first()
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //single操作符是对源Observable的结果进行判断，如果产生的结果满足指定条件的数量不为1，则抛出异常，否则把满足条件的结果提交给订阅者
    // private void singleUse() {
    // Observable.just(1, 2, 3, 4, 5, 6, 7, 8)
    // .single(new Func1<Integer, Boolean>() {
    // @Override
    // public Boolean call(Integer integer) {
    // //取大于10的第一个数字
    // return integer > 10;
    // }
    // })
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //last操作符把源Observable产生的结果的最后一个提交给订阅者，last操作符可以使用takeLast(1)替代。
    // private void lastUse() {
    // Observable.just(1, 2, 3)
    // .last()
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //ignoreElements操作符忽略所有源Observable产生的结果，只把Observable的onCompleted和onError事件通知给订阅者。ignoreElements操作符适用于不太关心Observable产生的结果，只是在Observable结束时(onCompleted)或者出现错误时能够收到通知。
    // private void ignoreElementsUse() {
    // Observable.just(1, 2, 3, 4, 5, 6, 7, 8).ignoreElements()
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //sample操作符定期扫描源Observable产生的结果，在指定的时间间隔范围内对源Observable产生的结果进行采样
    // private void sampleUse() {
    // Observable.create(new Observable.OnSubscribe<Integer>() {
    // @Override
    // public void call(Subscriber<? super Integer> subscriber) {
    // if (subscriber.isUnsubscribed()) return;
    // try {
    // //前8个数字产生的时间间隔为1秒，后一个间隔为3秒
    // for (int i = 1; i < 9; i++) {
    // subscriber.onNext(i);
    // Thread.sleep(1000);
    // }
    // Thread.sleep(2000);
    // subscriber.onNext(9);
    // subscriber.onCompleted();
    // } catch (Exception e) {
    // subscriber.onError(e);
    // }
    // }
    // }).subscribeOn(Schedulers.newThread())
    // .sample(2200, TimeUnit.MILLISECONDS) //采样间隔时间为2200毫秒
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //skip操作符针对源Observable产生的结果，跳过前面n个不进行处理，而把后面的结果提交给订阅者处理
    // private void skipUse() {
    // Observable.just(1, 2, 3, 4, 5, 6, 7).skip(3)
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //skipLast操作符针对源Observable产生的结果，忽略Observable最后产生的n个结果，而把前面产生的结果提交给订阅者处理
    // private void skipLastUse() {
    // Observable.just(1, 2, 3, 4, 5, 6, 7).skipLast(3)
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //take操作符是把源Observable产生的结果，提取前面的n个提交给订阅者，而忽略后面的结果
    // private void takeUse() {
    // Observable.just(1, 2, 3, 4, 5, 6, 7, 8)
    // .take(4)
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //takeFirst操作符类似于take操作符，同时也类似于first操作符，都是获取源Observable产生的结果列表中符合指定条件的前一个或多个，与first操作符不同的是，first操作符如果获取不到数据，则会抛出NoSuchElementException异常，而takeFirst则会返回一个空的Observable，该Observable只有onCompleted通知而没有onNext通知。
    // private void takeFirstUse() {
    // Observable.just(1, 2, 3, 4, 5, 6, 7).takeFirst(new Func1<Integer,
    // Boolean>() {
    // @Override
    // public Boolean call(Integer integer) {
    // //获取数值大于3的数据
    // return integer > 3;
    // }
    // })
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //takeLast操作符是把源Observable产生的结果的后n项提交给订阅者，提交时机是Observable发布onCompleted通知之时。
    // private void takeLast() {
    // Observable.just(1, 2, 3, 4, 5, 6, 7).takeLast(2)
    // .subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onNext(Integer item) {
    // // Log.e("Next: " + item);
    // }
    //
    // @Override
    // public void onError(Throwable error) {
    // // Log.e("Error: " + error.getMessage());
    // }
    //
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    // });
    // }
    //
    // //combineLatest操作符把两个Observable产生的结果进行合并，合并的结果组成一个新的Observable。
    // private void combineLatestUse() {
    // //产生0,5,10,15,20数列
    // Observable<Long> observable1 = Observable.timer(0, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 5;
    // }
    // }).take(5);
    //
    // //产生0,10,20,30,40数列
    // Observable<Long> observable2 = Observable.timer(500, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 10;
    // }
    // }).take(5);
    //
    //
    // Observable.combineLatest(observable1, observable2, new Func2<Long, Long,
    // Long>() {
    // @Override
    // public Long call(Long aLong, Long aLong2) {
    // return aLong + aLong2;
    // }
    // }).subscribe(new Subscriber<Long>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Long aLong) {
    // // Log.e("Next: " + aLong);
    // }
    // });
    // }
    //
    // //join操作符把类似于combineLatest操作符，也是两个Observable产生的结果进行合并，合并的结果组成一个新的Observable，但是join操作符可以控制每个Observable产生结果的生命周期，在每个结果的生命周期内，可以与另一个Observable产生的结果按照一定的规则进行合并
    // private void joinUse() {
    // //产生0,5,10,15,20数列
    // Observable<Long> observable1 = Observable.timer(0, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 5;
    // }
    // }).take(5);
    //
    // //产生0,10,20,30,40数列
    // Observable<Long> observable2 = Observable.timer(500, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 10;
    // }
    // }).take(5);
    //
    // observable1.join(observable2, new Func1<Long, Observable<Long>>() {
    // @Override
    // public Observable<Long> call(Long aLong) {
    // //使Observable延迟600毫秒执行
    // return Observable.just(aLong).delay(600, TimeUnit.MILLISECONDS);
    // }
    // }, new Func1<Long, Observable<Long>>() {
    // @Override
    // public Observable<Long> call(Long aLong) {
    // //使Observable延迟600毫秒执行
    // return Observable.just(aLong).delay(600, TimeUnit.MILLISECONDS);
    // }
    // }, new Func2<Long, Long, Long>() {
    // @Override
    // public Long call(Long aLong, Long aLong2) {
    // return aLong + aLong2;
    // }
    // }).subscribe(new Subscriber<Long>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Long aLong) {
    // // Log.e("Next: " + aLong);
    // }
    // });
    // }
    //
    // //groupJoin操作符非常类似于join操作符，区别在于join操作符中第四个参数的传入函数不一致
    // private void groupJoin() {
    // Observable<Long> observable1 = Observable.timer(0, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 5;
    // }
    // }).take(5);
    //
    // //产生0,10,20,30,40数列
    // Observable<Long> observable2 = Observable.timer(500, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 10;
    // }
    // }).take(5);
    //
    // observable1.groupJoin(observable2, new Func1<Long, Observable<Long>>() {
    // @Override
    // public Observable<Long> call(Long aLong) {
    // return Observable.just(aLong).delay(1600, TimeUnit.MILLISECONDS);
    // }
    // }, new Func1<Long, Observable<Long>>() {
    // @Override
    // public Observable<Long> call(Long aLong) {
    // return Observable.just(aLong).delay(600, TimeUnit.MILLISECONDS);
    // }
    // }, new Func2<Long, Observable<Long>, Observable<Long>>() {
    // @Override
    // public Observable<Long> call(final Long aLong, Observable<Long>
    // observable) {
    // return observable.map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong2) {
    // return aLong + aLong2;
    // }
    // });
    // }
    // }).subscribe(new Subscriber<Observable<Long>>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Observable<Long> observable) {
    // observable.subscribe(new Subscriber<Long>() {
    // @Override
    // public void onCompleted() {
    //
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    //
    // }
    //
    // @Override
    // public void onNext(Long aLong) {
    // // Log.e("Next: " + aLong);
    // }
    // });
    // }
    // });
    // }
    //
    // //merge操作符是按照两个Observable提交结果的时间顺序，对Observable进行合并，如ObservableA每隔500毫秒产生数据为0,5,10,15,20；而ObservableB每隔500毫秒产生数据0,10,20,30,40，其中第一个数据延迟500毫秒产生，最后合并结果为：0,0,5,10,10,20,15,30,20,40;
    // private void mergeUse() {
    // //产生0,5,10,15,20数列
    // Observable<Long> observable1 = Observable.timer(0, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 5;
    // }
    // }).take(5);
    //
    // //产生0,10,20,30,40数列
    // Observable<Long> observable2 = Observable.timer(500, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 10;
    // }
    // }).take(5);
    //
    // Observable.merge(observable1, observable2)
    // .subscribe(new Subscriber<Long>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Long aLong) {
    // // Log.e("Next:" + aLong);
    // }
    // });
    //// Next:0
    //// Next:0
    //// Next:5
    //// Next:10
    //// Next:10
    //// Next:20
    //// Next:15
    //// Next:30
    //// Next:20
    //// Next:40
    // }
    //
    // //mergeDelayError操作符
    // 从merge操作符的流程图可以看出，一旦合并的某一个Observable中出现错误，就会马上停止合并，并对订阅者回调执行onError方法，而mergeDelayError操作符会把错误放到所有结果都合并完成之后才执行
    // private void mergeDelayErrorUse() {
    // //产生0,5,10数列,最后会产生一个错误
    // Observable<Long> errorObservable = Observable.error(new Exception("this
    // is end!"));
    // Observable<Long> observable1 = Observable.timer(0, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 5;
    // }
    // }).take(3).mergeWith(errorObservable.delay(3500, TimeUnit.MILLISECONDS));
    //
    // //产生0,10,20,30,40数列
    // Observable<Long> observable2 = Observable.timer(500, 1000,
    // TimeUnit.MILLISECONDS)
    // .map(new Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 10;
    // }
    // }).take(5);
    //
    // Observable.mergeDelayError(observable1, observable2)
    // .subscribe(new Subscriber<Long>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Long aLong) {
    // // Log.e("Next:" + aLong);
    // }
    // });
    // }
    //
    // //startWith操作符是在源Observable提交结果之前，插入指定的某些数据
    // private void startWithUse() {
    // Observable.just(10, 20, 30).startWith(2, 3, 4).subscribe(new
    // Subscriber<Integer>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Integer value) {
    // // Log.e("Next:" + value);
    // }
    // });
    // }
    //
    // //switchOnNext操作符是把一组Observable转换成一个Observable，转换规则为：对于这组Observable中的每一个Observable所产生的结果，如果在同一个时间内存在两个或多个Observable提交的结果，只取最后一个Observable提交的结果给订阅者
    // private void switchOnNextUse() {
    // //每隔500毫秒产生一个observable
    // Observable<Observable<Long>> observable = Observable.timer(0, 500,
    // TimeUnit.MILLISECONDS).map(new Func1<Long, Observable<Long>>() {
    // @Override
    // public Observable<Long> call(Long aLong) {
    // //每隔200毫秒产生一组数据（0,10,20,30,40)
    // return Observable.timer(0, 200, TimeUnit.MILLISECONDS).map(new
    // Func1<Long, Long>() {
    // @Override
    // public Long call(Long aLong) {
    // return aLong * 10;
    // }
    // }).take(5);
    // }
    // }).take(2);
    //
    // Observable.switchOnNext(observable).subscribe(new Subscriber<Long>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Long aLong) {
    // // Log.e("Next:" + aLong);
    // }
    // });
    // }
    //
    // //zip操作符是把两个observable提交的结果，严格按照顺序进行合并
    // private void zipUse() {
    // Observable<Integer> observable1 = Observable.just(10, 20, 30);
    // Observable<Integer> observable2 = Observable.just(4, 8, 12, 16);
    // Observable.zip(observable1, observable2, new Func2<Integer, Integer,
    // Integer>() {
    // @Override
    // public Integer call(Integer integer, Integer integer2) {
    // return integer + integer2;
    // }
    // }).subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Integer value) {
    // // Log.e("Next:" + value);
    // }
    // });
    // }
    //
    // //onErrorReturn操作符是在Observable发生错误或异常的时候（即将回调oError方法时），拦截错误并执行指定的逻辑，返回一个跟源Observable相同类型的结果，最后回调订阅者的onComplete方法
    // private void onErrorReturn() {
    // Observable<Integer> observable = Observable.create(new
    // Observable.OnSubscribe<Integer>() {
    // @Override
    // public void call(Subscriber<? super Integer> subscriber) {
    // if (subscriber.isUnsubscribed()) return;
    // //循环输出数字
    // try {
    // for (int i = 0; i < 10; i++) {
    // if (i == 4) {
    // throw new Exception("this is number 4 error！");
    // }
    // subscriber.onNext(i);
    // }
    // subscriber.onCompleted();
    // } catch (Exception e) {
    // subscriber.onError(e);
    // }
    // }
    // });
    //
    // observable.onErrorReturn(new Func1<Throwable, Integer>() {
    // @Override
    // public Integer call(Throwable throwable) {
    // return 1004;
    // }
    // }).subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Integer value) {
    // // Log.e("Next:" + value);
    // }
    // });
    // }
    //
    // //onErrorResumeNext操作符跟onErrorReturn类似，只不过onErrorReturn只能在错误或异常发生时只返回一个和源Observable相同类型的结果，而onErrorResumeNext操作符是在错误或异常发生时返回一个Observable，也就是说可以返回多个和源Observable相同类型的结果
    // private void onErrorEwsumeNextUse() {
    // Observable<Integer> observable = Observable.create(new
    // Observable.OnSubscribe<Integer>() {
    // @Override
    // public void call(Subscriber<? super Integer> subscriber) {
    // if (subscriber.isUnsubscribed()) return;
    // //循环输出数字
    // try {
    // for (int i = 0; i < 10; i++) {
    // if (i == 4) {
    // throw new Exception("this is number 4 error！");
    // }
    // subscriber.onNext(i);
    // }
    // subscriber.onCompleted();
    // } catch (Exception e) {
    // subscriber.onError(e);
    // }
    // }
    // });
    //
    // observable.onErrorResumeNext(new Func1<Throwable, Observable<? extends
    // Integer>>() {
    // @Override
    // public Observable<? extends Integer> call(Throwable throwable) {
    // return Observable.just(100, 101, 102);
    // }
    // }).subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Integer value) {
    // // Log.e("Next:" + value);
    // }
    // });
    // }
    //
    // //onExceptionResumeNext操作符和onErrorResumeNext操作符类似，不同的地方在于onErrorResumeNext操作符是当Observable发生错误或异常时触发，而onExceptionResumeNext是当Observable发生异常时才触发。
    // private void onExceptionResumeNextUse() {
    // Observable<Integer> observable = Observable.create(new
    // Observable.OnSubscribe<Integer>() {
    // @Override
    // public void call(Subscriber<? super Integer> subscriber) {
    // if (subscriber.isUnsubscribed()) return;
    // //循环输出数字
    // try {
    // for (int i = 0; i < 10; i++) {
    // if (i == 4) {
    // throw new Exception("this is number 4 error！");
    // }
    // subscriber.onNext(i);
    // }
    // subscriber.onCompleted();
    // } catch (Throwable e) {
    // subscriber.onError(e);
    // }
    // }
    // });
    //
    // observable.onExceptionResumeNext(Observable.just(100, 101,
    // 102)).subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Integer value) {
    // // Log.e("Next:" + value);
    // }
    // });
    // }
    //
    // //retry操作符是当Observable发生错误或者异常时，重新尝试执行Observable的逻辑，如果经过n次重新尝试执行后仍然出现错误或者异常，则最后回调执行onError方法；当然如果源Observable没有错误或者异常出现，则按照正常流程执行
    // private void retryUse() {
    // Observable<Integer> observable = Observable.create(new
    // Observable.OnSubscribe<Integer>() {
    // @Override
    // public void call(Subscriber<? super Integer> subscriber) {
    // if (subscriber.isUnsubscribed()) return;
    // //循环输出数字
    // try {
    // for (int i = 0; i < 10; i++) {
    // if (i == 4) {
    // throw new Exception("this is number 4 error！");
    // }
    // subscriber.onNext(i);
    // }
    // subscriber.onCompleted();
    // } catch (Throwable e) {
    // subscriber.onError(e);
    // }
    // }
    // });
    //
    // observable.retry(2).subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Integer value) {
    // // Log.e("Next:" + value);
    // }
    // });
    // }
    //
    // //retryWhen操作符类似于retry操作符，都是在源observable出现错误或者异常时，重新尝试执行源observable的逻辑，不同在于retryWhen操作符是在源Observable出现错误或者异常时，通过回调第二个Observable来判断是否重新尝试执行源Observable的逻辑，如果第二个Observable没有错误或者异常出现，则就会重新尝试执行源Observable的逻辑，否则就会直接回调执行订阅者的onError方法。
    // private void retryWhenUse() {
    // Observable<Integer> observable = Observable.create(new
    // Observable.OnSubscribe<Integer>() {
    // @Override
    // public void call(Subscriber<? super Integer> subscriber) {
    // // Log.e("subscribing");
    // subscriber.onError(new RuntimeException("always fails"));
    // }
    // });
    //
    // observable.retryWhen(new Func1<Observable<? extends Throwable>,
    // Observable<?>>() {
    // @Override
    // public Observable<?> call(Observable<? extends Throwable> observable) {
    // return observable.zipWith(Observable.range(1, 3), new Func2<Throwable,
    // Integer, Integer>() {
    // @Override
    // public Integer call(Throwable throwable, Integer integer) {
    // return integer;
    // }
    // }).flatMap(new Func1<Integer, Observable<?>>() {
    // @Override
    // public Observable<?> call(Integer integer) {
    // // Log.e("delay retry by " + integer + " second(s)");
    // //每一秒中执行一次
    // return Observable.timer(integer, TimeUnit.SECONDS);
    // }
    // });
    // }
    // }).subscribe(new Subscriber<Integer>() {
    // @Override
    // public void onCompleted() {
    // // Log.e("Sequence complete.");
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    // // Log.e("Error: " + e.getMessage());
    // }
    //
    // @Override
    // public void onNext(Integer value) {
    // // Log.e("Next:" + value);
    // }
    // });
    // }
    //
    // // RxJava线程控制
    // // Schedulers.immediate(): 直接在当前线程运行，相当于不指定线程。这是默认的 Scheduler。
    // // Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
    // // Schedulers.io(): I/O 操作（读写文件、读写数据库、网络信息交互等）所使用的 Scheduler
    // // Schedulers.computation(): 计算所使用的 Scheduler。这个计算指的是 CPU 密集型计算，即不会被 I/O
    // 等操作限制性能的操作，例如图形的计算。
    // // Android 还有一个专用的 AndroidSchedulers.mainThread()，它指定的操作将在 Android 主线程运行。
    //
    // // subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe
    // 被激活时所处的线程。或者叫做事件产生的线程。
    // // observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
    //
    // //通过 observeOn() 的多次调用，程序实现了线程的多次切换。不过，不同于 observeOn() ， subscribeOn()
    // 的位置放在哪里都可以，但它是只能调用一次的。
    // //当使用了多个subscribeOn() 的时候，只有第一个 subscribeOn() 起作用
    // private void SchedulersUse() {
    // Observable.just(1, 2, 3, 4)
    // .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
    // .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
    // .subscribe(new Action1<Integer>() {
    // @Override
    // public void call(Integer number) {
    // Log.d("number:" + number);
    // }
    // });
    //
    // Observable.just(1, 2, 3, 4) // IO 线程，由 subscribeOn() 指定
    // .subscribeOn(Schedulers.io())
    // .observeOn(Schedulers.newThread())
    // .map(mapOperator) // 新线程，由 observeOn() 指定
    // .observeOn(Schedulers.io())
    // .map(mapOperator2) // IO 线程，由 observeOn() 指定
    // .observeOn(AndroidSchedulers.mainThread)
    // .subscribe(subscriber); // Android 主线程，由 observeOn() 指定
    // }
    //
    // //doOnSubscribe() 默认情况下， doOnSubscribe() 执行在 subscribe() 发生的线程；而如果在
    // doOnSubscribe() 之后有 subscribeOn() 的话，它将执行在离它最近的 subscribeOn() 所指定的线程。
    // private void doOnSubscribeUse() {
    // Observable.create(new Observable.OnSubscribe<String>() {
    // @Override
    // public void call(Subscriber<? super String> subscriber) {
    // subscriber.onNext("****");
    // }
    // }).subscribeOn(Schedulers.io())
    // .doOnSubscribe(new Action0() {
    // @Override
    // public void call() {
    // // 需要在流程开始前的初始化之前在主线程执行的操作
    // }
    // })
    // .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
    // .observeOn(AndroidSchedulers.mainThread())
    // .subscribe(new Action1<String>() {
    // @Override
    // public void call(String s) {
    //
    // }
    // });
    // }
    //
    // //hrottleFirst():在每次事件触发后的一定时间间隔内丢弃新的事件。常用作去抖动过滤，例如按钮的点击监听器：
    // private void flatUse(Context context) {
    // RxView.clickEvents(new Button(context)) // RxBinding 代码，后面的文章有解释
    // .throttleFirst(500, TimeUnit.MILLISECONDS) // 设置防抖间隔为 500ms
    // .subscribe(new Subscriber<ViewClickEvent>() {
    // @Override
    // public void onCompleted() {
    //
    // }
    //
    // @Override
    // public void onError(Throwable e) {
    //
    // }
    //
    // @Override
    // public void onNext(ViewClickEvent viewClickEvent) {
    //
    // }
    // });
    // }

    static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        }
    }

}
