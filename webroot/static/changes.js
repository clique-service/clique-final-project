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

function icon(type) {
    return $("<i />").addClass("fa").addClass("fa-" + type);
}

function handleData(data) {
    switch (data.action) {
        case ("FINISHED"): {
            $("#logo").removeClass("rotate").addClass("results");
            $("#first-part").removeClass("working").addClass("results");
            updateUsersDiv(data.users);
            break;
        }
        case ("WAIT_NO_DATA"): {
            createTimeout();
            break;
        }
        case ("SHOW_USERS"): {
            $("#first-part").addClass("working");
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

function capitalize(x) {
    return x.charAt(0).toUpperCase() + x.slice(1);
}

function capitalizeName(name) {
    return name.split(" ").map(capitalize).join(" ");
}

function wrapWithIcon(text, type) {
    return [icon(type), $("<span>").text(text)];
}

function buildUser(user) {
  var pic = $("<img>").addClass("profile-picture").attr("src", "//graph.facebook.com/" + user.id + "/picture?width=150&type=square");
  var link = $("<a>").addClass("profile-link").attr("href", "https://facebook.com/" + user.id).append(pic);
  var name = $("<span>").addClass("name").text(capitalizeName(user.name));
  var likes = $("<span>").addClass("details").addClass("likes").append(wrapWithIcon(" likes: " + user.likes, 'thumbs-up'));
  var categories = $("<span>").addClass("details").addClass("categories").append(wrapWithIcon("categories: " + user.categories, 'hashtag'));
  var events = $("<span>").addClass("details").addClass("events").append(wrapWithIcon("events: " + user.events, 'calendar'));
  var places = $("<span>").addClass("details").addClass("places").append(wrapWithIcon("places: " + user.places, 'map-marker'));
  var user = $("<div>").addClass("user").append(link, name, likes, categories, events, places);

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
