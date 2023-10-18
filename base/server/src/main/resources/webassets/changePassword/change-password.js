var base = base || {};

base.changePasswordController = function () {
  "use strict";

  const controller = {
    load: function () {
      document.getElementById("change-password-form").onsubmit = function (event) {
        event.preventDefault();
        const oldPassword = document.getElementById("old-password").value;
        const newPassword = document.getElementById("new-password").value;
        const confirmNewPassword = document.getElementById("confirm-new-password").value;

        if (newPassword !== confirmNewPassword) {
          showMessage("New passwords do not match!", "text-danger");
          return;
        }
        //Checks if the password is valid.
        if (newPassword.length >= 8 &&  /\d/.test(newPassword) && /[a-zA-Z]/.test(newPassword)){
        }
        else{
          showMessage("Password must be at least 8 characters long and contain at least one letter and one number!", "text-danger");
          return;
        }
        base.rest
          .changePassword(oldPassword, newPassword)
          .then(function (response) {
            showMessage("Password changed successfully!", "text-success");
          })
          .catch(function (error) {
            showMessage("Failed to change password. Please try again later.", "text-danger");
          });

        document.getElementById("change-password-form").reset();
      };
    },
  };

  function showMessage(message, className) {
    const messageDiv = document.getElementById("password-change-message");
    messageDiv.textContent = message;
    messageDiv.className = className;
  }

  return controller;
};

// Usage
const changePasswordController = base.changePasswordController();
changePasswordController.load();
