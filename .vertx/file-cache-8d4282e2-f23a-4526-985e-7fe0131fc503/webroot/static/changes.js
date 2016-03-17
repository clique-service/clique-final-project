function getChanges() {
  var id = getID();
  return fetch("/changes/" + id).then(function(data) {
    return data.json();
  }).catch(function(error) {
    createTimeout();
    throw error;
  });
}

function getID() {
  return window.USER_ID;
}

function handleData(data) {
    switch (data.action) {
        case ("FINISHED"): {
            $("#results-title").text("Final Results:");
            $("#logo").removeClass("rotate");
            updateUsersDiv(data.users);
            break;
        }
        case ("WAIT_NO_DATA"): {
            createTimeout();
            break;
        }
        case ("SHOW_USERS"): {
            createTimeout();
            updateUsersDiv(data.users);
            break;
        }
    }
}

function createTimeout() {
    setTimeout(callChanges, 10000);
}

function callChanges() {
  getChanges().then(handleData);
}

function buildUser(user) {
  var pic = $("<img>").addClass("profile-picture").attr("src", "//graph.facebook.com/" + user.id + "/picture?width=150&type=square");
  var name = $("<span>").addClass("name").text(user.name);
  var likes = $("<span>").addClass("details").addClass("likes").text("likes: " + user.likes);
  var categories = $("<span>").addClass("details").addClass("categories").text("categories: " + user.categories);
  var events = $("<span>").addClass("details").addClass("events").text("events: " + user.events);
  var places = $("<span>").addClass("details").addClass("places").text("places: " + user.places);
  var user = $("<div>").addClass("user").append(pic, name, likes, categories, events, places);

  return user;
}

function buildUsers(users) {
  return users.map(buildUser);
}

function updateUsersDiv(users) {
  $("#users").empty().append(buildUsers(users));
}

// Calling for the first time.
callChanges();
