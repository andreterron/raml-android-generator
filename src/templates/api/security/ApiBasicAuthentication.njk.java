{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.security') -%}

import android.support.annotation.Nullable;

import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpHeaders;

import java.util.Map;

/**
 * Created by andreterron on 11/24/15.
 */
public class ApiBasicAuthentication extends ApiAuthentication {

    private final HttpHeaders mHeaders = new HttpHeaders();

    public ApiBasicAuthentication(String username, String password) {
        mHeaders.setAuthorization(new HttpBasicAuthentication(username, password));
    }

    public ApiBasicAuthentication(HttpBasicAuthentication auth) {
        mHeaders.setAuthorization(auth);
    }

    @Nullable
    @Override
    public HttpHeaders getHeaders() {
        return mHeaders;
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
