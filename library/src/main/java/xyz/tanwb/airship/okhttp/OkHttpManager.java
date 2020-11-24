package xyz.tanwb.airship.okhttp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okio.Buffer;
import xyz.tanwb.airship.App;
import xyz.tanwb.airship.okhttp.cache.CacheEntity;
import xyz.tanwb.airship.okhttp.cache.CacheMode;
import xyz.tanwb.airship.okhttp.cookie.CookieJarImpl;
import xyz.tanwb.airship.okhttp.cookie.CookieStore;
import xyz.tanwb.airship.okhttp.https.HttpsUtils;
import xyz.tanwb.airship.okhttp.interceptor.LoggerInterceptor;
import xyz.tanwb.airship.okhttp.model.HttpHeaders;
import xyz.tanwb.airship.okhttp.model.HttpParams;
import xyz.tanwb.airship.okhttp.request.DeleteRequest;
import xyz.tanwb.airship.okhttp.request.GetRequest;
import xyz.tanwb.airship.okhttp.request.HeadRequest;
import xyz.tanwb.airship.okhttp.request.OptionsRequest;
import xyz.tanwb.airship.okhttp.request.PostRequest;
import xyz.tanwb.airship.okhttp.request.PutRequest;

/**
 * OKHttp API
 * https://github.com/jeasonlzy0216/OkHttpUtils
 */
public class OkHttpManager {

    public static final int DEFAULT_MILLISECONDS = 60000; //默认的超时时间

    private static OkHttpManager mInstance;                 //单例

    private Handler mDelivery;                            //用于在主线程执行的调度器
    private OkHttpClient.Builder okHttpClientBuilder;     //ok请求的客户端
    private HttpParams mCommonParams;                     //全局公共请求参数
    private HttpHeaders mCommonHeaders;                   //全局公共请求头
    private CacheMode mCacheMode;                         //全局缓存模式
    private long mCacheTime = CacheEntity.CACHE_NEVER_EXPIRE;  //全局缓存过期时间,默认永不过期
    private CookieJarImpl cookieJar;                      //全局 Cookie 实例

    public OkHttpManager() {
        mDelivery = new Handler(Looper.getMainLooper());
        okHttpClientBuilder = new OkHttpClient.Builder();
        setHostnameVerifier();
        setConnectTimeout(DEFAULT_MILLISECONDS);
        setReadTimeOut(DEFAULT_MILLISECONDS);
        setWriteTimeOut(DEFAULT_MILLISECONDS);
        setCache(new Cache(getContext().getCacheDir(), 1024 * 1024 * 100)); //100Mb
        debug();
    }

    public static OkHttpManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpManager();
                }
            }
        }
        return mInstance;
    }

    public static Context getContext() {
        return App.app();
    }

    public Handler getDelivery() {
        return mDelivery;
    }

    public OkHttpClient getOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

    public OkHttpClient.Builder getOkHttpClientBuilder() {
        return okHttpClientBuilder;
    }

    /**
     * get请求
     */
    public static GetRequest get(String url) {
        return new GetRequest(url);
    }

    /**
     * post请求
     */
    public static PostRequest post(String url) {
        return new PostRequest(url);
    }

    /**
     * put请求
     */
    public static PutRequest put(String url) {
        return new PutRequest(url);
    }

    /**
     * head请求
     */
    public static HeadRequest head(String url) {
        return new HeadRequest(url);
    }

    /**
     * delete请求
     */
    public static DeleteRequest delete(String url) {
        return new DeleteRequest(url);
    }

    /**
     * patch请求
     */
    public static OptionsRequest options(String url) {
        return new OptionsRequest(url);
    }

    /**
     * 调试模式
     */
    public OkHttpManager debug() {
        if (App.isDebug()) {
            okHttpClientBuilder.addInterceptor(new LoggerInterceptor(true));
        }
        return this;
    }

    /**
     * https的默认全局访问规则
     */
    public OkHttpManager setHostnameVerifier() {
        /**
         * 此类是用于主机名验证的基接口。 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，
         * 则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。策略可以是基于证书的或依赖于其他验证方案。
         * 当验证 URL 主机名使用的默认规则失败时使用这些回调。如果主机名是可接受的，则返回 true
         */
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        return setHostnameVerifier(hostnameVerifier);
    }

    /**
     * https的全局访问规则
     */
    public OkHttpManager setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * https的全局自签名证书
     */
    public OkHttpManager setCertificates(InputStream... certificates) {
        okHttpClientBuilder.sslSocketFactory(HttpsUtils.getSslSocketFactory(certificates, null, null));
        return this;
    }

    /**
     * https的全局自签名证书
     */
    public OkHttpManager setCertificates(String... certificates) {
        for (String certificate : certificates) {
            InputStream inputStream = new Buffer().writeUtf8(certificate).inputStream();
            setCertificates(inputStream);
        }
        return this;
    }

    /**
     * 全局cookie存取规则
     */
    public OkHttpManager setCookieStore(CookieStore cookieStore) {
        cookieJar = new CookieJarImpl(cookieStore);
        okHttpClientBuilder.cookieJar(cookieJar);
        return this;
    }

    /**
     * 获取全局的cookie实例
     */
    public CookieJarImpl getCookieJar() {
        return cookieJar;
    }

    /**
     * 全局读取超时时间
     */
    public OkHttpManager setReadTimeOut(int readTimeOut) {
        okHttpClientBuilder.readTimeout(readTimeOut, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 全局写入超时时间
     */
    public OkHttpManager setWriteTimeOut(int writeTimeout) {
        okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 全局连接超时时间
     */
    public OkHttpManager setConnectTimeout(int connectTimeout) {
        okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    public OkHttpManager setCache(Cache cache) {
        okHttpClientBuilder.cache(cache);
        return this;
    }

    /**
     * 全局的缓存模式
     */
    public OkHttpManager setCacheMode(CacheMode cacheMode) {
        mCacheMode = cacheMode;
        return this;
    }

    /**
     * 获取全局的缓存模式
     */
    public CacheMode getCacheMode() {
        return mCacheMode;
    }

    /**
     * 全局的缓存过期时间
     */
    public OkHttpManager setCacheTime(long cacheTime) {
        if (cacheTime <= -1) cacheTime = CacheEntity.CACHE_NEVER_EXPIRE;
        mCacheTime = cacheTime;
        return this;
    }

    /**
     * 获取全局的缓存过期时间
     */
    public long getCacheTime() {
        return mCacheTime;
    }

    /**
     * 获取全局公共请求参数
     */
    public HttpParams getCommonParams() {
        return mCommonParams;
    }

    /**
     * 添加全局公共请求参数
     */
    public OkHttpManager addCommonParams(HttpParams commonParams) {
        if (mCommonParams == null) mCommonParams = new HttpParams();
        mCommonParams.put(commonParams);
        return this;
    }

    /**
     * 获取全局公共请求头
     */
    public HttpHeaders getCommonHeaders() {
        return mCommonHeaders;
    }

    /**
     * 添加全局公共请求参数
     */
    public OkHttpManager addCommonHeaders(HttpHeaders commonHeaders) {
        if (mCommonHeaders == null) mCommonHeaders = new HttpHeaders();
        mCommonHeaders.put(commonHeaders);
        return this;
    }

    /**
     * 添加网络拦截器
     */
    public OkHttpManager addNetworkInterceptor(@Nullable Interceptor interceptor) {
        okHttpClientBuilder.addNetworkInterceptor(interceptor);
        return this;
    }

    /**
     * 添加全局拦截器
     */
    public OkHttpManager addInterceptor(@Nullable Interceptor interceptor) {
        okHttpClientBuilder.addInterceptor(interceptor);
        return this;
    }

    /**
     * 根据Tag取消请求
     */
    public void cancelTag(Object tag) {
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    //    OkHttpUtils.get(Urls.URL_METHOD) // 请求方式和请求url, get请求不需要拼接参数，支持get，post，put，delete，head，options请求
    //    .tag(this)               // 请求的 tag, 主要用于取消对应的请求
    //    .connTimeOut(10000)      // 设置当前请求的连接超时时间
    //    .readTimeOut(10000)      // 设置当前请求的读取超时时间
    //    .writeTimeOut(10000)     // 设置当前请求的写入超时时间
    //    .cacheKey("cacheKey")    // 设置当前请求的缓存key,建议每个不同功能的请求设置一个
    //    .cacheMode(CacheMode.DEFAULT) // 缓存模式，详细请看第四部分，缓存介绍
    //    目前提供了四种CacheMode缓存模式
    //    DEFAULT: 按照HTTP协议的默认缓存规则，例如有304响应头时缓存
    //    REQUEST_FAILED_READ_CACHE：先请求网络，如果请求网络失败，则读取缓存，如果读取缓存失败，本次请求失败。该缓存模式的使用，会根据实际情况，导致onResponse,onError,onAfter三个方法调用不只一次，具体请在三个方法返回的参数中进行判断。
    //    IF_NONE_CACHE_REQUEST：如果缓存不存在才请求网络，否则使用缓存。
    //    FIRST_CACHE_THEN_REQUEST：先使用缓存，不管是否存在，仍然请求网络，如果网络顺利，会导致onResponse方法执行两次，第一次isFromCache为true，第二次isFromCache为false。使用时根据实际情况，对onResponse,onError,onAfter三个方法进行具体判断。
    //    .setCertificates(getAssets().open("srca.cer")) // 自签名https的证书，可变参数，可以设置多个
    //    .addInterceptor(interceptor)            // 添加自定义拦截器
    //    .headers("header1", "headerValue1")     // 添加请求头参数
    //    .headers("header2", "headerValue2")     // 支持多请求头参数同时添加
    //    .params("param1", "paramValue1")        // 添加请求参数
    //    .params("param2", "paramValue2")        // 支持多请求参数同时添加
    //    .params("file1", new File("filepath1")) // 可以添加文件上传
    //    .params("file2", new File("filepath2")) // 支持多文件同时添加上传
    //    .addCookie("aaa", "bbb")                // 这里可以传递自己想传的Cookie
    //    .addCookie(cookie)                      // 可以自己构建cookie
    //    .addCookies(cookies)                    // 可以一次传递批量的cookie
    //    //这里给出的泛型为 RequestInfo，同时传递一个泛型的 class对象，即可自动将数据结果转成对象返回
    //    .execute(new DialogCallback<RequestInfo>(this, RequestInfo.class) {
    //        @Override
    //        public void onBefore(BaseRequest request) {
    //            // UI线程 请求网络之前调用
    //            // 可以显示对话框，添加/修改/移除 请求参数
    //        }
    //
    //        @Override
    //        public RequestInfo parseNetworkResponse(Response response) throws Exception{
    //            // 子线程，可以做耗时操作
    //            // 根据传递进来的 response 对象，把数据解析成需要的 RequestInfo 类型并返回
    //            // 可以根据自己的需要，抛出异常，在onError中处理
    //            return null;
    //        }
    //
    //        @Override
    //        public void onResponse(boolean isFromCache, RequestInfo requestInfo, Request request, @Nullable Response response) {
    //            // UI 线程，请求成功后回调
    //            // isFromCache 表示当前回调是否来自于缓存
    //            // requestInfo 返回泛型约定的实体类型参数
    //            // request     本次网络的请求信息，如果需要查看请求头或请求参数可以从此对象获取
    //            // response    本次网络访问的结果对象，包含了响应头，响应码等，如果数据来自于缓存，该对象为null
    //        }
    //
    //        @Override
    //        public void onError(boolean isFromCache, Call call, @Nullable Response response, @Nullable Exception e) {
    //            // UI 线程，请求失败后回调
    //            // isFromCache 表示当前回调是否来自于缓存
    //            // call        本次网络的请求对象，可以根据该对象拿到 request
    //            // response    本次网络访问的结果对象，包含了响应头，响应码等，如果网络异常 或者数据来自于缓存，该对象为null
    //            // e           本次网络访问的异常信息，如果服务器内部发生了错误，响应码为 400~599之间，该异常为 null
    //        }
    //
    //        @Override
    //        public void onAfter(boolean isFromCache, @Nullable RequestInfo requestInfo, Call call, @Nullable Response response, @Nullable Exception e) {
    //            // UI 线程，请求结束后回调，无论网络请求成功还是失败，都会调用，可以用于关闭显示对话框
    //            // isFromCache 表示当前回调是否来自于缓存
    //            // requestInfo 返回泛型约定的实体类型参数，如果网络请求失败，该对象为　null
    //            // call        本次网络的请求对象，可以根据该对象拿到 request
    //            // response    本次网络访问的结果对象，包含了响应头，响应码等，如果网络异常 或者数据来自于缓存，该对象为null
    //            // e           本次网络访问的异常信息，如果服务器内部发生了错误，响应码为 400~599之间，该异常为 null
    //        }
    //
    //        @Override
    //        public void upProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
    //            // UI 线程，文件上传过程中回调，只有请求方式包含请求体才回调（GET,HEAD不会回调）
    //            // currentSize  当前上传的大小（单位字节）
    //            // totalSize 　 需要上传的总大小（单位字节）
    //            // progress     当前上传的进度，范围　0.0f ~ 1.0f
    //            // networkSpeed 当前上传的网速（单位秒）
    //        }
    //
    //        @Override
    //        public void downloadProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
    //            // UI 线程，文件下载过程中回调
    //            //参数含义同　上传相同
    //        }
    //    });
}