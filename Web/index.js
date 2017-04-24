var express = require('express');
var MongoClient = require('mongodb').MongoClient
var bodyParser = require('body-parser');
var app = express();

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

var db;
MongoClient.connect('mongodb://127.0.0.1:27017/zombiesvshumans', (err, database)=>{
  if(err) return console.log(err);
  db=database;
  app.listen(3000, function(){
    console.log("Listening on 3000");
  });
});

app.get('/getusers', (req, res) =>{
  //Dumps the entire user table
  var user = req.query.username;
  if(user!=null){
    var cursor = db.collection('users').find({username:user}).toArray(function(err, results){
      res.send(results);
    });
  } else {
    var cursor = db.collection('users').find().toArray(function(err, results){
      res.send(results);
    });
  }

});

app.post('/updateuser', (req, res) =>{
  var cursor = db.collection('users').find({username: req.body.username}).toArray(function(err, results){
    if(err) return (console.log(err));
    //If the user doesn't already exist, insert it
    console.log(req.body);
    if(results.length==0){
      db.collection('users').save(req.body, (err, results)=>{
        if(err) return (console.log(err));
      });
    } else {
      //If the user does exist, update the entry. Prob better way to do this
      console.log(req.body.lat!=null)
      if(req.body.username!=null && req.body.long!=null && req.body.lat!=null && req.body.iszombie!=null && req.body.lastupdated!=null ){
        db.collection('users').update({username: req.body.username}, {username:req.body.username, long:req.body.long, lat:req.body.lat, iszombie:req.body.iszombie, lastupdated:req.body.lastupdated});
      }
    }
  });
  res.end('yes');
  //Not sure if need
  //var isZombie = req.query.isZombie;

});
