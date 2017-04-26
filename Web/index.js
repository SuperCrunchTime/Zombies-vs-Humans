var express = require('express');
var MongoClient = require('mongodb').MongoClient
var bodyParser = require('body-parser');
var app = express();

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

//add post request for tagging a user

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
  console.log(db.collection('users').stats());
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
    if(results.length==0){
      db.collection('users').save(req.body, (err, results)=>{
        if(err) return (console.log(err));
      });
    } else {
      //If the user does exist, update the entry. Prob better way to do this
      if(req.body.long!=null){
        db.collection('users').update({username: req.body.username}, { $set: {long:req.body.long } });
      }
      if(req.body.lat!=null){
        db.collection('users').update({username: req.body.username}, { $set: {lat:req.body.lat } });
      }
      if(req.body.iszombie!=null){
        db.collection('users').update({username: req.body.username}, { $set: {iszombie:req.body.iszombie } });
      }
      if(req.body.lastupdated!=null){
        db.collection('users').update({username: req.body.username}, { $set: {lastupdated:req.body.lastupdated } });
      }

      var dbSize;
      db.collection('users').count((err, results)=>{
        if(err) return (console.log(err));
        dbSize = results;
      });

      db.collection('users').count({iszombie:'true'}, (err, result)=>{
        if(err) return (console.log(err));
        if(result==dbSize){
          console.log("New Game");
          db.collection('users').updateMany({ }, {$set:{iszombie:"false"}});

          var randomNumber = Math.floor(Math.random()*dbSize);

          db.collection('users').find().limit(-1).skip(randomNumber).next((err, result)=>{
            db.collection('users').update({username: result.username}, {$set:{iszombie:"true"}});
            console.log(result.username +" is now the zombie");
          });
        }
      });

    }
  });
  res.end('yes');
  //change this to got tagged for tagged, and new game for new game
});

app.post('/taguser', (req, res) =>{
  db.collection('users').find({username: req.body.username}).toArray((err, results)=>{
    if(err) return (console.log(err));
    if(results.length>0){
      if(req.body.iszombie!=null){
        db.collection('users').update({username: req.body.username}, { $set: {iszombie:"true" } });
      }
    }
  })
});
