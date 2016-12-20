'use strict';

var {$ ngapp $} = angular.module('{$ name $}', [
    {%- for dep in angular.deps -%}
        '{$ dep $}'
        {%- if not loop.last %}, {% endif -%}
    {%  endfor %}])
