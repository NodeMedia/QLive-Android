package xyz.tanwb.airship.retrofit;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import xyz.tanwb.airship.rxjava.RxBus;

public class HttpResponseBody extends ResponseBody {

    ResponseBody responseBody;

    public HttpResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        return Okio.buffer(new ForwardingSource(responseBody.source()) {

            //当前读取字节数
            long bytesReaded;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                bytesReaded = bytesReaded + (bytesRead == -1 ? 0 : bytesRead);
                int curr = (int) (bytesReaded * 100 / contentLength);
                RxBus.getInstance().post(RxJavaCallback.class.getName(), curr);
                return bytesRead;
            }
        });
    }
}
