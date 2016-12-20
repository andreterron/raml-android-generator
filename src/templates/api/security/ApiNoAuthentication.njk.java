{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.security') -%}

import android.support.annotation.Nullable;

import org.springframework.http.HttpHeaders;

import java.util.Map;

/**
 * Created by andreterron on 11/25/15.
 */
public class ApiNoAuthentication extends ApiAuthentication {
    @Nullable
    @Override
    public HttpHeaders getHeaders() {
        return null;
    }

    @Nullable
    @Override
    public Map<String, String> getQueryParameters() {
        return null;
    }

    @Nullable
    @Override
    public String getQueryString() {
        return null;
    }
}

{%- endcall %}
