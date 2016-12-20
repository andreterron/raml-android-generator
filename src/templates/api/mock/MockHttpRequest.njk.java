{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.mock') -%}

import org.openide.util.io.NullOutputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Created by andreterron on 10/30/15.
 */
public class MockHttpRequest extends AbstractClientHttpRequest {

    HttpMethod mHttpMethod;
    URI mUri;
    ClientHttpResponse mResponse;

    public MockHttpRequest(HttpMethod httpMethod, URI uri, ClientHttpResponse response) {
        mHttpMethod = httpMethod;
        mUri = uri;
        mResponse = response;
    }

    @Override
    protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
        return new NullOutputStream();
    }

    @Override
    protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
        return mResponse;
    }

    @Override
    public HttpMethod getMethod() {
        return mHttpMethod;
    }

    @Override
    public URI getURI() {
        return mUri;
    }
}

{%- endcall %}
