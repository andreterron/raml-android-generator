{%- import "../javaBase.njk" as java -%}
{%- set className = (api.title + "ApiModule") | camelCaseCapital %}
{%- azapp className + ".java" -%}
{% call java.wrapImports(app, '.service.api') %}

import android.content.Context;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
{%- call java.importClass('.service.api.mock.MockApi') %}{% endcall %}
{%- call java.importClass('.service.api.mock.MockRestTemplate') %}{% endcall %}

/**
 * Created by andreterron on 9/29/15.
 */
@Module
public class {$ className $} {

    Context mContext;
    List<ClientHttpRequestInterceptor> mInterceptors = new ArrayList<>();

    public {$ className $}(Context context) {
        mContext = context;
    }

    public List<ClientHttpRequestInterceptor> getInterceptors() {
        return mInterceptors;
    }

    @Provides Context provideApplication() {
        return mContext;
    }

    @Provides @Singleton
    public RestApiBase provideRestApiBase(@Named("rootUrl") String rootUrl, RestOperations restOperations) {
        return new RestApiBase(rootUrl, restOperations);
    }

    @Provides @Singleton @Named("rootUrl")
    String provideRootUrl() {
        return "{$ api.baseUri | replace('{version}', api.version) $}";
    }

    @Provides @Singleton
    public RestOperations provideRestOperations(MockRestTemplate restTemplate) {
        return restTemplate;
    }

    @Provides @Singleton
    public MockRestTemplate provideMockRestTemplate(Context context, MockApi mockApi) {
        MockRestTemplate restTemplate = new MockRestTemplate(context, mockApi);
        restTemplate.getMessageConverters().clear();
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
        restTemplate.setInterceptors(mInterceptors);
        return restTemplate;
    }

    @Provides @Singleton
    public RestTemplate provideRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().clear();
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
        restTemplate.setInterceptors(mInterceptors);
        return restTemplate;
    }
}
{% endcall %}
{% endazapp %}
