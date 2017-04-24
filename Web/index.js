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
  var cursor = db.collection('users').find().toArray(function(err, results){
    res.send(results);
  });
});

app.post('/updateuser', (req, res) =>{
  var cursor = db.collection('users').find({username: req.body.username}).toArray(function(err, results){
    if(err) return (console.log(err));
    //If the user doesn't already exist, insert it
    if(results.length==0){
      db.collection('users').save(req.body, (err, results)=>{
        if(err) return (console.log(err));
      });
    } else {
      //If the user does exist, update the entry. Prob better way to do this
      db.collection('users').update({username: req.body.username}, {username:req.body.username, long:req.body.long, lat:req.body.lat, iszombie:req.body.iszombie});
    }
  });
  res.end('yes');
  //Not sure if need
  //var isZombie = req.query.isZombie;

});
