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
    // Creates HTML for each user in model
    render: function () {
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
      let userRow;
      const deleteModal = new bootstrap.Modal(document.getElementById("deleteModal"));
      // Adds a click event listener to the table, delegating the event to the buttons
      document.querySelector("table").addEventListener("click", function (event) {
        if (event.target && event.target.classList.contains("delete-user")) {
          // This condition checks if the clicked element has the 'delete-user' class
          const button = event.target;
          // Displays a modal which lets the admin confirm deletion of account
          deleteModal.show();
          // finds the row in which the button is clicked
          userRow = button.closest("tr");
          if (userRow) {
            // Finds the specific column with user id
            const userIdDataCell = userRow.querySelector(".user-id-data");
            if (userIdDataCell) {
              userIdColumnValue = userIdDataCell.textContent;
            }
          }
        }
      });
      const addModal = new bootstrap.Modal(document.getElementById("modalAddUser"));
      // When delete button is pressed inside the modal, it calls the REST API to delete the user
      document.getElementById("modal-delete-user").onclick = () => {
        controller.adminDeleteUser(userIdColumnValue, userRow);
        deleteModal.hide();
      };
      //When add new admin button is pressed, a modal register form is displayed
      document.getElementById("new-admin").onclick = function (event) {
        addModal.show();
      };
      document.getElementById("addForm").onsubmit = function (event) {
        event.preventDefault();
        controller.addAdminUser();
        addModal.hide();
      };
    },
    //Calls REST API to delete user
    adminDeleteUser: function (id, userRow) {
      base.rest.deleteUser(id);
      //Removes the user row from the table
      userRow.remove();
    },
    addAdminUser: function () {
      const username = document.getElementById("input-username").value;
      const password = document.getElementById("input-password").value;
      const firstName = document.getElementById("input-firstname").value;
      const lastName = document.getElementById("input-lastname").value;
      const email = document.getElementById("input-email").value;

      const userData = {
        username: username,
        password: password,
        // Assigns the new user an admin role
        role: "ADMIN",
        first_name: firstName,
        last_name: lastName,
        email: email,
      };
      //Calls REST API to add user with the inputed values of the form
      base.rest.addUser(userData).then(function (user) {
        const vm = new UserViewModel(user);
        model.push(vm); // append the user to the end of the model array
        vm.render(view.template()); // append the user to the table
      });
    },
  };
  return controller;
};
