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
      document.getElementById("Register-User").onsubmit = function (event) {
        event.preventDefault;
        controller.submitUser();
        return false;
      };
    },
    submitUser: function () {

      const username = document.getElementById("username-input").value;
      const password = document.getElementById("password-input").value;
      const firstName = document.getElementById("first-name-input").value;
      const lastName = document.getElementById("last-name-input").value;
      const email = document.getElementById("email-input").value;
      const phoneNumber = document.getElementById("phone-input").value;
      const role = document.getElementById("roles").value;

      const userData = {
        username: username,
        password: password,
        role: role,
        first_name: firstName,
        last_name: lastName,
        email: email,
        phone_number: phoneNumber,
      };

      base.rest.createUser(userData)
        .then(res => {
          // user registered successfully
        }).catch(err => {
          // error happened, see err
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
