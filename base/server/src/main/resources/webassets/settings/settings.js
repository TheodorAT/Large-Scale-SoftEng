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
        window.location.hash = "#/change-password";
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
      //Displays a modal when delete-button is clicked which lets the user confirm removal of account
      document.getElementById("delete").onclick = function (event) {
        //If the user is admin, the account cannot be deleted by themselves
        if (currentUser.isAdmin()) {
          const adminModal = new bootstrap.Modal(document.getElementById("adminDelete"));
          adminModal.show();
        } else {
          const deleteModal = new bootstrap.Modal(document.getElementById("deleteModal"));
          deleteModal.show();
        }
      };
      //When delete button in modal is clicked, the user is logged out and redirected to login-page
      document.getElementById("deleteBtn").onclick = () => {
        const deleteUserId = currentUser.id;
        base.rest.logout().then(function (response) {
          base.changeLocation("/login/login.html");
        });
        // Calls REST API to delete user
        base.rest.deleteUser(deleteUserId);
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
