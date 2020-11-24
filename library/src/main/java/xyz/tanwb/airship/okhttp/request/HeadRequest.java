package xyz.tanwb.airship.okhttp.request;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Head 请求的实现类.
 */
public class HeadRequest extends BaseRequest<HeadRequest> {

    public HeadRequest(String url) {
        super(url);
    }

    @Override
    protected RequestBody generateRequestBody() {
        return null;
    }

    @Override
    protected Request generateRequest(RequestBody requestBody) {
        Request.Builder requestBuilder = new Request.Builder();
        appendHeaders(requestBuilder);
        url = createUrlFromParams(url, params.urlParamsMap);
        return requestBuilder.head().url(url).tag(tag).build();
    }
}
