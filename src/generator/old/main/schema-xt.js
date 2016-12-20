var Q = require('q'),
    generateSchema = require('generate-schema')

var types = {}
//
// function samePropertyType(schemaType, typeType) {
//     return (typeType == schemaType || schemaType == "null" ||
//             schemaType == "number" && typeType == "integer" ||
//             schemaType == "integer" && typeType == "number")
// }
//
// /** Checks if this schema can be used as the type
//  * @returns boolean (schema == type)
//  */
// function isType(schema, type) {
//     if (!schema || !type.id || type.id == '' || schema.type != type.type) return false
//     for (var k in schema.properties) {
//         if (type.properties[k] && samePropertyType(schema.properties[k].type, type.properties[k].type)) {
//
//         } else {
//             return false
//         }
//     }
//     // console.log("Schema", type.title, "is compatible with", schema)
//     return true
// }
//
// function missingParams(type, schema) {
//     var missing = 0
//     for (var param in type.properties) {
//         if (!(param in schema.properties)) {
//             missing++
//         }
//     }
//     return missing
// }
//
// /**
//  * Maybe it should return an array of matched types?
//  * @returns the type object, or null if no type
//  */
// function isAnyType(schema, service) {
//     var possibleTypes = []
//     for (var t in types) {
//         var type = types[t]
//         if (isType(schema, type)) {
//             possibleTypes.push(type)
//         }
//     }
//     if (possibleTypes.length == 1) {
//         return possibleTypes[0]
//     } else if (possibleTypes.length > 1) {
//         var bestType = 0
//         var bestMissingParams = missingParams(possibleTypes[0], schema)
//         for (var i in possibleTypes) {
//             if (i != bestType) {
//                 var type = possibleTypes[i]
//                 var typeid = type.id.match(/\/([a-z\-]+).json/)[1]
//                 var typeMissingParams = missingParams(type, schema)
//                 if (typeMissingParams < bestMissingParams) {
//                     bestType = i
//                     bestMissingParams = typeMissingParams
//                 }
//             }
//         }
//         return possibleTypes[bestType]
//     }
// }
//
// /** Recursively walks through the schema to find predefined types
//  */
// function findSubTypes(schema, service) {
//     var t = isAnyType(schema, service)
//     if (t) {
//         schema.id = t.id
//     } else if (schema && schema.type == 'object') {
//         for (key in schema.properties) {
//             findSubTypes(schema.properties[key], service)
//         }
//     } else if (schema && schema.type == 'array') {
//         // console.log('ARRAY', schema)
//         findSubTypes(schema.items, service)
//         // for (key in schema.items.properties) {
//         //     findSubTypes(schema.items.properties[key])
//         // }
//     }
// }

function extractJsonSchema(schema, methodSchema) {
    // findSubTypes(schema)
    // findSubTypes(methodSchema)
    // console.log("METHOD SCHEMA = " + methodSchema)
    if (methodSchema !== undefined){
        if (!schema || schema.type == 'undefined') {
            return Q(methodSchema)
        }

        if (methodSchema.type === undefined) {
            console.log("METHOD SCHEMA ERROR")
            // schema.OMGerror = "NO TYPE"
            // methodSchema.OMGerror2 = "NOTYPE2"
            return Q(schema)
        }
        // methodSchema.push(schema)
        if (methodSchema.type != schema.type) {
            // methodSchema.different = "DIFFERENT"
            return Q(methodSchema)
        } else if (schema.type == 'object') {
            // methodSchema.mytype = "WE OBJECT"
            var promise = Q()
            if (methodSchema.properties) {
                for (key in schema.properties) {
                    if (key in methodSchema.properties) {
                    } else {
                        methodSchema.properties[key] = schema.properties[key]
                    }
                    methodSchema.properties[key].title = key
                    promise = (function(prom, k) {
                        return prom.then(extractJsonSchema(schema.properties[k], methodSchema.properties[k]))
                    })(promise, key)
                    // promise.then(extractJsonSchema(schema.properties[key], methodSchema.properties[key]))
                }
            }
            return promise.then(function() {
                return Q(methodSchema)
            })
        } else {
            // findSubTypes(methodSchema)
            methodSchema.mytype = "ELSE: " + schema.type
            return Q(methodSchema)
        }
    } else if (!schema || schema.type == 'undefined') {
        return Q(undefined)
    } else {
        // findSubTypes(schema)
        return Q(schema)
    }
}

function extractFormData(form, currentSchema) {
    var schema = {
        type: 'object',
        properties: {}
    }

    for (var name in form.formParameters) {
        var param = form.formParameters[name]
        schema.properties[name] = {
            type: param.type,
            description: param.description,
        }
        if (param.required) {
            schema.properties[name].required = true
        }
    }

    return extractJsonSchema(schema, currentSchema)
}

module.exports = {

    // extracts and merges a json schema
    json: extractJsonSchema,

    // extracts JsonSchema from an Example Json
    jsonExample: function(example, currentSchema) {
            var jsonSchema = generateSchema.json(example)
            return extractJsonSchema(jsonSchema, currentSchema)
    },

    form: extractFormData,

    schemas: function(t) {
        types = t
    }

}
