{# macros #}
{%- import "./javaBase.njk" as java -%}

{%- macro pathSegment(res, comma) -%}

    {%- for v, info in res.uriParameters -%}
    {$ comma() $}{$ java.type(info.type) $} {$ v $}
    {%- endfor -%}

{%- endmacro -%}

{%- macro pathMethod(res, method, path) -%}
    {%- set name = path + res.relativeUri -%}
    {%- set name = name | replace('{', '') -%}
    {%- set name = name | replace('}', '') -%}
    {$ method.method $}{$ name | camelCaseCapital $}
{%- endmacro -%}

{%- macro responseType(stack, method) -%}
    {%- set i = raml -%}
    {%- for n in stack -%}
        {%- set i = i[n.relativeUri] -%}
    {%- endfor -%}

    {%- set schemaName = i[method.method].responses['200'].body['application/json'].schema %}

    {%- if schemaName -%}
        {$ java.type('.model.' + schemaName) $}
    {%- else -%}
        Void
    {%- endif -%}

{%- endmacro -%}

{%- set className = (api.title + "Api") | camelCaseCapital %}
{%- azapp className + ".java" -%}

{% call java.wrapImports(app, '.service.api') %}
import android.support.annotation.NonNull;
import android.content.Context;
import java.util.HashMap;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.http.HttpMethod;

/**
 * Created by andreterron on 9/17/15.
 */ {#
@Rest(rootUrl = "{$ api.baseUri | replace('{version}', api.version) $}", converters = GsonHttpMessageConverter.class) #}
public class {$ className $} {

    private String rootUrl;
    private RestTemplate restTemplate;

    public {$ className $}(Context context) {
        rootUrl = "{$ api.baseUri | replace('{version}', api.version) $}";
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().clear();
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
    }

    {%- macro resource(node, path = "", stack = []) -%}

        {%- for res in node.resources %}
            {%- for method in res.methods %}

    {%- set verb = method.method | capitalize -%}
    {%- set resType = responseType(stack.concat(res), method) -%}
    {%- set resTypeClass = resType + '.class' if resType != 'Void' else "((Class<Void>) null)" -%}
    {# java.importClass('org.androidannotations.annotations.rest.' + verb) #}

    /**
     * {$ method.description $}
     {%- for n in stack.concat(res) %}
        {%- for v, info in n.uriParameters %}
     * @param {$ v $} {$ info.displayName $}
        {%- endfor %}
     {%- endfor %}
     */{# @{$ verb $}("{$ path + res.relativeUri $}") #}
    ResponseEntity<{$ resType $}> {$ pathMethod(res, method, path) $}(
        {%- set comma = joiner(', ') -%}
        {%- for n in stack.concat(res) -%}
            {$ pathSegment(n, comma) $}
        {%- endfor -%}
    ) {
        {%- set urlParams = "" -%}
        {%- for n in stack.concat(res) -%}
            {%- for v, info in n.uriParameters -%}
                {%- if not urlParams %}
        HashMap<String, Object> urlVariables = new HashMap<>();
                    {%- set urlParams = ", urlVariables" -%}
                {%- endif %}
        urlVariables.put("{$ v $}", {$ v $});
            {%- endfor -%}
        {%- endfor %}
        return restTemplate.exchange("{$ path + res.relativeUri $}", HttpMethod.{$ verb | upper $}, null, {$ resTypeClass $}{$ urlParams $});
    }

    {%- if method.queryParameters %}

    /**
     * {$ method.description $}
     {%- for n in stack.concat(res) %}
        {%- for v, info in n.uriParameters %}
     * @param {$ v $} {$ info.displayName $}
        {%- endfor %}
     {%- endfor %}
     */{# @{$ verb $}("{$ path + res.relativeUri $}") #}
    ResponseEntity<{$ resType $}> {$ pathMethod(res, method, path) $}(
        {%- set comma = joiner(', ') -%}
        {%- for n in stack.concat(res) -%}
            {$ pathSegment(n, comma) $}
        {%- endfor -%}
        {$ comma() $}@NonNull HashMap<String, Object> queryParams) {

        {%- set urlParams = "" -%}
        {%- for n in stack.concat(res) -%}
            {%- for v, info in n.uriParameters -%}
                {%- if not urlParams %}
        HashMap<String, Object> urlVariables = new HashMap<>();
                    {%- set urlParams = ", urlVariables" -%}
                {%- endif %}
        urlVariables.put("{$ v $}", {$ v $});
            {%- endfor -%}
        {%- endfor %}
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(rootUrl.concat("{$ path + res.relativeUri $}"));
        for (String key : queryParams.keySet()) {
            builder.queryParam(key, queryParams.get(key));
        }

        return restTemplate.exchange(builder.build().encode().toUriString(), HttpMethod.{$ verb | upper $}, null, {$ resTypeClass $}{$ urlParams $});
    }
    {%- endif -%}

        {%- endfor %}

            {%- call resource(res, path + res.relativeUri, stack.concat(res)) %}{% endcall -%}
        {%- endfor -%}

    {%- endmacro -%}

    {$ resource(api) $}

}
{%- endcall -%}
{% endazapp -%}
