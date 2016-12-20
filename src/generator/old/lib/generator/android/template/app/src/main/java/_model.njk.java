{% macro pkgDir() -%}
{$ package | replace('.', '/') $}/
{%- endmacro %}

{% macro className() -%}
{$ type | camelCase $}
{%- endmacro %}

{% for type in model %}
{% azapp pkgDir() + "/" + className() + ".java" -%}

package {$ package $};

class {$ type | camelCase $} {

}
{%- endazapp %}
{% endfor %}
