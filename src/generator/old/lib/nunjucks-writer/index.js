var nunjucks = require('nunjucks')
var fs = require('fs-extra')
var path = require('path')
var fsp = require('fs-promise')
var Promise = require('any-promise');
var cbPromise = require('cb-promise');
var Q = require('q');

var env = new nunjucks.Environment(new nunjucks.FileSystemLoader('.'), {
    autoescape: false,
    watch: false,
    tags: {
        "variableStart": "{$",
        "variableEnd": "$}"
    }
})

nunjucks.configure({
    autoescape: false,
    watch: false
});

module.exports.configure = function(path, opts) {
    if (opts === undefined && typeof(path) === 'object') {
        opts = path
        path = undefined
    }
    if (opts) {
        if (opts['watch'] === undefined) {
            opts['watch'] = false
        }
        if (opts['autoescape'] === undefined) {
            opts['autoescape'] = false
        }
    }
    // env = new nunjucks.Environment(new nunjucks.FileSystemLoader('.'), opts)
    // env.addExtension("Azapp", new AzappExtention())
    nunjucks.configure(path, opts)
}

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

function AzappExtention() {
    this.tags = ['azapp']

    this.parse = function(parser, nodes, lexer) {


        // get the tag token
        var tok = parser.nextToken();

        // parse the args and move after the block end. passing true
        // as the second arg is required if there are no parentheses
        var args = parser.parseSignature(null, true);
        parser.advanceAfterBlockEnd(tok.value);

        // parse the body and possibly the error block, which is optional
        var body = parser.parseUntilBlocks('endazapp');

        parser.advanceAfterBlockEnd();

        // See above for notes about CallExtension
        return new nodes.CallExtension(this, 'run', args, [body]);
    }

    this.run = function(context, filename, body) {
        var res = body()
        var fullfname = path.join(context.ctx.__folder, filename);
        fs.writeFile(fullfname, res, function(err) {
        })
        return '';
    }
}


function DebugExtension() {
    this.tags = ['debug']

    this.parse = function(parser, nodes, lexer) {


        // get the tag token
        var tok = parser.nextToken();

        // parse the args and move after the block end. passing true
        // as the second arg is required if there are no parentheses
        var args = parser.parseSignature(null, true);
        parser.advanceAfterBlockEnd(tok.value);

        // See above for notes about CallExtension
        return new nodes.CallExtension(this, 'run', args);
    }

    this.run = function(context, value) {
        console.log('NJK debug:', value)
        return '';
    }
}

function underscoreFilter(str) {
    if (!str)
        return str
    var str = str.replace(/(\s|\.|\-|\/)([a-zA-Z\d])/g, function(_,y,z) {return '_' + z.toLowerCase()})
    return str.replace(/([a-z])([A-Z])/g, function(_,y,z) {return y + '_' + z.toLowerCase()})
}

function camelCaseFilter(str) {
    if (!str)
        return str
    return str.replace(/(_|\s|\.|\-|\/)([a-zA-Z])/g, function(_,y,z) {return z.toUpperCase()})
}
function camelCaseCapitalFilter(str) {
    if (!str)
        return str
    var cc = camelCaseFilter(str)
    return cc.charAt(0).toUpperCase() + cc.slice(1)
}
function camelCaseDecapitalFilter(str) {
    if (!str)
        return str
    var cc = camelCaseFilter(str)
    return cc.charAt(0).toLowerCase() + cc.slice(1)
}

function concat(a1, a2) {
    return a1.concat(a2)
}

env.addExtension("Azapp", new AzappExtention())
env.addExtension("Debug", new DebugExtension())
env.addFilter("camelCase", camelCaseFilter)
env.addFilter("camelCaseDecapital", camelCaseDecapitalFilter)
env.addFilter("camelCaseCapital", camelCaseCapitalFilter)
env.addFilter("concat", concat)
env.addFilter("jsonParse", function(str) {return JSON.parse(str)})
env.addFilter("underscore", underscoreFilter)

copyObj = function(src) {
    var dest = {}
    for (var i in src) {
        if (src.hasOwnProperty(i)) {
            dest[i] = src[i]
        }
    }
    return dest
}

module.exports.process = function(template, context, folder, callback) {
    var ctx = copyObj(context)
    ctx.__folder = folder
    //console.log('CTX1', context)
    //return cbp(callback, Q.ninvoke(env, 'render', template, context))
    return cbp(callback, fsp.ensureDir(folder)
        .then(function() {
            return env.render(template, ctx);
        }))
}

module.exports.render = function(template, context, filename, callback) {
    var dir = path.dirname(filename)
    var ctx = copyObj(context)
    ctx.__folder = dir
    // context.__folder = dir
    return cbp(callback, fsp.ensureDir(dir)
        .then(function() {
            return env.render(template, ctx);
        }).then(function(data) {
            if (data.trim().length > 0) {
                return fsp.writeFile(filename, data)
            } else {
                return null
            }
        }))
}
