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
  }
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
    }
};
  return controller;
};
