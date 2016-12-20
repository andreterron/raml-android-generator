{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.mock') -%}

import android.content.Context;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by andreterron on 10/30/15.
 */
@Singleton
public class MockRestTemplate extends RestTemplate {

    Context mContext;

    MockApi mMockApi;

    @Inject
    public MockRestTemplate(Context context, MockApi mockApi) {
        mContext = context;
        mMockApi = mockApi;
    }

    @Override
    protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
        ClientHttpRequest request = mMockApi.createHttpRequest(url, method);
        if (request != null) {
            return request;
        }
        return super.createRequest(url, method);
    }

}

{%- endcall %}
