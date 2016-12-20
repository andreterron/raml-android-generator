'use strict';

var fs = require('fs'),
    nunjucks = require('nunjucks'),
    nunjucksWriter = require('../nunjucks-writer/index');

function CodeGenerator(opt) {
    // hide "new"
    if (!(this instanceof CodeGenerator)) {
        return new CodeGenerator(opt);
    }
            // make params optional
    opt = opt || {};

    // this.foo = opt.foo || 'default'
    // handle other options...

    function formatName(name) {
        return name.charAt(0).toUpperCase() + name.slice(1);
    }

    function generate(model, callback) {
        var i;
        for (i in model) {
            console.log("MODEL:", i)
                // name =
            var context = {
                package: "com.devnup.hellobonus.ws.model",
                name: formatName(i),
                fields: Object.keys(model[i].fields).map(function (j) {
                    model[i].fields[j].jsonName = model[i].fields[j].jsonName || j;
                    model[i].fields[j].name = j;
                    return model[i].fields[j];
                })
            };
            // var template = 'generators/javascript/classTemplate.njk';
            var template = 'public/component/data-browser/index.njk.html'
            nunjucksWriter.configure({
                autoescape: false,
                watch: false
            })
            var filename = 'output/java/' + i + '.html'
            nunjucksWriter.render(template, context, filename, callback);
        }
    }

    this.generate = generate;
}

module.exports = CodeGenerator;
