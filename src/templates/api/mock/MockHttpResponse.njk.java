{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.mock') -%}

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;

import org.openide.util.io.NullInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.AbstractClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by andreterron on 10/30/15.
 */
public class MockHttpResponse extends AbstractClientHttpResponse {

    private MediaType mContentType;

    Context mContext;

    @RawRes
    Integer mResponseResource;

    int mStatusCode = 200;

    public MockHttpResponse(Context context, MediaType contentType, int statusCode, @RawRes @Nullable Integer responseResource) {
        mContext = context;
        mContentType = contentType;
        mStatusCode = statusCode;
        mResponseResource = responseResource;
    }

    @Override
    protected InputStream getBodyInternal() throws IOException {
        if (mResponseResource != null) {
            return mContext.getResources().openRawResource(mResponseResource);
        }
        return new NullInputStream();
    }

    @Override
    protected void closeInternal() {

    }

    @Override
    public int getRawStatusCode() throws IOException {
        return mStatusCode;
    }

    @Override
    public String getStatusText() throws IOException {
        return HttpStatus.valueOf(mStatusCode).getReasonPhrase();
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mContentType);
        return headers;
    }
}

{%- endcall %}
