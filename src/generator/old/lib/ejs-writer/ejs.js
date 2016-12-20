var ejs = require('ejs')
var fs = require('fs-extra')
var path = require('path')
var fsp = require('fs-promise')
var Promise = require('any-promise');
var cbPromise = require('cb-promise');
var Q = require('q');

// nunjucks.configure({
//     autoescape: false,
//     watch: false
// });

// module.exports.configure = function(path, opts) {
//     if (opts === undefined && typeof(path) === 'object') {
//         opts = path
//         path = undefined
//     }
//     if (opts) {
//         if (opts['watch'] === undefined) {
//             opts['watch'] = false
//         }
//         if (opts['autoescape'] === undefined) {
//             opts['autoescape'] = false
//         }
//     }
//     // env = new nunjucks.Environment(new nunjucks.FileSystemLoader('.'), opts)
//     // env.addExtension("Azapp", new AzappExtention())
//     nunjucks.configure(path, opts)
// }

var mcbp = function(callback) {
    var resolve, reject
    var promise = new Promise(function(_resolve, _reject) {
        resolve = _resolve;
        reject = _reject;
    })
    return {
        reject: reject
    }
    if (callback) {
        return callback
    }
    return function(err, data) {

    }
}

var cbp = function(callback, promise) {
    if (callback) {
        promise.then(function(data) {
            // TODO : allow multiple params
            callback(undefined, data)
        }, function(err) {
            callback(err)
        })
    } else {
        return promise
    }
}

// function AzappExtention() {
//     this.tags = ['azapp']
//
//     this.parse = function(parser, nodes, lexer) {
//
//
//         // get the tag token
//         var tok = parser.nextToken();
//
//         // parse the args and move after the block end. passing true
//         // as the second arg is required if there are no parentheses
//         var args = parser.parseSignature(null, true);
//         parser.advanceAfterBlockEnd(tok.value);
//
//         // parse the body and possibly the error block, which is optional
//         var body = parser.parseUntilBlocks('endazapp');
//
//         parser.advanceAfterBlockEnd();
//
//         // See above for notes about CallExtension
//         return new nodes.CallExtensionAsync(this, 'run', args, [body]);
//     }
//
//     this.run = function(context, filename, body, callback) {
//         //console.log('CTX2', context.ctx)
//         var res = body()
//         fs.writeFile(path.join(context.ctx.__folder, filename), res, function(err) {
//             callback(err, '')
//         })
//     }
// }

function camelCaseFilter(str) {
    return str.replace(/(_|\s|\.|\-|\/)([a-zA-Z])/g, function(_,y,z) {return z.toUpperCase()})
}
function camelCaseCapitalFilter(str) {
    var cc = camelCaseFilter(str)
    return cc.charAt(0).toUpperCase() + cc.slice(1)
}
function camelCaseDecapitalFilter(str) {
    var cc = camelCaseFilter(str)
    return cc.charAt(0).toLowerCase() + cc.slice(1)
}

// function concat(a1, a2) {
//     return a1.concat(a2)
// }

// env.addExtension("Azapp", new AzappExtention())
// env.addFilter("camelCase", camelCaseFilter)
// env.addFilter("camelCaseDecapital", camelCaseDecapitalFilter)
// env.addFilter("camelCaseCapital", camelCaseCapitalFilter)
// env.addFilter("concat", concat)
// env.addFilter("jsonParse", function(str) { return JSON.parse(str) })

module.exports.process = function(template, context, folder, callback) {
    context.__folder = folder
    return cbp(callback, Q.ninvoke(ejs, 'render', template, context))
}

module.exports.render = function(template, context, filename, callback) {
    context.__folder = path.dirname(filename)
    return cbp(callback, Q.ninvoke(ejs, 'renderFile', template, context)
        .then(function(data) {
            if (data.trim().length > 0) {
                return fsp.writeFile(filename, data)
            } else {
                return null
            }
        }))
}
