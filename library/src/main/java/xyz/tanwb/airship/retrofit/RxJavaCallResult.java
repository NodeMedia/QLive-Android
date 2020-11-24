package xyz.tanwb.airship.retrofit;

import java.io.IOException;

import retrofit2.Response;

/**
 * The result of executing an HTTP request.
 */
public final class RxJavaCallResult<T> {

    private final Response<T> response;
    private final Throwable error;

    private RxJavaCallResult(Response<T> response, Throwable error) {
        this.response = response;
        this.error = error;
    }

    public static <T> RxJavaCallResult<T> response(Response<T> response) {
        if (response == null) throw new NullPointerException("response == null");
        return new RxJavaCallResult<>(response, null);
    }

    /**
     * The response received from executing an HTTP request. Only present when {@link #isError()} is
     * false, null otherwise.
     */
    public Response<T> response() {
        return response;
    }

    public static <T> RxJavaCallResult<T> error(Throwable error) {
        if (error == null) throw new NullPointerException("error == null");
        return new RxJavaCallResult<>(null, error);
    }

    /**
     * The error experienced while attempting to execute an HTTP request. Only present when {@link
     * #isError()} is true, null otherwise.
     * <p/>
     * If the error is an {@link IOException} then there was a problem with the transport to the
     * remote server. Any other exception type indicates an unexpected failure and should be
     * considered fatal (configuration error, programming error, etc.).
     */
    public Throwable error() {
        return error;
    }

    /**
     * {@code true} if the request resulted in an error. See {@link #error()} for the cause.
     */
    public boolean isError() {
        return error != null;
    }
}
