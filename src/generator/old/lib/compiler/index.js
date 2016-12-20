'use strict';

var fs = require('fs'),
    path = require('path'),
    nunjucksWriter = require('../nunjucks-writer/index')

module.exports.compile = function(libpath, context, cb) {
    // config = require(path + "config.json")
    fs.readFile(path.join(libpath, "config.json") ,{
        encoding: 'utf8'
    }, function(err, res) {
        if (err) {
            return cb(err)
        }

        var opt = JSON.parse(res)

        if (!("autoescape" in opt.config)) {
            opt.config.autoescape = false
        }

        nunjucksWriter.configure(opt.config)
        nunjucksWriter.render(libpath + "index.njk.html", context, libpath + "index.html", cb)
    })
}
