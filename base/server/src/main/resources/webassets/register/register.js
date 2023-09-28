/**
 * JavaScript for creating an account in the Carpooling service.
 * Written by Max Emtefall and Justin Hellsten.
 */
var base = base || {};
base.changeLocation = function (url) {
  window.location.replace(url);
};

/**
 * Constructor for the user object with user data.
 * @constructor
 *
 */
const user = function (userData) {
  this.username = userData.username;
  this.password = userData.password;
  this.firstName = userData.firstName;
  this.lastName = userData.lastName;
  this.email = userData.email;
  this.phoneNumber = userData.phoneNumber;
  this.role = userData.role;
};

/**
 * Controller for user registration.
 * @returns The user registration controller object.
 */
base.registerController = (function () {
  const controller = {
    //Loads the controller, adds event listeners.
    load: function () {
      document.getElementById("Register-User").onsubmit = function (event) {
        event.preventDefault();
        controller.submitUser();
      };
    },

    /**
     * Validation then submit user registration data.
     */
    submitUser: function () {

      const username = document.getElementById("username-input").value;
      const password = document.getElementById("password-input").value;
      const firstName = document.getElementById("first-name-input").value;
      const lastName = document.getElementById("last-name-input").value;
      const email = document.getElementById("email-input").value;
      const phoneNumber = document.getElementById("phone-input").value;
      const role = document.getElementById("roles").value;

      //Checks if the username is valid.
      function isValidUsername(username) {
        return username.length >= 3;
      }

      //Checks if the password is valid.
      function isValidPassword(password) {
        return password.length >= 8 && /\d/.test(password);
      }

      if (!isValidUsername(username)) {
        alert("Username is invalid.");
        return;
      }

      if (!isValidPassword(password)) {
        alert("Password is invalid. It must be at least 8 characters long and contain at least one number");
        return;
      }

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
          const successMessage = document.getElementById('success-message');
          successMessage.style.display = 'block';
        }).catch(err => {
          //This is handled by the alert in rest.js
        });

      alert("TODO: add user " + JSON.stringify(userData)); // TODO: make call to the API
      let userCreated = true; // TODO: modify this variable based on the response from the API
      if (userCreated) {
        base.changeLocation("/login/login.html");
      }
    },
    initOnLoad: function () {
      document.addEventListener("DOMContentLoaded", base.registerController.load);
    },
  };
  return controller;
})();
