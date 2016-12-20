'use strict';

var fs = require('fs'), path = require('path')
var njk = require('../nunjucks-writer/index')

function prepareFolder(folder, callback) {
    fs.mkdir(folder, function(err) {
        if (err && err.code !== 'EEXIST') {
            return callback(err)
        }
        callback()
    })
}

module.exports.init = function(folder, data, callback) {
    var jspath = path.join(folder, 'js')
    prepareFolder(jspath, function(err) {
        // compile app.js
        // for each model:
        //      compile controller
        //      compile dao
        //      // TODO : think about routes
    })
}