/**
 * JavaScript for creating an account in the Carpooling service.
 * Written by Max Emtefall and Justin Hellsten.
 */
let base = base || {};

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
const controller = {
  //Loads the controller, adds event listeners.
  load: function () {
    document.getElementById("button").addEventListener("click", function () {
      controller.submitUser();
    });
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
      if (username.length < 3) {
        return false;
      }

      let users = base.rest.getUsers();
      let usernameExists = users.some(function (user) {
        return user.username == username;
      });
      return !usernameExists;
    }

    //Checks if the password is valid.
    function isValidPassword(password) {
      return password.length >= 8 && /^(?=.*[^\w\s]).{8,}$/.test(password);
    }

    if (!isValidUsername(username)) {
      alert("Username is invalid.");
      return;
    }

    if (!isValidPassword(password)) {
      alert(
        "Password is invalid. It must be at least 8 characters long and contain at least one non-letter character.",
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

/**
 * Changes the current window location to URL.
 * @param The URL to navigate to.
 */
base.changeLocation = function (url) {
  window.location.replace(url);
};

// User registration controller
const userRegistrationController = base.registerUserController();

// Load the user registration controller
userRegistrationController.load();
