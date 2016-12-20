var njk = require('./njk.js')

njk.generate('./android/template', './android/output', {
    appName: "DoAtom",
    model: ['field', 'type', 'relation'],
    package: "com.zapplean.doatom"
}, function(e) {
    console.log('DONE', e)
})
