{%- import "../../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api.security') -%}

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Created by andreterron on 11/23/15.
 */
public abstract class ApiAuthentication {

    public HttpHeaders apply(UriComponentsBuilder builder, MultiValueMap<String, String> headers) {
        if (headers == null) {
            return getHeaders();
        }

        HttpHeaders tempHeaders = new HttpHeaders();

        // Add previous headers
        tempHeaders.putAll(headers);

        // Add authentication headers
        HttpHeaders authHeaders = getHeaders();
        if (authHeaders != null) {
            tempHeaders.putAll(authHeaders);
        }

        // TODO : apply queryParameters and queryStrings if needed

        return tempHeaders;
    }

    @Nullable
    abstract public HttpHeaders getHeaders();

    @Nullable
    abstract public Map<String, String> getQueryParameters();

    @Nullable
    abstract public String getQueryString();

}

{%- endcall %}
