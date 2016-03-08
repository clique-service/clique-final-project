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

function handleData(data) {
    switch (data.action) {
        case ("FINISHED"): {
            $("#results-title").text("Final Results:");
            clearInterval(window.fetchInterval);
            updateUsersDiv(data.users);
            break;
        }
        case ("WAIT_NO_DATA"): {
            break;
        }
        case ("SHOW_USERS"): {
            updateUsersDiv(data.users);
            break;
        }
    }
}

function callChanges() {
  getChanges().then(handleData);
}

window.fetchInterval = setInterval(callChanges, 10000);
callChanges();

function buildUser(user)
{
  var pic = $("<img>").addClass("profile-picture").attr("src", "http://graph.facebook.com/" + user.id + "/picture?width=150&type=square");
  var name = $("<span>").addClass("name").text(user.name);
  var likes = $("<span>").addClass("details").addClass("likes").text("likes: " + user.likes);
  var categories = $("<span>").addClass("details").addClass("categories").text("categories: " + user.categories);
  var events = $("<span>").addClass("details").addClass("events").text("events: " + user.events);
  var places = $("<span>").addClass("details").addClass("places").text("places: " + user.places);
  var user = $("<div>").addClass("user").append(pic, name, likes, categories, events, places);

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
