var moment = require('moment');
var fs = require('fs');

function ModelGroup(opt) {

    // hide "new"
    if (!(this instanceof ModelGroup))
        return new ModelGroup(opt)

    // make params optional
    opt = opt || {}

    this.native = opt.native || 'example/native.json'
    this.encoding = opt.encoding || 'utf8'
    // handle other options...

    fs.readFile(this.native, {
        encoding: this.encoding
    }, function(err, data) {
        if (err) {

        }
    })

}



module.exports = {
    ModelGroup: ModelGroup
}
