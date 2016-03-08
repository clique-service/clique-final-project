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
    updateUsersDiv(data);
  })
}, 1000);

function buildUser(user)
{
  var pic = $("<img>").src("http://graph.facebook.com/" + user.id + "/picture?width=150&type=square");
  var name = $("<span>").text(user.name);
  var likes = $("<span>").text("likes: " + user.likes);
  var categories = $("<span>").text("categories: " + user.categories);
  var events = $("<span>").text("events: " + user.events);
  var places = $("<span>").text("places: " + user.places);

  var user = $("<div>").append(pic, name, likes, categories, events, places);

  return user;
}

function buildUsers(users)
{
  return users.map(buildUser);
}

function updateUsersDiv(users)
{
  $("#users").empty().append(buildUsers(users));
}
