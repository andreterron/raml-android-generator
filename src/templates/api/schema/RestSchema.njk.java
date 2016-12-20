{%- import "./../../javaBase.njk" as java -%}

{%- macro propertyType(property, name) -%}
{%- if property.enum -%}
    {$ name | camelCaseCapital $}
{%- elif property.type == 'array' -%}
    {$ java.type('java.util.ArrayList') $}<{% call propertyType(property.items, name | replace(r/s$/, '')) %}{% endcall %}>
{%- elif property.type == 'object' -%}
    {%- if property.id -%}
        {$ java.type('Api' + (property.title | camelCaseCapital)) $}
    {%- else -%}
        Object{# $ java.type(name | camelCaseCapital) $ #}
    {%- endif -%}
{%- elif property.type == 'number' -%}
    {$ java.type('java.math.BigDecimal') $}
{%- elif property.type == 'integer' -%}
    Integer
{%- elif not property.type -%}
    Void
{%- else -%}
    {$ java.type(property.type) $}
{%- endif -%}
{%- endmacro -%}

{%- macro validProperty(property) -%}
{%- if property.type == 'array' -%}
    {$ validProperty(property.items) $}
{%- elif property.type == 'object' -%}
    {%- if property.id -%}
        true
    {%- else -%}
        {# FALSE #}
    {%- endif -%}
{%- elif not property.type -%}
    {# FALSE #}
{%- else -%}
    true
{%- endif -%}
{%- endmacro -%}

{%- macro parcelType (property) -%}
{$ propertyType(property) | camelCaseCapital $}
{%- endmacro -%}

{% macro parcelRead(property, parcel, name) -%}
{%- if property.enum -%}
    ({$ propertyType(property, name) $}) {$ parcel $}.readSerializable()
{%- elif property.type == 'array' -%}
    {%- if property.items.type == 'string' -%}
        {$ parcel $}.createStringArrayList()
    {%- else -%}
        {$ parcel $}.createTypedArrayList({$ propertyType(property.items, name | replace(r/s$/, '')) $}.CREATOR)
    {%- endif -%}
{%- elif property.type == 'boolean' -%}
    {$ parcel $}.readInt() == 1
{%- elif property.type == 'number' -%}
    ({$ propertyType(property, name) $}) {$ parcel $}.readSerializable()
{%- elif property.id -%}
    {$ parcel $}.readParcelable(ClassLoader.getSystemClassLoader())
{%- else -%}
    {$ parcel $}.read{$ (propertyType(property, name) | camelCaseCapital) | replace("Integer", "Int") $}()
{%- endif -%}
{%- endmacro -%}


{% macro parcelWrite(property, writeVar) -%}
{%- if property.enum -%}
    Serializable({$ writeVar $})
{%- elif property.type == 'array' -%}
    {%- if property.items.type == 'string' -%}
        StringList({$ writeVar $})
    {%- else -%}
        TypedList({$ writeVar $})
    {%- endif -%}
{%- elif property.type == 'boolean' -%}
    Int({$ writeVar $} ? 1 : 0)
{%- elif property.type == 'number' -%}
    Serializable({$ writeVar $})
{%- elif property.type == 'integer' -%}
    Int({$ writeVar $} != null ? {$ writeVar $} : 0)
{%- elif property.id -%}
    Parcelable({$ writeVar $}, 0)
{%- else -%}
    {$ (propertyType(property, writeVar) | camelCaseCapital) | replace("Integer", "Int") $}({$ writeVar $})
{%- endif -%}
{%- endmacro -%}

{# % for schemaObj in schemas -%#}
{% for type, schema in schemas -%}
{% if schema.id | replace(app.schemaUrlId, "SCHEMAURLID") | replace(r/^SCHEMAURLID.*$/g, 'SCHEMATYPE') == 'SCHEMATYPE' %}

{# % set schema = schema | jsonParse % #}
{% set typeClass = ("Api" + type | camelCaseCapital) %}


{% azapp typeClass + ".java" -%}
{% call java.wrapImports(app, '.service.api.schema') %}
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by andreterron on 9/4/15.
 */
public class {$ typeClass $} implements Parcelable {
    {% for fname, property in schema.properties %}
    {%- if validProperty(property).val.length %}
    {$ "@" + java.type("android.support.annotation." + ("NonNull" if property.required else "Nullable")) $}
    public {$ propertyType(property, fname) $} {$ fname | camelCaseDecapital $};
    {%- endif %}
    {% endfor %}

    public {$ typeClass $}() {

    }

    // Parcel

    public {$ typeClass $}(Parcel in) {
        {%- for fname, property in schema.properties %}
        {%- if validProperty(property).val.length %}
        {$ fname | camelCaseDecapital $} = {$ parcelRead(property, 'in', fname) $};
        {%- endif %}
        {%- endfor %}
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        {%- for fname, property in schema.properties %}
        {%- if validProperty(property).val.length %}
        dest.write{$ parcelWrite(property, fname | camelCaseDecapital) $};
        {%- endif %}
        {%- endfor %}
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public {$ typeClass $} createFromParcel(Parcel in) {
            return new {$ typeClass $}(in);
        }

        public {$ typeClass $}[] newArray(int size) {
            return new {$ typeClass $}[size];
        }
    };

    {%- for fname, property in schema.properties %}
        {%- if property.enum %}

    public enum {$ fname | camelCaseCapital $} {
            {%- set comma = joiner(',') %}
            {%- for val in property.enum -%}
        {$ comma() $}
        {$ val $}
            {%- endfor %}
    }
        {%- endif %}
    {%- endfor %}

}
{% endcall %}
{%- endazapp -%}
{% endif %}
{# %- endfor -% #}
{%- endfor -%}
