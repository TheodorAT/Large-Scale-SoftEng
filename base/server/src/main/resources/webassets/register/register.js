//Written by Max Emtefall and Justin Hellsten
var base = base || {};

base.registerUserController = function () {
  "use strict";

  const user = function (userData) {
    this.username = userData.username;
    this.password = userData.password;
    this.firstName = userData.firstName;
    this.lastName = userData.lastName;
    this.email = userData.email;
    this.phoneNumber = userData.phoneNumber;
    this.role = userData.role;
  };

  const controller = {
    load: function () {
      document.getElementById("button").addEventListener("click", function () {
        controller.submitUser();
      });
    },
    submitUser: function () {
      const username = document.getElementById("username-input").value;
      const password = document.getElementById("password-input").value;
      const firstName = document.getElementById("first-name-input").value;
      const lastName = document.getElementById("last-name-input").value;
      const email = document.getElementById("email-input").value;
      const phoneNumber = document.getElementById("phone-input").value;
      const role = document.getElementById("roles").value;

      function isValidUsername(username) {
        if (username.length < 3) {
            return false;
        }    

        let users = base.rest.getUsers();
        let usernameExists = users.some(function (user) {
            return user.username == username;
        });
        return !usernameExists;
    }

      function isValidPassword(password) {
        return password.length >= 8 && /^(?=.*[^\w\s]).{8,}$/.test(password);
      }

      if (!isValidUsername(username)) {
        alert("Username is invalid.");
        return;
      }

      if (!isValidPassword(password)) {
        alert(
          "Password is invalid. It must be at least 8 characters long and contain at least one non letter character.",
        );
        return;
      }

      const userData = {
        username: username,
        password: password,
        firstName: firstName,
        lastName: lastName,
        email: email,
        phoneNumber: phoneNumber,
        role: role,
      };

      base.rest.createUser(new user(userData)).then(function () {
        alert("User registration success!");
      });
    },
  };
  return controller;
};

base.changeLocation = function (url) {
  window.location.replace(url);
};

// user registration controller
const userRegistrationController = base.registerUserController();
userRegistrationController.load();
