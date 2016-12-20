'use strict';

var bowerGen = require('./bower-gen.js')
var fs = require('fs-extra')
var fsp = require('fs-promise')
var path = require('path')
var njk = require('../nunjucks-writer/index')
var Q = require('q')
var aaa = require('./njk.js')

function prepareFolder(folder, callback) {
    fs.mkdir(folder, function(err) {
        if (err && err.code !== 'EEXIST') {
            return callback(err)
        }
        callback()
    })
}

function parseModel(type) {

    function parseType(type) {
        for (var f in type.fields) {
            if (typeof type.fields[f] === 'string') {
                type.fields[f] = {
                    name: f,
                    type: type.fields[f]
                }
            }
        }
        return type
    }
}

module.exports.generate = function (data, callback) {
    var name = data.name

    var folder = data.folder || path.join('output', 'apps', name)


    fs.ensureDir(folder, function(err) {
        if (err) {
            return callback(err)
        }

        bowerGen.init(folder, data, function(err) {
            njk.configure({ tags: {
                "variableStart": "{$",
                "variableEnd": "$}"
            }})
            var context = {
                name: data.name,
                angular: {
                    deps: ['ngRoute']
                },
                resources: {
                    css: ['bower_components/bootstrap/dist/css/bootstrap.css'],
                    js: ['bower_components/angular/angular.js', 'bower_components/angular-route/angular-route.js', data.name + '.js']
                },
                types: data.model,
                begin: data.begin
            }


            fs.ensureDir(path.join(folder, 'template'), function(err) {
                if (err) {
                    return callback(err)
                }

                // var njkrender = Q.nbind(njk.render, njk)
                var promises = [
                    njk.render("lib/generator/template/index.njk.html", context, path.join(folder, 'index.html')),
                    njk.render("lib/generator/template/js/ngapp.njk.js", context, path.join(folder, data.name + '.js')),
                    fsp.copy("lib/generator/template/server.js", path.join(folder, "server.js")),
                ]
                for (var t in data.model) {
                    if (data.model.hasOwnProperty(t)) {
                        var model = parseModel(data.model[t])
                        promises.push(njk.render("lib/generator/template/template/edit-view.njk.html", data.model[t], path.join(folder, 'template', t + '-edit-view.html'), function(){}))
                        promises.push(njk.render("lib/generator/template/template/list-view.njk.html", data.model[t], path.join(folder, 'template', t + '-list-view.html'), function(){}))
                    }
                }
                Q.all(promises).nodeify(callback)
            })
        })

    })

}

module.exports.new = function(name, callback) {
//    fs.mkdir(name, callback)
}
