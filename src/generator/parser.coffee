ramlParser = require('raml-parser')
# Promise = require 'bluebird'
fsp = require 'fs-promise'


module.exports = (options) ->
    raml = null
    api = null
    raw = null

    ramlPromise = ramlParser.loadFile(options.ramlfile, {transform: false, validate: false})
        # .then (data) ->
        #     raml = data

    #  apiPromise = ramlParser.loadFile('api.raml').then(function(data) {
    #     api = data
    # })

    #  apiPromise = Q.nfcall(fs.readFile, 'api.raml', 'utf-8')
    #     .then(function(text) {
    #          notInclude = text.replace(/(^|\n)(\s*)(example:\s*)\!include (.*)(\s*\n|$)/g, '$1$2$3!include $4\n$2exampleFile: "$4"$5')
    apiPromise = ramlParser.loadFile options.ramlfile,
        validate: false,
        reader: new ramlParser.FileReader (file) ->
            fsp.readFile(file).then (data) ->
                data.toString().replace(/(^|\n)(\s*)(example:\s*)\!include (.*)(\s*\n|$)/g, '$1$2$3!include $4\n$2exampleFile: "$4"$5')

            # new Promise (resolve, reject) ->
            #     _this = this;
            #     deferred = this.q.defer();
            #     require('fs').readFile(file, function(err, data) {
            #         if (err) {
            #             return reject(new exports.FileError("while reading " + file, null, "cannot read " + file + " (" + err + ")", _this.start_mark));
            #         } else {
            #              notInclude = data.toString().replace(/(^|\n)(\s*)(example:\s*)\!include (.*)(\s*\n|$)/g, '$1$2$3!include $4\n$2exampleFile: "$4"$5')
            #             return resolve(notInclude);
            #         }
            #     })
    # }).then (data) ->
    #     api = data

    Promise.all [ramlPromise, apiPromise]
        .then (res) ->
            raml: res[0]
            api: res[1]
