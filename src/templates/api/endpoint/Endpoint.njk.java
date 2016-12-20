{%- import "./../../javaBase.njk" as java -%}


{%- macro propertyType(property, fname) -%}
    {%- if property.type == 'file' -%}
        {$ java.type('java.io.File') $}
    {%- elif property.type == 'array' -%}
        {$ java.type('java.util.ArrayList') $}<{% call propertyType(property.items, fname | replace(r/s$/, '')) %}{% endcall %}>
    {%- elif property.type == 'object' -%}
        {%- set idtype = typeFromId(property.id) -%}
        {%- if idtype.val -%}
            {$ idtype $}
        {%- else -%}
            {$ java.type(fname | camelCaseCapital) $}
        {%- endif -%}
    {%- elif property.type -%}
        {$ java.type(property.type | replace("number", "integer")) $}
    {%- else -%}
        Void
    {%- endif -%}
{%- endmacro -%}

{%- macro formParam(name, param) -%}
    {%- if param.type == 'file' -%}
        new {$ java.type('.util.' + 'ImageFileSystemResource') $}({$ name $}, 1024)
    {%- else -%}
        {$ name $}
    {%- endif -%}
{%- endmacro -%}

{%- macro typeFromId(id) -%}
    {%- for key, schema in schemas -%}
        {%- if id == schema.id -%}
            {%- set title = schema.title if schema.title else key -%}
            {$ java.type('.service.api.schema.Api' + title | camelCaseCapital) $}
        {%- endif -%}
    {%- endfor -%}
{%- endmacro -%}

{%- macro tabs(tabNumber) -%}
{%- for i in range(0, tabNumber) %}    {% endfor -%}
{%- endmacro -%}

{%- macro parcelType (property) -%}
{$ propertyType(property) | camelCaseCapital $}
{%- endmacro -%}

{% macro parcelRead(property) -%}
{%- if property.type == 'array' -%}
    createTypedArrayList({$ propertyType(property.items) $}.CREATOR)
{%- else -%}
    read{$ propertyType(property) | camelCaseCapital $}()
{%- endif -%}
{%- endmacro -%}


{% macro parcelWrite(property) -%}
{%- if property.type == 'array' -%}
    TypedArray
{%- else -%}
    {$ propertyType(property) | camelCaseCapital $}
{%- endif -%}
{%- endmacro -%}

{%- macro pathClass(res, path) -%}
    {%- set name = path + res.relativeUri -%}
    {%- set name = name | replace('{', '') -%}
    {%- set name = name | replace('}', '') -%}
    {$ (name + "Endpoint") | camelCaseCapital $}
{%- endmacro -%}

{%- macro hasUrlParams(res, stack) -%}
    {%- set urlParams = 'false' -%}
    {%- for n in stack.concat(res) -%}
        {%- for v, info in n.uriParameters -%}
            {%- set urlParams = 'true' -%}
        {%- endfor -%}
    {%- endfor -%}
    {$ urlParams $}
{%- endmacro -%}

{%- macro pathSegment(res, comma) -%}

    {%- for v, info in res.uriParameters -%}
    {$ comma() $}{$ java.type(info.type) $} {$ v $}
    {%- endfor -%}

{%- endmacro -%}


{%- macro pathSegmentVar(res, comma) -%}

    {%- for v, info in res.uriParameters -%}
    {$ comma() $}{$ v $}
    {%- endfor -%}

{%- endmacro -%}

{%- macro requestType(stack, method) -%}
    {%- debug method.body['application/json'].schema -%}
    {%- if method.body['application/json'].schema -%}
        {%- set reqSchema = method.body['application/json'].schema | jsonParse -%}
        {%- if reqSchema.id -%}
            {$ typeFromId(reqSchema.id) $}
        {%- else -%}
            {$ java.type((method.method + 'Request') | camelCaseCapital) $}
        {%- endif -%}
    {%- else -%}
        Void
    {%- endif -%}
{%- endmacro -%}

{%- macro nameFromSchema(method, schema) -%}
    {%- if schema.type == 'object' -%}
        {%- if schema.id -%}
            {$ typeFromId(schema.id) $}
        {%- else -%}
            {$ (method + 'Response') | camelCaseCapital $}
        {%- endif -%}
    {%- elif schema.type == 'array' -%}
        {$ (method + 'Response') | camelCaseCapital $}
    {%- endif -%}

{%- endmacro -%}

{%- macro responseType(stack, method) -%}
    {%- set i = raml -%}
    {%- for n in stack -%}
        {%- set i = i[n.relativeUri] -%}
    {%- endfor -%}

    {%- if method.responses['200'].body['application/json'].schema -%}
        {$ nameFromSchema(method.method, method.responses['200'].body['application/json'].schema | jsonParse) $}
    {%- else -%}
        {%- set schemaName = i[method.method].responses['200'].body['application/json'].schema -%}
        {%- if not schemaName -%}
            {%- set schemaName = i[method.method].responses['200'].body['application/json'].example -%}
        {%- endif -%}

        {%- if method.schema -%}
            {$ java.type((method.method + 'Response') | camelCaseCapital) $}
        {%- else -%}
            Void
        {%- endif -%}
    {%- endif -%}

{%- endmacro -%}

{%- macro resource(node, path = "", stack = []) -%}


    {%- for res in node.resources -%}

        {%- set className = pathClass(res, path) %}


        {%- azapp className + ".java" -%}

            {% call java.wrapImports(app, '.service.api.endpoint') %}
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import bolts.Task;

@Singleton
public class {$ className $} extends {$ java.type('.service.api.RestApiEndpoint') $} {

    {%- for r in res.resources %}

    @Inject
    public {$ java.type(".service.api.endpoint." + pathClass(r, path + res.relativeUri)) $} {$ r.relativeUri | replace(r/[{}]/g, '') | camelCaseDecapital $};

    {%- endfor %}

    @Inject
    public {$ className $}() {
        mPath = "{$ path $}{$ res.relativeUri $}";
    }

                {%- for method in res.methods -%}

                    {%- set verb = method.method | capitalize -%}
                    {%- set resType = responseType(stack.concat(res), method) -%}
                    {%- set resTypeClass = resType + '.class' if resType != 'Void' else "((Class<Void>) null)" -%}


                    {%- set reqType = requestType(stack.concat(res), method) -%}
                    {%- set hasReqBody = (reqType != 'Void') -%}
                    {%- set reqTypeClass = reqType if hasReqBody else "null" -%}

                    {%- set builder = (verb + "Builder") | camelCaseDecapital %}

    /**
    {%- if method.description %}
     * {$ method.description | replace(r/\n/g, '\n     * ') $}
    {%- endif %}
     {%- for n in stack.concat(res) %}
        {%- for v, info in n.uriParameters %}
     * @param {$ v $} {$ info.displayName $}
        {%- endfor %}
     {%- endfor %}
     */
                    {%- set resHasParams = (hasUrlParams(res, stack) == 'true') %}
    public Task<{$ resType $}> {$ verb | camelCaseDecapital $}(
                    {%- set comma = joiner(', ') -%}
                    {%- for n in stack.concat(res) -%}
                        {$ pathSegment(n, comma) $}
                    {%- endfor -%}{$ (comma() + reqType + " request") if hasReqBody else "" $}
    ) {
        return {$ builder $}(
                        {%- set comma = joiner(', ') -%}
                        {%- for n in stack.concat(res) -%}
                            {$ pathSegmentVar(n, comma) $}
                        {%- endfor -%}
        ).run({$ ("request") if hasReqBody else "" $});
    }

    public {$ verb $} {$ builder $}(
                        {%- set comma = joiner(', ') -%}
                        {%- for n in stack.concat(res) -%}
                            {$ pathSegment(n, comma) $}
                        {%- endfor -%}
    ) {
        return new {$ verb $}(
                        {%- set comma = joiner(', ') -%}
                        {%- for n in stack.concat(res) -%}
                            {$ pathSegmentVar(n, comma) $}
                        {%- endfor -%});
    }

    public class {$ verb $} {

                        {%- if method.queryParameters %}
        UriComponentsBuilder mUriBuilder = mRestApi.uriBuilder(getPath());
                        {%- endif %}

                        {%- if resHasParams %}
                            {%- set urlParams = 'mUrlParams' %}
        Map<String, Object> mUrlParams = new HashMap<>();
                        {%- endif %}

                        {%- if method.securedBy.length %}
        {$ java.type('.service.api.security.ApiAuthentication') $} mAuthentication;
                            {%- for security in method.securedBy %}
                                {%- if security == 'basic' %}

        public {$ verb $} basicAuth({$ java.type('.service.api.security.ApiBasicAuthentication') $} authentication) {
            mAuthentication = authentication;
            return this;
        }
                                {%- endif %}
                            {%- endfor %}
                        {%- endif %}

        public {$ verb $} (
                        {%- set comma = joiner(', ') -%}
                        {%- for n in stack.concat(res) -%}
                            {$ pathSegment(n, comma) $}
                        {%- endfor -%}
        ) {
                        {%- for n in stack.concat(res) -%}
                            {%- for v, info in n.uriParameters %}
            mUrlParams.put("{$ v $}", {$ v $});
                            {%- endfor -%}
                        {%- endfor %}
        }

                        {%- for param, paramInfo in method.queryParameters %}

        public {$ verb $} {$ param | camelCaseDecapital $}({$ java.type(paramInfo.type) $} {$ param | camelCaseDecapital $}) {
            mUriBuilder.queryParam("{$ param $}", {$ param | camelCaseDecapital $});
            return this;
        }
                        {%- endfor %}

        private <REQ> Task<{$ resType $}> runInternal(REQ request, MediaType mediaType) {
            return mRestApi
                    .requestBuilder({$ "mUriBuilder" if method.queryParameters else "getPath()" $}, HttpMethod.{$ verb | upper $}, request, {$ resTypeClass $})
                    .mediaType(mediaType)
                    {%- if method.securedBy.length %}
                    .authentication(mAuthentication)
                    {%- endif %}
                    .execute({$ urlParams if resHasParams $});
        }

        public Task<{$ resType $}> run({$ (reqType + " request") if hasReqBody else "" $}) {
            return runInternal({$ "request" if hasReqBody else "null" $}, MediaType.APPLICATION_JSON);
        }

        {%- if method.body['multipart/form-data'] %}

        public Task<{$ resType $}> runFormData({$ (reqType + " request") if hasReqBody else "" $}) {
            return runInternal({$ "request" if hasReqBody else "null" $}.getFormData(), MediaType.MULTIPART_FORM_DATA);
        }
        {%- endif %}
    }

        {%- macro typeClass(schema, tabSize) -%}
            {%- for fname, property in schema.properties %}
        {$ tabs(tabSize) $}public {$ propertyType(property, fname) $} {$ fname | camelCaseDecapital $};
            {%- endfor %}

            {%- for fname, property in schema.properties -%}
                {%- if property.type == 'array' -%}
                    {%- set property = property.items -%}
                    {%- set fname = fname | replace(r/s$/, '') -%}
                {%- endif -%}
                {%- if property.type == 'object' %}
                {%- if not property.id %}

        {$ tabs(tabSize) $}public static class {$ propertyType(property, fname) $} {{$ typeClass(property, tabSize + 1) $}
        {$ tabs(tabSize) $}}
                {%- endif -%}
                {%- endif -%}
            {%- endfor -%}
        {%- endmacro -%}


                    {%- if method.body.schema %}
                    {%- if not method.body.schema.id %}

    public static class {$ reqType $} {
        {$ typeClass(method.body.schema, 0) $}

        {%- if method.body['multipart/form-data'] %}
        {%- set form = method.body['multipart/form-data'] %}

        private {$ java.type('org.springframework.util.MultiValueMap') $}<String, Object> getFormData() {
            MultiValueMap<String, Object> map = new {$ java.type('org.springframework.util.LinkedMultiValueMap') $}<>();
            {%- for name, param in form.formParameters %}
            map.add("{$ name $}", {$ formParam(name, param) $});
            {%- endfor %}
            return map;
        }
        {%- endif %}
    }

                    {%- endif %}
                    {%- endif %}

                    {%- if method.responses['200'].body['application/json'].schema %}
                    {%- set resSchema = method.responses['200'].body['application/json'].schema | jsonParse %}
                    {%- if not resSchema.id %}

    public static class {$ resType $}{%- if resSchema.type == 'array' %} extends {$ java.type('java.util.ArrayList') $}<{$ nameFromSchema(method.method, resSchema.items) $}>{%- endif %} {
        {$ typeClass(method.responses['200'].body['application/json'].schema, 0) $}
    }
                    {%- endif %}
                    {%- endif -%}
                {%- endfor %}
}
            {%- endcall -%}
        {% endazapp -%}
        {%- call resource(res, path + res.relativeUri, stack.concat(res)) %}{% endcall %}
    {%- endfor -%}

{%- endmacro -%}

{$ resource(api) $}
