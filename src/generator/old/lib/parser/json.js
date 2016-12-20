var moment = require('moment');
var translate = require('yandex-translate')('trnsl.1.1.20150703T020023Z.f6721ba6cc6bcd7f.c3156408e6ced98be2e13da184e88b98795a3064');

var needTranslate = true;

function ModelParserJson(opt) {
    // hide "new"
    if (!(this instanceof ModelParserJson))
        return new ModelParserJson(opt)

    // make params optional
    opt = opt || {}

    // this.foo = opt.foo || 'default'
    // handle other options...

    var typeMap = {}

    // Short-circuiting, and saving a parse operation
    function isInt(value) {
        var x;
        if (isNaN(value)) {
            return false;
        }
        x = parseFloat(value);
        return (x | 0) === x;
    }

    function formatName(name) {
        return name.charAt(0).toUpperCase() + name.slice(1);
    }

    function typeNameFromField(field) {
        return formatName(field)
    }

    function listTypeFromField(field) {
        var name = formatName(field)
        if (name.charAt(name.length - 1) == 's') {
            name = name.slice(0, name.length - 1)
        }
        return name
    }

    function typeFromName(name) {
        if (!(name in typeMap)) {
            typeMap[name] = {
                name: name,
                fields: {}
            };
        }
        return typeMap[name];
    }

    function addTypeField(type, fieldName, fieldType) {

        if (!(fieldName in type.fields)) {
            type.fields[fieldName] = {
                name: fieldName,
                type: fieldType
            };
        }
    }

    function fieldType(object) {

    }

    function typeForString(str) {
        if (moment(str, 'DD/MM/YYYY').isValid()) {
            // date type
            return typeNameFromField('Date')
        } else {
            // string type
            return typeNameFromField('String');
        }
    }

    function typeForNumber(num) {
        if (isInt(num)) {
            return typeNameFromField("integer")
        } else {
            return typeNameFromField("double")
        }
    }

    function objToModel(object, type) {
        var i;
        for (i in object) {
            var objType = typeof (object[i])
            if (Array.isArray(object[i])) {
                var tName = listTypeFromField(i)
                addTypeField(type, i, "List<" + tName + ">");
                var listType = typeFromName(tName)
                // console.log("list of", tName)
                var arr = object[i]
                var len = arr.length
                for (j = 0; j < len; j++) {
                    // console.log('i =', i, '; j = ', j)
                    // console.log('Object = ', arr[j])
                    objToModel(arr[j], listType)
                }
            } else if (objType === 'object') {
                var tName = typeNameFromField(i);
                objToModel(object[i], typeFromName(tName))
                addTypeField(type, i, tName);
            } else if (objType === 'string') {
                addTypeField(type, i, typeForString(object[i]))
            } else if (objType === 'number') {
                addTypeField(type, i, typeForNumber(object[i]));
            } else {
                addTypeField(type, i, typeNameFromField(objType));
            }
        }
        return type;
    }

    function translateNames(callback) {
        var type;
        for (type in typeMap) {

        }
    }

    function parse(json, options, callback) {
        if (typeof (options) === 'function' && callback === undefined) {
            callback = options
            options = {}
        }
        options = options || {}
        var type = typeFromName(options.baseType || 'Base')
        objToModel(JSON.parse(json), type)
        if (needTranslate) {

        }
        callback(undefined, typeMap)
    }

    this.parse = parse;
}

module.exports = ModelParserJson
