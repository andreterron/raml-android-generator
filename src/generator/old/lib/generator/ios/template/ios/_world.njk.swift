{% for type in model %}
{% azapp type + ".swift" -%}

import Foundation

func hello() {
    let appName = "{$ appName $}"
    let type = "{$ type $}"
}
{%- endazapp %}
{% endfor %}
