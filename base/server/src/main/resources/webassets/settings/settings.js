/*
 * Model/view/controller for the Settings tab.
 * Author: Bianca Widstam
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.

base.settingsController = function () {
  "use strict";
  let currentUser;

  //Displays the current user's information
  const view = {
    render: function (user) {
      document.getElementById("userName").textContent = user.username;
      document.getElementById("name").textContent = user.first_name + " " + user.last_name;
      document.getElementById("email").textContent = user.email;
      document.getElementById("phone").textContent = user.phone_number;
      let options = controller.createRoleOptions(user.role);
      document.getElementById("select-role").replaceChildren();
      options.forEach((option) => document.getElementById("select-role").appendChild(option));
    },
  };

  const controller = {
    load: function () {
      // Loads the user from the server through the REST API, see rest.js for definition.
      base.rest.getUser().then(function (user) {
        currentUser = user;
        view.render(user);
        document.getElementById("select-role").onchange = function (event) {
          let selectedIndex = event.target.selectedIndex;
          let selectedOption = event.target.options[selectedIndex].id;
          //If role is Admin, ask the user if it wants to downgrade its role
          if (currentUser.isAdmin()) {
            const myModal = new bootstrap.Modal(document.getElementById("adminModal"));
            myModal.show();
          } else {
            base.rest.changeRole(currentUser.id, selectedOption).then(function (response) {
              base.mainController.load();
            });
          }
        };
      });
      document.getElementById("changePassword").onclick = function (event) {
        alert("TODO: changePassword");
      };
      document.getElementById("downgradeBtn").onclick = function (event) {
        let selected = document.getElementById("select-role");
        let selectedIndex = selected.selectedIndex;
        let selectedOption = selected.options[selectedIndex].id;
        base.rest.changeRole(currentUser.id, selectedOption).then(function (response) {
          base.mainController.load();
        });
      };
      document.getElementById("cancelBtn").onclick = function (event) {
        controller.load();
      };
      document.getElementById("delete").onclick = function (event) {
        alert("TODO: delete");
      };
      document.getElementById("logOut").onclick = function (event) {
        base.rest.logout().then(function (response) {
          base.changeLocation("/login/login.html");
        });
      };
    },
    createRoleOptions: function (current) {
      let options = [];
      let roles = currentUser.isAdmin() ? ["Admin", "User", "Driver"] : ["User", "Driver"];
      roles.forEach((role) => {
        let option = document.createElement("option");
        option.innerHTML = role;
        option.id = role.toLocaleUpperCase();
        if (current.label == role) {
          option.selected = true;
        }
        options.push(option);
      });
      return options;
    },
  };

  return controller;
};
