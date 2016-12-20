parser = require './parser'
sgen = require('./old/main/sgen') # DEPRECATED
renderer = require './renderer'

module.exports = (options) ->
    parser(ramlfile: options.ramlfile)
        .then (data) ->
            sgen(data.raml, data.api)
                .then (schemas) ->
                    data.schemas = schemas
                    return data
        .then (ramlData) ->
            renderer(ramlData, options)
