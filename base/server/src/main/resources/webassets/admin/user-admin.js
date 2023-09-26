var base = base || {};
base.userAdminController = function () {
  "use strict";
  let model = [];

  // Every user has one of these
  const UserViewModel = function (_user) {
    this.user = _user;
    const viewModel = this;

    this.render = function (template) {
      this.update(template.content.querySelector("tr"));
      const clone = document.importNode(template.content, true);
      template.parentElement.appendChild(clone);
    };
    this.update = function (trElement) {
      const td = trElement.children;
      td[0].textContent = viewModel.user.first_name + " " + viewModel.user.last_name;
      td[1].textContent = viewModel.user.username;
      td[2].textContent = viewModel.user.id;
      td[3].textContent = viewModel.user.role.name;
    };
  };
  const view = {
    // Creates HTML for each trip in model
    render: function () {
      // A template element is a special element used only to add dynamic content multiple times.
      // See: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/template
      const t = this.template();
      model.forEach((d) => d.render(t));
    },
    template: function () {
      return document.getElementById("user-template");
    },
  };
  const controller = {
    load: function () {
      base.rest.getUsers().then(function (users) {
        model = users.map((f) => new UserViewModel(f));
        view.render();
      });
      let userIdColumnValue;
      // Add a click event listener to the table, delegating the event to the buttons
      document.querySelector("table").addEventListener("click", function (event) {
        if (event.target && event.target.classList.contains("delete-user")) {
          // This condition checks if the clicked element has the 'delete-user' class
          const button = event.target;
          const myModal = new bootstrap.Modal(document.getElementById("deleteModal"));
          myModal.show();
          // You can access the specific button or its parent row if needed:
          const parentRow = button.closest("tr");
          // console.log(parentRow);
          if (parentRow) {
            // Find the specific column (e.g., first column)
            const userIdDataCell = parentRow.querySelector(".user-id-data");

            if (userIdDataCell) {
              // Access the text content of the column
              userIdColumnValue = userIdDataCell.textContent;

              // Do something with the column value
              console.log("UserID of the selected row: ", userIdColumnValue);
            }
          }
        }
      });

      document.getElementById("modal-delete-user").onclick = function (event) {
        controller.adminDeleteUser(userIdColumnValue);
      };

      document.getElementById("new-admin").onclick = function (event) {
        const myModal = new bootstrap.Modal(document.getElementById("modalAddUser"));
        myModal.show();
      };
      document.getElementById("modal-add-admin").onclick = function (event) {
        controller.addAdminUser();
      };
    },
    //TODO: update view after deleting user without having to refresh the page
    adminDeleteUser: function (id) {
      base.rest.deleteUser(id);
    },

    //TODO: Implement addAdminUsers
    addAdminUser: function () {
      const name = document.getElementById("new-name");
      const username = document.getElementById("new-username");
      const password = document.getElementById("new-password");
      //TODO: assign the user an admin role
      //const credentials = {username, password, role};
      // TODO: add admin, Call the REST API to add admin user
      //base.rest.addUser(credentials);
      console.log(
        "Added user with name, username and password:" + " " + name.value + " " + username.value + " " + password.value,
      );
    },
  };
  return controller;
};
