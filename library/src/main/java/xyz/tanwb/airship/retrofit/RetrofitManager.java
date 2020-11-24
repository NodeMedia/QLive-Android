package xyz.tanwb.airship.retrofit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;
import xyz.tanwb.airship.okhttp.OkHttpManager;

public class RetrofitManager {

    private static final String PATHANNOTATION = "{path}";
    private static final String PATHPARAM = "path";
    private static final String JSONPARAM = "json";

    private String baseUrl;
    private Map<String, Retrofit> mRetrofitMap;
    private Retrofit.Builder retrofirBuildr;

    public RetrofitManager() {
        this(null);
    }

    public RetrofitManager(String baseUrl) {
        this.baseUrl = baseUrl;
        mRetrofitMap = new HashMap<>();
        retrofirBuildr = new Retrofit.Builder();
        callFactory(getOkHttpClient());
        addConverterFactory(JSONConverterFactory.create());
        addCallAdapterFactory(RxJavaCallAdapterFactory.create());
    }

    public OkHttpClient getOkHttpClient() {
        return OkHttpManager.getInstance().getOkHttpClientBuilder().addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(new HttpResponseBody(originalResponse.body())).build();
            }
        }).build();
    }

    public Retrofit getRetrofit(String baseUrl) {
        if (mRetrofitMap.containsKey(baseUrl)) {
            return mRetrofitMap.get(baseUrl);
        } else {
            retrofirBuildr.baseUrl(baseUrl);
            mRetrofitMap.put(baseUrl, retrofirBuildr.build());
            return getRetrofit(baseUrl);
        }
    }

    public ApiService getService() {
        return getService(baseUrl, ApiService.class);
    }

    public <T> T getService(String baseUrl, Class<T> clazz) {
        if (baseUrl == null) {
            throw new NullPointerException("baseUrl == null");
        }
        if (clazz == null) {
            throw new NullPointerException("clazz == null");
        }
        return getRetrofit(baseUrl).create(clazz);
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RetrofitManager callFactory(okhttp3.Call.Factory factory) {
        retrofirBuildr.callFactory(factory);
        return this;
    }

    public RetrofitManager addConverterFactory(Converter.Factory factory) {
        retrofirBuildr.addConverterFactory(factory);
        return this;
    }

    public RetrofitManager addCallAdapterFactory(CallAdapter.Factory factory) {
        retrofirBuildr.addCallAdapterFactory(factory);
        return this;
    }

    public interface ApiService {

        @GET(PATHANNOTATION)
        Observable<ResponseBody> getHttp(@Path(value = PATHPARAM, encoded = true) String path, @QueryMap Map<String, Object> options);

        @GET(PATHANNOTATION)
        Observable<ResponseBody> getHttpToJson(@Path(value = PATHPARAM, encoded = true) String path, @Query(JSONPARAM) String json);

        @GET
        Observable<ResponseBody> getHttpToUrl(@Url String url, @QueryMap Map<String, Object> options);

        @FormUrlEncoded
        @POST(PATHANNOTATION)
        Observable<ResponseBody> postHttp(@Path(value = PATHPARAM, encoded = true) String path, @FieldMap Map<String, Object> options);

        @FormUrlEncoded
        @POST(PATHANNOTATION)
        Observable<ResponseBody> postHttpToJson(@Path(value = PATHPARAM, encoded = true) String path, @Field(JSONPARAM) String param);

        @POST
        Observable<ResponseBody> postHttpToUrl(@Url String url, @FieldMap Map<String, Object> options);

        @POST(PATHANNOTATION)
        Observable<ResponseBody> postHttpToBody(@Path(value = PATHPARAM, encoded = true) String path, @Body String body);

        @Multipart
        @POST(PATHANNOTATION)
        Observable<ResponseBody> postHttpToMultipart(@Path(value = PATHPARAM, encoded = true) String path, @Part("param") RequestBody requestBody);

        @FormUrlEncoded
        @PUT(PATHANNOTATION)
        Observable<ResponseBody> putHttp(@Path(value = PATHPARAM, encoded = true) String path, @FieldMap Map<String, Object> options);

        @FormUrlEncoded
        @PUT(PATHANNOTATION)
        Observable<ResponseBody> putHttpToJson(@Path(value = PATHPARAM, encoded = true) String path, @Field(JSONPARAM) String param);

        @FormUrlEncoded
        @DELETE(PATHANNOTATION)
        Observable<ResponseBody> deleteHttp(@Path(value = PATHPARAM, encoded = true) String path, @FieldMap Map<String, Object> options);

        @FormUrlEncoded
        @DELETE(PATHANNOTATION)
        Observable<ResponseBody> deleteHttpToJson(@Path(value = PATHPARAM, encoded = true) String path, @Field(JSONPARAM) String param);

        @Headers({"Content-Type: */*"})
        @GET(PATHANNOTATION)
        Observable<ResponseBody> header(@Header("X-LC-Session") String sesssion, @Path(value = PATHPARAM, encoded = true) String downloadUrl);

        @Multipart
        @POST(PATHANNOTATION)
        Observable<ResponseBody> upload(@Path(value = PATHPARAM, encoded = true) String path, @Part MultipartBody.Part parts);

        @GET(PATHANNOTATION)
        Observable<ResponseBody> download(@Path(value = PATHPARAM, encoded = true) String path);
    }
}