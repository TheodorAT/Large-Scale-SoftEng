/*
 * Model/view/controller for the DriverTrip tab.
 * Author: Bianca Widstam, Amanda Nystedt
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.
base.driverTripController = function () {
  "use strict"; // add this to avoid some potential bugs

  let model = [];

  const DriverTripViewModel = function (_trip) {
    this.trip = _trip;
    const viewModel = this;

    this.render = function (template) {
      this.update(template.content.querySelector("tr"));
      const clone = document.importNode(template.content, true);
      template.parentElement.appendChild(clone);
    };
    this.update = function (trElement) {
      const td = trElement.children;
      td[0].textContent = viewModel.trip.number;
      td[1].textContent = viewModel.trip.from;
      td[2].textContent = viewModel.trip.to;
      td[3].textContent = viewModel.trip.seats;
      td[4].textContent = viewModel.trip.datetime;
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
      return document.getElementById("drivertrip-template");
    },
  };

  const controller = {
    load: function () {
      document.getElementById("driver-form").onsubmit = function (event) {
        event.preventDefault();
        controller.submitDriver();
        return false;
      };
      /*  
      TODO: getDestinations to/from from rest-call instead of a temporary list of destinations
      base.rest.getDestinations().then(function (destinations) {
        controller.setCitys("from", destinations);
        controller.setCitys("to", destinations);
      }); */
      controller.setCitys("from");
      controller.setCitys("to");
      document.getElementById("from").onkeyup = function (event) {
        controller.filterFunction("from");
      };
      document.getElementById("to").onkeyup = function (event) {
        controller.filterFunction("to");
      };

      let from = document.querySelectorAll("#dropdown-from li option");
      for (var i = 0, il = from.length; i < il; i++) {
        from[i].onclick = function (event) {
          controller.selectCity(event.target.label, "from");
        };
      }
      let to = document.querySelectorAll("#dropdown-to li option");
      for (var i = 0, il = to.length; i < il; i++) {
        to[i].onclick = function (event) {
          controller.selectCity(event.target.label, "to");
        };
      }

      // TODO: Loads all registered trips from the server through the REST API, see res.js for definition.
      // It will replace the model with the trips, and then render them through the view.
      /* base.rest.getDriverTrips().then(function (trips) {
        model = trips.map((t) => new DriverTripViewModel(t));
        view.render();
      });
      */
    },
    setCitys: function (id) {
      let destinations = ["Malmö", "Lund", "Helsingborg"];
      for (let i = 0; i < destinations.length; i++) {
        let ul = document.getElementById("dropdown-" + id);
        let li = document.createElement("li");
        ul.appendChild(li);
        let option = document.createElement("option");
        option.text = destinations[i];
        option.classList.add("dropdown-item");
        li.appendChild(option);
      }
    },
    selectCity: function (option, id) {
      document.getElementById(id).value = option;
      document.getElementById("dropdown-" + id).classList.toggle("show");
    },
    filterFunction: function (id) {
      var input, filter, options, i;
      input = document.getElementById(id);
      filter = input.value.toUpperCase();
      options = document.querySelectorAll("#dropdown-" + id + " li option");
      for (i = 0; i < options.length; i++) {
        let txtValue = options[i].textContent || options[i].innerText;
        if (txtValue.toUpperCase().indexOf(filter) > -1) {
          options[i].style.display = "";
        } else {
          options[i].style.display = "none";
        }
      }
    },
    submitDriver: function () {
      const from = document.getElementById("from").value;
      const to = document.getElementById("to").value;
      const seats = document.getElementById("seats").value;
      const datetime = document.getElementById("datetime").value;
      const form = { from: from, to: to, seats: seats, datetime: datetime, number: 1 };
      document.getElementById("registeredTripsModal").textContent =
        "From: " + from + " To: " + to + " Date: " + datetime + " Number of available seats: " + seats;
      const vm = new DriverTripViewModel(form);
      model.push(vm);
      vm.render(view.template());
      document.getElementById("from").value = "";
      document.getElementById("to").value = "";
      document.getElementById("seats").value = "";
      document.getElementById("datetime").value = "";
      const myModal = new bootstrap.Modal(document.getElementById("driverModal"));
      myModal.show();

      // TODO: Call the REST API to register trip, see file rest.js for definitions.
      /* base.rest.registerDriverTrip({ payload: form }).then(function (trip) {
          // Trip is the response from the server, it will have this form:
          // { from: from, to: to, seats: seats, datetime: datetime, number: number};
          const vm = new DriverViewModel(trip);
          model.push(vm); // append the foo to the end of the model array
          vm.render(view.template()); // append the foo to the table
        }); */
    },
  };

  return controller;
};
