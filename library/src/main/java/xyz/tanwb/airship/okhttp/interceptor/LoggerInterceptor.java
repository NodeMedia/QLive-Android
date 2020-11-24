package xyz.tanwb.airship.okhttp.interceptor;

import android.net.ConnectivityManager;
import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import xyz.tanwb.airship.rxjava.RxBus;
import xyz.tanwb.airship.utils.Log;
import xyz.tanwb.airship.utils.NetUtils;

public class LoggerInterceptor implements Interceptor {

    private boolean showResponse;

    public LoggerInterceptor(boolean showResponse) {
        this.showResponse = showResponse;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        boolean isConnect = NetUtils.isConnected();
        RxBus.getInstance().post(ConnectivityManager.CONNECTIVITY_ACTION, isConnect);
        if (!isConnect) {
            throw new IOException("请设置网络后再试.");
        }

        //请求拦截器，可用来做日志记录
        Request request = chain.request();
        long t1 = System.nanoTime();
        Log.e(String.format("Request %s on %s", request.method(), chain.connection()));
        Headers headers = request.headers();
        if (headers != null && headers.size() > 0) {
            Log.e(String.format("Request headers %s", headers.toString()));
        }
        RequestBody requestBody = request.body();
        if (requestBody != null) {
            MediaType mediaType = requestBody.contentType();
            if (mediaType != null) {
                Log.e(String.format("Request contentType %s", mediaType.toString()));
                if (isText(mediaType)) {
                    Log.e("Request content : " + bodyToString(request));
                } else {
                    Log.e("Request content : maybe [file part] , too large too print , ignored!");
                }
            }
        }
        Response response = chain.proceed(request);
        long t2 = System.nanoTime();
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        Log.e("Response for " + response.request().url().toString().replace("[", "%5B").replace("]", "%5D").replace("{", "%7B").replace("}", "%7D"));
        Log.e("Response code:" + clone.code() + " time consuming:" + (t2 - t1) + "ns protocol:" + clone.protocol());
        if (!TextUtils.isEmpty(clone.message())) {
            Log.e("Response message " + clone.message());
        }
        if (showResponse) {
            ResponseBody body = clone.body();
            if (body != null) {
                MediaType mediaType = body.contentType();
                if (mediaType != null) {
                    Log.e(String.format("Response contentType %s", mediaType.toString()));
                    if (isText(mediaType)) {
                        String resp = body.string();
                        Log.e("Response content:" + resp);
                        body = ResponseBody.create(mediaType, resp);
                        return response.newBuilder().body(body).build();
                    } else {
                        Log.e("Response content: maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        }
        return response;
    }

    private boolean isText(MediaType mediaType) {
        return mediaType.type() != null && mediaType.type().equals("text") || mediaType.subtype() != null && (mediaType.subtype().equals("json") || mediaType.subtype().equals("xml") || mediaType.subtype().equals("html") || mediaType.subtype().equals("webviewhtml"));
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
    }
}
