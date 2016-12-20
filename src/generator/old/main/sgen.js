var Q = require('q'),
    sgen = require('generate-schema')
    ramlParser = require('raml-parser'),
    njk = require('../lib/generator/njk'),
    inquirer = require("inquirer"),
    extractor = require("./schema-xt"),
    typeFinder = require("./type-finder")

module.exports = function(raml, api) {

    var types = {}

    function findTypes(service) {
        return function(schema) {
            typeFinder.find(types, schema, service)
            return schema
        }
    }

    function bodyJson(method, currentSchema, service) {
        var body = method.body
        if (body && body['application/json']) {
            var json = body['application/json'].example
            if (typeof json == 'string' && json.trim().length > 0) {
                try {
                    return extractor.jsonExample(JSON.parse(json), currentSchema)
                } catch (e) {
                    console.log('ERROR IN JSON PARSING at ' + method.method.toUpperCase() + ' at ' + service)
                    // console.log(xtxtx)
                    // console.log(e.stack)
                }
            }
        }
        return Q(currentSchema)
    }


    function bodyForm(method, currentSchema) {
        var body = method.body
        if (body && body['multipart/form-data']) {
            var form = body['multipart/form-data']
            if (form) {
                return extractor.form(form, currentSchema)
            }
        }
        return Q(currentSchema)
    }

    function bodySchema(method, service) {
        return bodyForm(method)
            .then(function(currentSchema) {
                return bodyJson(method, currentSchema, service)
            })
            .then(findTypes(service))
            .then(function(schema) {
                if (method.body) {
                    method.body.schema = schema
                }
            })
    }

    function abc(n, service) {
        var promises = []

        for (var i in n.resources) {
            var res = n.resources[i]
            var newService = service == null ? res.relativeUri.replace("/", "") : service
            promises = promises.concat(abc(res, newService))
        }

        for (var j in n.methods) {
            var method = n.methods[j]
            // console.log("evaluating | " + n.relativeUri)
            try {

                var promise = bodySchema(method, service)

                method.jschema = ""
                // console.log(method)
                // console.log(method.responses)
                var responses = method.responses
                for (var r in responses) {
                    if (responses[r] && responses[r].body && responses[r].body['application/json']) {
                        var rrr = responses[r].body['application/json']
                        if (rrr.schema) {
                            method.jschema += "\nSchema(" + r + "):\n" + rrr.schema + "\n"
                        }
                        if (rrr.example) {
                            method.jschema += "\nExample(" + r + "):\n" + rrr.example + "\n"
                        }

                        var extxt = responses[r].body['application/json'].example
                        // if (extxt)
                        promise = (function(restxt) {
                            return promise.then(function(a) {
                                var bla
                                try {
                                    if (restxt.trim().length > 0) {
                                        bla = JSON.parse(restxt)
                                    }
                                } catch (e) {
                                    console.log('ERROR IN JSON PARSING at ' + n.relativeUri + '! ' + e)
                                    console.log(restxt)
                                    // console.log(e.stack)
                                }
                                return extractor.jsonExample(bla, a)
                                    .then(findTypes(service))
                            })
                        })(extxt)
                    }
                }



                promises.push((function (pr, method) {
                    return pr.then(function(schema) {
                        // console.log('setting method schema to ' + schema)
                        method.jschema = "\nMASTER:\n" + JSON.stringify(schema, null, 4) + "\n\n--------\n\n" + method.schema
                        method.schema = schema
                    })
                })(promise, method))
                // console.log(extxt)
            } catch (e) {
                console.log('ERRO', e.stack)
            }
            // ex = JSON.parse()
        }
        return promises;
    }



    var i, k
    var ids = {}
    for (i in api.schemas) {
        for (k in api.schemas[i]) {
            try {
                types[k] = JSON.parse(api.schemas[i][k])
                if (types[k].id) {
                    ids[types[k].id] = types[k]
                }
                types[k].id = types[k].id || ""

            } catch(e) {
                console.log("Error parsing Schema " + k)
            }
        }
    }

    // function findRefs(type) {
    //     for (key in type.properties) {
    //         var property = type.properties[key]
    //         if (property['$ref']) {
    //             type.properties[key] = ids[property['$ref']]
    //         } else if (property.type == 'array' && property.items['$ref']) {
    //             type.properties[key].items = ids[property.items['$ref']]
    //         }
    //     }
    // }
    //
    // for (i in types) {
    //     findRefs(types[i])
    // }

    console.log(types)

    // extractor.schemas(types)

    return Q.resolve(types)

    // var promises = abc(api);
    //
    // return Q.all(promises).then(function() {
    //     console.log("promises finished")
    //     return types
    // }, function(e) {
    //     console.log("promises error", e.stack)
    // });
    //api.resources[3].resources[0].methods[1].responses['200'].body['application/json'].example
}

// var raml
// var api
//
// var ramlPromise = ramlParser.loadFile('api.raml', {transform: false}).then(function(data) {
//     raml = data
// })
//
// var apiPromise = ramlParser.loadFile('api.raml').then(function(data) {
//     api = data
// })
//
// Q.all([ramlPromise, apiPromise]).then(function() {
//     return module.exports(raml, api)
// }).then(function(data) {
//     console.log('DONE')
//     console.log(data)
//     console.log('DONEDONE')
// }, function(err) {
//     console.error(err)
// })
