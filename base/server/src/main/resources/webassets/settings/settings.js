/*
 * Model/view/controller for the Settings tab.
 * Author: Bianca Widstam
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.

base.settingsController = function () {
  "use strict"; // add this to avoid some potential bugs

  const view = {
    render: function (user) {
      console.log(user);
      document.getElementById("userName").textContent = user.username;
      document.getElementById("name").textContent = user.first_name + " " + user.last_name;
      document.getElementById("email").textContent = user.email;
      document.getElementById("phone").textContent = user.phone_number;
      document.getElementById("role").textContent = user.role.label;
    },
  };

  const controller = {
    load: function () {
      let userPromise = base.rest.getUser().then(function (user) {
        view.render(user);
      });
      document.getElementById("changePassword").onclick = function (event) {
        alert("TODO: changePassword");
      };
      document.getElementById("delete").onclick = function (event) {
        alert("TODO: delete");
      };
      document.getElementById("logOut").onclick = function (event) {
        base.rest.logout().then(function (response) {
          base.changeLocation("/login/login.html");
        });
      };
      document.getElementById("changeRole").onclick = function (event) {
        alert("TODO: changerole");
      };
    },
  };

  return controller;
};
