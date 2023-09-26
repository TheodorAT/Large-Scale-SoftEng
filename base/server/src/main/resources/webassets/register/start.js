var base = base || {};


let loginButton = document.getElementById("login-button");
let createButton = document.getElementById("create-button");

    // Add a click event listener to the login button
    loginButton.addEventListener("click", function() {
        // Redirect to the second page
        base.changeLocation("/login/login.html")
    });

    // Add a click event listener to the login button
    createButton.addEventListener("click", function() {
        // Redirect to the second page
        base.changeLocation("/register/register.html")
    });

    base.changeLocation = function (url) {
        window.location.replace(url);
      };