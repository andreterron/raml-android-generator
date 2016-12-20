var fswalk = require('fswalk'),
    skywalker = require('skywalker'),
    fs = require('fs-extra'),
    fsp = require('fs-promise'),
    path = require('path'),
    njk = require('../nunjucks-writer/index'),
    // ejs = require('../ejs-writer/ejs'),
    Q = require('q')


njk.configure({ tags: {
    "variableStart": "{$",
    "variableEnd": "$}"
}})

function isAzapp(name) {
    return name == '_azapp.js' // ? ou _azapp.json ?
}

function isHidden(name) {
    return name.charAt(0) == '_'
}

function isNjk(name) {
    return !!name.match('.njk')
}

function isEjs(name) {
    return !!name.match('.ejs')
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

module.exports.generate = function(dir, destination, context, callback) {

    var deferred = Q.defer();
    skywalker(dir)
        .ignoreDotFiles()
        //.ignore(/(^|\/)_.*?$/g)
        // .filter(/./g, function(next, done) {
        //     var relative = path.relative(dir, this._.path)
        //     this.destination = path.join(destination, relative)
        //     console.log('1PATH =', this._.path)
        //     console.log('1DEST =', this.destination)
        //     next()
        // })
        // .filter(/(^|\/)_.*\.njk.*$/g, function(next, done) {
        //     console.log("FOUND COOL FILE!")
        //     next()
        // })
        .filter(/_azapp\.js$/g,function(next,done){
            var file = this;
            // require() // ?
            // TODO : run azapp script
            next()
        })
        .directoryFilter(null, function(next, done) {
            if (this._.isDirectory) {
                var relative = path.relative(dir, this._.path)
                var dest = path.join(destination, relative)
                fs.ensureDir(dest, function(err) {
                    if (err) {
                        return done(err, false)
                    }
                    next()
                })
            } else {
                next()
            }
        })
        .on('file', function(file) {
            var name = file._.filename

            var relative = path.relative(dir, file._.path)
            var dest = path.join(destination, relative)
            if (isNjk(name)) {
                if (isHidden(name)) {
                    var destDir = path.join(destination, path.relative(dir, file._.dirname))
                    njk.process(file._.path, context, destDir, function(err) {
                        if (err) console.error(err.stack || err)
                    })
                } else {
                    // compile(file)
                    dest = dest.replace('.njk', '')
                    njk.render(file._.path, context, dest, function(err) {
                        if (err) console.error(err.stack || err)
                        // console.log("FILE - FINISHED RENDER")
                    })
                }
            // }  else if (isEjs(name)) {
                // if (isHidden(name)) {
                //     var destDir = path.join(destination, path.relative(dir, file._.dirname))
                //     // ejs.process(file._.path, context, destDir, function(err) {
                //     //     if (err) console.log(err.stack)
                //     // })
                // } else {
                //     // compile(file)
                //     dest = dest.replace('.ejs', '')
                //     ejs.render(file._.path, context, dest, function(err) {
                //         if (err) console.log(err.stack)
                //     })
                // }
            } else {
                fs.copy(file._.path, dest, function(err) {
                    if (err) console.error(err.stack || err)
                })
            }
        })
        .start(function(err, file) {
            if (!err) {
                deferred.resolve()
            } else {
                deferred.reject(err)
            }
        })

        return cbp(callback, deferred.promise)
    // fswalk(dir, function(file, stats) {
    //     var name = path.basename(file)
    //
    //     if (isAzapp(name)) {
    //         // run script
    //
    //     } else if (ignore(name)) {
    //         // do nothing
    //     } else if (isNjk(name)) {
    //         // compile(file)
    //     } else {
    //         // copy(file)
    //     }
    // }, callback);

}
