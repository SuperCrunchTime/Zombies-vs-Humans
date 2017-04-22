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

app.get('/users', (req, res) =>{
  // console.log(req.query.tagId);
  // console.log(req.query.test1);
  var cursor = db.collection('users').find().toArray(function(err, results){
    console.log(results);
    res.send(results);
  });
  //console.log(cursor);
})
