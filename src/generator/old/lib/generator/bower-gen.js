var bower = require('bower')
var fs = require('fs')
var path = require('path')
var util = require('util');

module.exports.init = function (folder, data, callback) {
    var bJson = {
        "name": data.name,
        "version": "0.0.1",
        "description": "Demo app from Zapplean",
        // "main": [
        //     "js/motion.js",
        //     "sass/motion.scss"
        //   ],
        "dependencies": {
            "bootstrap": "~3.3.5",
            "angular": "~1.3.17",
            "angular-route": "~1.3.17",
            "zpl-list": "/Users/andreterron/Root/Code/model-gen/zpl-list/"
        }
    }

    fs.writeFile(path.join(folder, 'bower.json'), JSON.stringify(bJson, null, 4), function(err) {
        if (err) {
            return callback(err)
        }

        var currentPath = process.cwd()
        
        process.chdir(folder)
        bower.commands.install([], {}, {})
        .on("end", function(installed) {
            process.chdir(currentPath)
            callback()
        }).on('error', function(err) {
            console.log('err', util.inspect(err, false, 4))
            callback(err)
        })
    });
}
