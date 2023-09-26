/*
 * Routing for startpage.
 * Author: Justin Hellsten, Max Emtefall
 */

var base = base || {};

let loginButton = document.getElementById("login-button");
let createButton = document.getElementById("create-button");

loginButton.addEventListener("click", function () {
  base.changeLocation("/login/login.html");
});

createButton.addEventListener("click", function () {
  base.changeLocation("/register/register.html");
});

base.changeLocation = function (url) {
  window.location.replace(url);
};
