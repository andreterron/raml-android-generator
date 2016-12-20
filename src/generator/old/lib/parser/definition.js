'use strict';

module.exports.parse = function (json, options, callback) {

    if (typeof (options) === 'function' && callback === undefined) {
        callback = options
        options = {}
    }
    options = options || {}

    var obj = JSON.parse(json)

    callback(undefined, obj)
}
