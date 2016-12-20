{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.mock') -%}

import android.content.Context;
import android.support.annotation.RawRes;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;

{%- call java.importClass('.service.api.utils.UrlPathMatcher') %}{% endcall %}

/**
 * Created by andreterron on 10/30/15.
 */
public class MockEndpoint {
    UrlPathMatcher mMatcher;
    String mPath;
    String mRootUrl;
    HttpMethod mHttpMethod;
    int mResCode = 200;
    boolean mMock = false;
    @RawRes
    Integer mResponseResource = null;
    MediaType mContentType = MediaType.APPLICATION_JSON;

    Context mContext;
    private UrlPathMatcher mPathMatcher;

    public MockEndpoint(Context context, String rootUrl) {
        mContext = context;
        mRootUrl = rootUrl;
    }

    public MockHttpRequest createRequest(HttpMethod method, URI uri) {
        return new MockHttpRequest(method, uri, createResponse());
    }

    public MockHttpResponse createResponse() {
        return new MockHttpResponse(mContext, mContentType, mResCode, mResponseResource);
    }

    public MockEndpoint execute(HttpMethod method, String path) {
        mPath = path;
        mMatcher = new UrlPathMatcher(mRootUrl, mPath);
        mPathMatcher = new UrlPathMatcher(mPath);
        mHttpMethod = method;
        return this;
    }

    public boolean matches(String path) {
        return mMatcher.matches(path);
    }

    public boolean matches(URI url, HttpMethod method) {
        return (mMatcher.matches(url.getPath()) && method == mHttpMethod);
    }

    public void setMock(boolean mock) {
        mMock = mock;
    }

    public MockEndpoint get(String path) {
        return execute(HttpMethod.GET, path);
    }

    public MockEndpoint post(String path) {
        return execute(HttpMethod.POST, path);
    }

    public MockEndpoint put(String uri) {
        return execute(HttpMethod.PUT, uri);
    }

    public MockEndpoint delete(String uri) {
        return execute(HttpMethod.DELETE, uri);
    }

    public MockEndpoint success() {
        mResCode = 200;
        return this;
    }

    public MockEndpoint resCode(int code) {
        mResCode = code;
        return this;
    }

    public MockEndpoint json(@RawRes Integer responseResource) {
        mContentType = MediaType.APPLICATION_JSON;
        mResponseResource = responseResource;
        return this;
    }

    public boolean isMocked() {
        return mMock;
    }

    public boolean matchesPath(String path) {
        return mPathMatcher.matches(path);
    }
}

{%- endcall %}
