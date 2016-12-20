var express = require('express');
var app = express();
var path = require('path');

var server_port = 8080
var server_ip_address = '127.0.0.1'

app.use('/', express.static(path.join(__dirname, '.')));

app.listen(server_port);
console.log("Listening on " + server_ip_address + ", server_port " + server_port)
