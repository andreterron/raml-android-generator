njk = require('./old/lib/generator/njk')

module.exports = (ramlData)->
    # for (var skey in schemas) {
    #     fs.writeFile('auto-schemas/' + skey + '.json', JSON.stringify(schemas[skey], null, 4), function() {})
    # }
    ramlData.app =
        schemaUrlId: 'http://www.topcal.co/api/v1/schema',
        android: package: 'co.topcal.app'
    njk.generate('./src/templates', './output', ramlData)
