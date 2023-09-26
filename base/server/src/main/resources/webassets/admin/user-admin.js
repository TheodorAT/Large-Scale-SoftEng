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

    this.remove = function () {
      base.rest.deleteUser(model.selectedUser.user.id).then(function () {
        const le = model.selectedUser.listElement;
        le.parentElement.removeChild(le);
        const ix = model.users.indexOf(model.selectedUser);
        model.users.splice(ix, 1);
        model.users[0].select();
      });
    };

    this.select = function () {
      model.selectedUser = viewModel;

      // Set appropriate user-view class to either add or edit.
      const userView = document.getElementById("user-view");
      if (viewModel.user.username === "") {
        userView.classList.remove("edit");
        userView.classList.add("add");
        controller.editPassword(true);
      } else {
        userView.classList.add("edit");
        userView.classList.remove("add");
        controller.editPassword(false);
      }

      // Set active link on the left-hand side menu.
      document
        .getElementById("user-list")
        .querySelectorAll(".active")
        .forEach((activeEl) => activeEl.classList.remove("active"));
      viewModel.listElement.classList.add("active");

      document.getElementById("user-data").querySelector("a").href = "/rest/foo/user/" + viewModel.user.id;

      // Set defaults of form values. This will allow the HTML reset button to work by default HTML behaviour.
      document.getElementById("user-id").defaultValue = viewModel.user.id;
      document.getElementById("set-username").defaultValue = viewModel.user.username;
      document.getElementById("set-password").defaultValue = "";
      const roleIx = model.roleNames.indexOf(viewModel.user.role.name);
      const options = document.getElementById("set-role").querySelectorAll("option");
      options.forEach((o) => (o.defaultSelected = false));
      options[roleIx].defaultSelected = true;

      // Since we have specified reset values for all fields, we can use the reset function to populate the form
      document.getElementById("user-form").reset();
    };

    this.submit = function (submitEvent) {
      submitEvent.preventDefault;
      const password = document.getElementById("set-password").value;
      const username = document.getElementById("set-username").value;
      const role = document.getElementById("set-role").value;
      const id = document.getElementById("user-id").value;
      const credentials = { username, password, role };
      if (password === "") {
        // This makes it so we don't send an empty password, instead we send nothing on password field
        delete credentials.password;
      }
      if (id !== "") {
        // old user
        base.rest.putUser(id, credentials).then(function (user) {
          viewModel.user = user;
          viewModel.listElement.textContent = user.username;
          // This will fix the new reset state
          viewModel.select();
        });
      } else {
        base.rest.addUser(credentials).then(function (user) {
          const addedUserController = new UserViewModel(user);
          addedUserController.renderListElement();
          model.users.push(addedUserController);
          addedUserController.select();
        });
      }
      return false;
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
    },
  };
  return controller;
};
