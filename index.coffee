gen = require './src/generator'

gen(ramlfile: './samples/topcal/api.raml')
    .then (r) ->
        console.log 'DONE'
        console.log r
    .catch (e) ->
        console.error e.stack || e
