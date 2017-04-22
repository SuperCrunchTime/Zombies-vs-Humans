var express = require('express');
var MongoClient = require('mongodb').MongoClient

var app = express();

var db;
MongoClient.connect('mongodb://127.0.0.1:27017/zombiesvshumans', (err, database)=>{
  if(err) return console.log(err);
  db=database;
  app.listen(3000, function(){
    console.log("Listening on 3000");
  });
})
