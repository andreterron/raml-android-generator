'use strict';

var fs = require('fs');
var path = require('path');

var generator = require('../generator/index')
var appgen = require('../generator/app-gen')
var compiler = require('../compiler/index')
var jsonParser = require('../parser/json')
var definitionParser = require('../parser/definition')

module.exports.generate = function(folder, callback) {
    fs.readFile(path.join(folder, 'azapp.json'), {
        encoding: 'utf8'
    }, function (err, data) {
        if (err) {
            return console.log(err)
        }

        return definitionParser.parse(data, function (err, model) {
            if (err) {
                return console.log(err)
            }

            model.folder = folder

            appgen.generate(model, callback)

        })
    });
}
