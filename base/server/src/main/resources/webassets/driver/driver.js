/*
 * Model/view/controller for the Driver tab.
 * Author: Bianca Widstam, Amanda Nystedt
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.
base.driverController = function () {
  "use strict"; // add this to avoid some potential bugs

  // List of all foo data, will be useful to have when update functionality is added in lab 2.
  let model = [];

  const DriverViewModel = function (_trip) {
    // We call the parameter _foo to avoid accidentally using the old version, we might otherwise end up in a
    // scenario where foo and this.foo are different things.
    this.trip = _trip;
    // This assignment is used below where 'this' is not available
    const viewModel = this;

    this.render = function (template) {
        this.update(template.content.querySelector("ul"));
        const clone = document.importNode(template.content, true);
        // TODO: Add stuff from lab 2 end-2-end task here
        template.parentElement.appendChild(clone);
      };
      // Update a single table row to display a foo
      this.update = function (ulElement) {
        const lis = ulElement.children;
        lis[0].textContent = "From: " + viewModel.trip.from + " To: " + viewModel.trip.to + " Date: " + viewModel.trip.datetime + " Number of available seats: " + viewModel.trip.seats;
      };

      
  };

  const view = {
    // Creates HTML for each foo in model
    render: function () {
      // A template element is a special element used only to add dynamic content multiple times.
      // See: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/template
      const t = this.template();
      model.forEach((d) => d.render(t));
    },
    template: function () {
        return document.getElementById("driver-template");
      },
  };

  const controller = {
    load: function () {
        document.getElementById("driver-form").onsubmit = function (event) {
            event.preventDefault();
            controller.submitDriver();
            return false;
          };
    },
    submitDriver: function () {
        const from = document.getElementById("from").value;
        const to = document.getElementById("to").value;
        const seats = document.getElementById("seats").value;
        const datetime = document.getElementById("datetime").value;
        const form = {'from': from, 'to': to, 'seats':seats, 'datetime':datetime};
        const vm = new DriverViewModel(form);
        model.push(vm);
        vm.render(view.template())
        // Call the REST API, see file rest.js for definitions.
        /* base.rest.registerTrip({ payload: form }).then(function (foo) {
          // Foo is the response from the server, it will have this form:
          // {id: 123, userId: 1, payload: 'data', created: 1525343407}
          const vm = new FooViewModel(foo);
          model.push(vm); // append the foo to the end of the model array
          vm.render(view.template()); // append the foo to the table
          input.value = ""; // clear the input HTML element
        }); */
      },


  };

  return controller;
};
