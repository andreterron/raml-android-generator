{%- import "../../javaBase.njk" as java -%}

{%- for securityScheme in raml.securitySchemes -%}
    {%- for schemeName, schemeData in securityScheme -%}
        {%- set authName = schemeName | camelCaseCapital %}

        {%- azapp "Api" + authName + "Authentication.java" -%}

{% call java.wrapImports(app, '.service.api.security') -%}

import android.support.annotation.Nullable;

import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpHeaders;

import java.util.Map;

/**
 * Created by andreterron on 11/24/15.
 */
public class Api{$ authName $}Authentication extends ApiAuthentication {

    private final HttpHeaders mHeaders = new HttpHeaders();

    public Api{$ authName $}Authentication(
        {%- set comma = joiner(', ') -%}
        {%- for header, headerInfo in schemeData.describedBy.headers -%}
            String {$ header | camelCaseDecapital $}
        {%- endfor -%}
    ) {
        {%- for header, headerInfo in schemeData.describedBy.headers %}
        mHeaders.set("{$ header $}", {$ header | camelCaseDecapital $});
        {%- endfor %}
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
{% endazapp -%}
{%- endfor -%}
{%- endfor -%}
