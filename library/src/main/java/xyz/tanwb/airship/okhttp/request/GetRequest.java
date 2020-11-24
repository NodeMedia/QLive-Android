package xyz.tanwb.airship.okhttp.request;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Get 请求的实现类.
 */
public class GetRequest extends BaseRequest<GetRequest> {

    public GetRequest(String url) {
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
        return requestBuilder.get().url(url).tag(tag).build();
    }
}