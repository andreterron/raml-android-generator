{%- import "../javaBase.njk" as java -%}
{% call java.wrapImports(app, '.service.api') %}

import javax.inject.Inject;

/**
 * Created by andreterron on 11/4/15.
 */
public class RestApiEndpoint {

    protected String mPath;

    @Inject
    public RestApiBase mRestApi;

    public String getPath() {
        return mPath;
    }
}

{%- endcall %}
