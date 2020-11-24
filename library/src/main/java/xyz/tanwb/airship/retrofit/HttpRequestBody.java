package xyz.tanwb.airship.retrofit;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import xyz.tanwb.airship.rxjava.RxBus;

public class HttpRequestBody extends RequestBody {

    private RequestBody requestBody;

    public HttpRequestBody(String filePath) {
        this(new File(filePath));
    }

    public HttpRequestBody(File file) {
        this(RequestBody.create(MediaType.parse("image/jpg"), file));
    }

    public HttpRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (sink == null) {
            //包装
            sink = Okio.buffer(new ForwardingSink(sink) {

                //当前写入字节数
                long bytesWritten;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        //获得contentLength的值，后续不再调用
                        contentLength = contentLength();
                    }
                    bytesWritten = bytesWritten + byteCount;
                    int curr = (int) (bytesWritten * 100 / contentLength);
                    RxBus.getInstance().post(RxJavaCallback.class.getName(), curr);
                }
            });
        }
        //写入
        requestBody.writeTo(sink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        sink.flush();
    }

}
