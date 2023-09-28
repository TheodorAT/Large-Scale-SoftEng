var base = base || {};
base.changeLocation = function (url) {
  window.location.replace(url);
};
base.loginController = (function () {
  "use strict";
  const controller = {
    load: function () {
      document.getElementById("login-form").onsubmit = function (event) {
        event.preventDefault();
        controller.loginUser();
        return false;
      };
      document.getElementById("register").onclick = function () {
        base.changeLocation("/register/register.html");
      };
      base.rest.getUser().then(function (user) {
        if (!user.isNone()) {
          base.changeLocation("/");
        }
      });

      // Show a notification if we just created a user from the registration page
      const userJustCreated = localStorage.getItem("userJustCreated") === "true";
      if (userJustCreated) {
        document.getElementById("success-message").classList.add("slide-down");
        localStorage.setItem("userJustCreated", "false");
        // Hide the alert after 3 seconds
        setTimeout(() => {
          document.getElementById("success-message").classList.remove("slide-down");
          document.getElementById("success-message").classList.add("fade-out");
        }, 3000);
      } 
    },
    loginUser: function () {
      const username = document.getElementById("username").value;
      const password = document.getElementById("password").value;
      const remember = document.getElementById("remember").checked;
      base.rest
        .login(username, password, remember)
        .then(() => base.changeLocation("/"))
        .catch(() => (document.getElementById("password").value = ""));
    },
    initOnLoad: function () {
      document.addEventListener("DOMContentLoaded", base.loginController.load);
    },
  };
  return controller;
})();
