var Q = require('q')

function samePropertyType(schemaType, typeType) {
    return (typeType == schemaType || schemaType == "null" ||
            schemaType == "number" && typeType == "integer" ||
            schemaType == "integer" && typeType == "number")
}

/** Checks if this schema can be used as the type
 * @returns boolean (schema == type)
 */
function isType(schema, type) {
    if (!schema || !type.id || type.id == '' || schema.type != type.type) return false
    for (var k in schema.properties) {
        if (type.properties[k] && samePropertyType(schema.properties[k].type, type.properties[k].type)) {

        } else {
            return false
        }
    }
    // console.log("Schema", type.title, "is compatible with", schema)
    return true
}

function missingParams(type, schema) {
    var missing = 0
    for (var param in type.properties) {
        if (!(param in schema.properties)) {
            missing++
        }
    }
    return missing
}

/**
 * Maybe it should return an array of matched types?
 * @returns the type object, or null if no type
 */
function isAnyType(types, schema, service) {
    var possibleTypes = []
    for (var t in types) {
        var type = types[t]
        if (isType(schema, type)) {
            possibleTypes.push(type)
        }
    }
    if (possibleTypes.length == 1) {
        return possibleTypes[0]
    } else if (possibleTypes.length > 1) {
        var bestType = 0
        var bestMissingParams = missingParams(possibleTypes[0], schema)
        for (var i in possibleTypes) {
            var type = possibleTypes[i]

            // Checks if this type is the type of the service
            // Example: POST /albums will match with a
            //      type.id = "http://www.example.com/schema/album.json"
            var match = type.id.match(/\/([a-z\-]+).json/)
            if (!match) {
                return type
            }
            var typeid = match[1]
            if (service.indexOf(typeid) == 0) {
                return type
            }

            // Otherwise, calculates the most fit schema (less extra params)
            if (i != bestType) {
                var typeMissingParams = missingParams(type, schema)
                if (typeMissingParams < bestMissingParams) {
                    bestType = i
                    bestMissingParams = typeMissingParams
                }
            }
        }
        return possibleTypes[bestType]
    }
}

/** Recursively walks through the schema to find predefined types
 */
function findSubTypes(types, schema, service) {
    var t = isAnyType(types, schema, service)
    if (t) {
        schema.id = t.id
    } else if (schema && schema.type == 'object') {
        for (key in schema.properties) {
            findSubTypes(types, schema.properties[key], service)
        }
    } else if (schema && schema.type == 'array') {
        // console.log('ARRAY', schema)
        findSubTypes(types, schema.items, service)
        // for (key in schema.items.properties) {
        //     findSubTypes(schema.items.properties[key])
        // }
    }
}

module.exports = {
    find: findSubTypes
}
