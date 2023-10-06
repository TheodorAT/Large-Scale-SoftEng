/*
 * Model/view/controller for the Settings tab.
 * Author: ..
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.

base.settingsController = function () {
  "use strict"; // add this to avoid some potential bugs

  const view = {
    render: function () {},
  };

  const controller = {
    load: function () {
      document.getElementById("userName").textContent = "TODO";
      document.getElementById("name").textContent = "TODO";
      document.getElementById("email").textContent = "TODO";
      document.getElementById("phone").textContent = "TODO";
      document.getElementById("role").textContent = "TODO";
      document.getElementById("changePassword").onclick = function (event) {
        alert("TODO: changePassword");
      };
      document.getElementById("delete").onclick = function (event) {
        alert("TODO: delete");
      };
      document.getElementById("logOut").onclick = function (event) {
        alert("TODO: logout");
      };
      document.getElementById("changeRole").onclick = function (event) {
        alert("TODO: changerole");
      };
    },
  };

  return controller;
};
