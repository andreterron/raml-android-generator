{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.mock') -%}

import android.content.Context;
import android.support.annotation.Nullable;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

{%- call java.importClass('.R') %}{% endcall %}
{%- call java.importClass('.service.api.RestApiEndpoint') %}{% endcall %}

/**
 * Created by andreterron on 10/30/15.
 */
@Singleton
public class MockApi {

    List<MockEndpoint> mEndpoints = new ArrayList<>();

    MockEndpoint mNullEndpoint;

    Context mContext;
    String mRootUrl;
    boolean mMockAll = false;

    @Inject
    public MockApi(Context context, @Named("rootUrl") String rootUrl) {
        mContext = context;
        mRootUrl = rootUrl;

        mNullEndpoint = new MockEndpoint(mContext, mRootUrl);

        {%- macro resource(node, path = "", stack = []) -%}

            {%- for res in node.resources %}

                {%- for method in res.methods -%}
                    {%- set verb = method.method | lower %}
                    {%- if method.responses['200'] %}
                        {%- set code = 200 %}
                        {%- set example = method.responses['200'].body['application/json'].exampleFile %}
                    {%- elif method.responses['201'] %}
                        {%- set code = 201 %}
                        {%- set example = method.responses['201'].body['application/json'].exampleFile %}
                    {%- endif %}
                    {%- if example %}
                        {%- set exampleFile = example.replace(r/((.*)\/)|(\.json)/g, '') | underscore %}
        registerEndpoint(new MockEndpoint(mContext, mRootUrl).{$ verb $}("{$ path $}{$ res.relativeUri $}").resCode({$ code $}).json(R.raw.{$ exampleFile $}));
                    {%- endif %}
                {%- endfor -%}

                {$ resource(res, path + res.relativeUri, stack.concat(res)) $}
            {%- endfor -%}
        {%- endmacro %}

        {$ resource(api) $}
    }

    protected void registerEndpoint(MockEndpoint endpoint) {
        mEndpoints.add(endpoint);
    }

    public void removeEndpoint(URI path) {
        removeEndpoint(path.toString());
    }

    public void removeEndpoint(RestApiEndpoint endpoint) {
        removeEndpoint(endpoint.getPath());
    }

    public void removeEndpoint(String path) {
        for (MockEndpoint mock: mEndpoints) {
            if (mock.matches(mRootUrl.concat(path)) && mock.isMocked()) {
                mock.setMock(false);
            }
        }
    }

    public <T extends RestApiEndpoint> T mock(T endpoint) {
        if (!mMockAll) {
            for (MockEndpoint mock : mEndpoints) {
                if (mock.matches(mRootUrl.concat(endpoint.getPath()))) {
                    mock.setMock(true);
                }
            }
        }
        return endpoint;
    }

    public MockApi mockAll() {
        mMockAll = true;
        return this;
    }

    @Nullable
    public ClientHttpRequest createHttpRequest(URI url, HttpMethod method) {
        for (MockEndpoint endpoint: mEndpoints) {
            if (endpoint.matches(url, method) && (mMockAll || endpoint.isMocked())) {
                return endpoint.createRequest(method, url);
            }
        }
        return null;
    }

}

{%- endcall %}
