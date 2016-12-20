{%- import "../javaBase.njk" as java -%}
{%- set className = (api.title + "Api") | camelCaseCapital %}


{%- macro pathClass(res, path = '') -%}
    {%- set name = path + res.relativeUri -%}
    {%- set name = name | replace('{', '') -%}
    {%- set name = name | replace('}', '') -%}
    {$ (name + "Endpoint") | camelCaseCapital $}
{%- endmacro -%}

{%- azapp className + ".java" -%}
{% call java.wrapImports(app, '.service.api') %}
{%- call java.importClass('.service.api.security.ApiAuthentication') %}{% endcall %}

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by andreterron on 9/4/15.
 */
@Singleton
public class {$ className $} {

    {% for res in api.resources -%}
    @Inject
    public {$ java.type(".service.api.endpoint." + pathClass(res)) $} {$ res.relativeUri | camelCaseDecapital $};

    {% endfor -%}

    @Inject
    RestApiBase mRestApiBase;

    @Inject
    public {$ className $}() {
    }

    public void setDefaultAuth(ApiAuthentication auth) {
        mRestApiBase.setDefaultAuthentication(auth);
    }
}
{%- endcall -%}
{% endazapp -%}
