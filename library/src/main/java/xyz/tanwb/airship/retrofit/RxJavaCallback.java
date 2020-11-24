package xyz.tanwb.airship.retrofit;

import androidx.annotation.IntDef;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import xyz.tanwb.airship.App;
import xyz.tanwb.airship.BaseConstants;
import xyz.tanwb.airship.bean.ErrorCode;
import xyz.tanwb.airship.rxjava.RxBus;
import xyz.tanwb.airship.rxjava.RxBusManage;
import xyz.tanwb.airship.utils.FileUtils;

public abstract class RxJavaCallback extends Subscriber<ResponseBody> {

    public static final int HTTP_COMMON = 0X00000001;
    public static final int HTTP_UPLOAD = 0X00000002;
    public static final int HTTP_DOWN = 0X00000003;

    @IntDef({HTTP_COMMON, HTTP_UPLOAD, HTTP_DOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HttpType {
    }

    //Http请求类型
    protected int httpType;
    //下载文件类型标志
    protected String mark;
    //Http返回实体类型
    protected Type entryType;

    protected RxBusManage rxBusManage;

//    public RxJavaCallback() {
//        entryType = getSuperclassTypeParameter(getClass());
//    }
//
//    static Type getSuperclassTypeParameter(Class<?> subclass) {
//        Type superclass = subclass.getGenericSuperclass();
//        if (superclass instanceof Class) {
//            return null;
//        }
//        ParameterizedType parameterized = (ParameterizedType) superclass;
//        return parameterized.getActualTypeArguments()[0];
//    }

    /**
     * 设置返回实体类型
     */
    public void setEntryType(Type entryType) {
        this.entryType = entryType;
    }

    /**
     * 设置Http请求类型
     */
    public void setHttpType(@HttpType int httpType) {
        this.httpType = httpType;
    }

    /**
     * 设置请求标志
     *
     * @param mark 请求标志,默认『default』,上传设置为『upload』,下载设置为下载文件类型
     */
    public void setHttpMark(String mark) {
        this.mark = mark;
    }

    /**
     * 开启进度监听
     */
    public RxJavaCallback openProgress() {
        if (rxBusManage == null) {
            rxBusManage = new RxBusManage();
        }
        rxBusManage.add(RxBus.getInstance().register(RxJavaCallback.class.getName()).onBackpressureBuffer().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                onLoading((int) o);
            }
        }));
        return this;
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        try {
            switch (httpType) {
                case HTTP_COMMON:
                    onNextData(responseBody);
                    break;
                case HTTP_UPLOAD:
                    onNextUpload(responseBody);
                    break;
                case HTTP_DOWN:
                    onNextDown(responseBody);
                    break;
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    public void onError(Throwable e) {
        onFailure(App.isDebug() ? e.getMessage() : ErrorCode.ERRORMAP.get("-1"));
    }

    @Override
    public void onCompleted() {
        if (rxBusManage != null) {
            rxBusManage.clear();
            rxBusManage = null;
        }
        this.unsubscribe();
    }

    protected void onNextData(ResponseBody responseBody) throws Exception {
        String result = toString(responseBody);
        onSuccess(JSON.parseObject(result, entryType));
    }

    protected void onNextUpload(ResponseBody responseBody) throws Exception {
        String result = toString(responseBody);
        onSuccess(JSON.parseObject(result, entryType));
    }

    protected void onNextDown(ResponseBody responseBody) throws Exception {
        String filePath = toFile(responseBody, FileUtils.getAppSdPath(FileUtils.PATH_FILE) + File.separator + "down_" + System.currentTimeMillis() + BaseConstants.DOT + mark);
        onSuccess(filePath);
    }

    protected String toString(ResponseBody responseBody) throws Exception {
        BufferedSource bufferedSource = Okio.buffer(responseBody.source());
        String tempStr = bufferedSource.readUtf8();
        bufferedSource.close();
        return tempStr;
    }

    protected String toFile(ResponseBody responseBody, String filePath) throws Exception {
        File file = new File(filePath);
        InputStream in = null;
        FileOutputStream out = null;
        byte[] buf = new byte[2048 * 10];
        int len;
        try {
            in = responseBody.byteStream();
            out = new FileOutputStream(file);
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * 接口调用成功
     *
     * @param data 返回数据
     */
    public void onSuccess(Object data) {
    }

    /**
     * 接口调用失败
     *
     * @param strMsg 失败消息
     */
    public void onFailure(String strMsg) {
    }

    /**
     * 文件上传或下载进度
     *
     * @param curr 进度
     */
    public void onLoading(long curr) {
    }

}
