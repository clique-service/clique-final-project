function getChanges()
{
  var id = getID();
  return fetch("/changes/" + id).then(function(data)
  {
    return data.json();
  });
}

function getID()
{
  return window.USER_ID;
}

setInterval(function(){
  getChanges().then(function(data)
  {
    console.log(data);
  })
}, 1000);
