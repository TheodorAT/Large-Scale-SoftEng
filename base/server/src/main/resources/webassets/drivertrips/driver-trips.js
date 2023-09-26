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

  let locations = [];

  const DriverTripViewModel = function (_trip) {
    this.trip = _trip;
    const viewModel = this;

    this.render = function (template) {
      let now = new Date().getTime();
      if (viewModel.trip.startTime > now) {
        this.update(template.content.querySelector("tr"));
        const clone = document.importNode(template.content, true);
        template.parentElement.appendChild(clone);
      }
    };
    this.update = function (trElement) {
      const td = trElement.children;
      td[0].textContent = viewModel.trip.id;
      let fromlocation = controller.getLocationFromId(viewModel.trip.fromLocationId);
      let tolocation = controller.getLocationFromId(viewModel.trip.toLocationId);
      td[1].textContent = fromlocation.name + ", " + fromlocation.municipality;
      td[2].textContent = tolocation.name + ", " + tolocation.municipality;
      td[3].textContent = viewModel.trip.seatCapacity;
      const start = viewModel.trip.startTime;
      td[4].textContent = start.toLocaleDateString() + " " + start.toLocaleTimeString();
      const end = viewModel.trip.endTime;
      td[5].textContent = end.toLocaleDateString() + " " + end.toLocaleTimeString();
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
        // Before submitting, needs to check if locations exists, otherwise mark the input invalid.
        const from = document.getElementById("from");
        const to = document.getElementById("to");
        let f = controller.getLocationId(from.value.trim());
        let t = controller.getLocationId(to.value.trim());
        f == undefined ? from.classList.add("is-invalid") : "";
        t == undefined ? to.classList.add("is-invalid") : "";
        if (f && t) {
          controller.submitDriverTrip();
        }
        return false;
      };
      document.getElementById("from").onkeyup = function (event) {
        controller.filterFunction("from");
      };
      document.getElementById("to").onkeyup = function (event) {
        controller.filterFunction("to");
      };
      let date = new Date();
      document
        .getElementById("startTime")
        .setAttribute("min", date.toLocaleDateString() + "T" + date.getHours() + ":" + date.getMinutes());

      document.getElementById("mytrips").onclick = function (event) {
        event.preventDefault();
        base.changeLocation("#/my-trips");
      };
      // Loads all registered trips and destinations from the server through the REST API, see res.js for definition.
      // It will replace the model with the trips, and then render them through the view.
      base.rest.getLocations().then(function (l) {
        locations = l;
        controller.setLocations("from", l);
        controller.setLocations("to", l);
        base.rest.getDriverTrips().then(function (trips) {
          model = trips.map((t) => new DriverTripViewModel(t));
          view.render();
        });
      });
    },
    getLocationId: function (value) {
      return locations.find((location) => location.name + ", " + location.municipality == value)?.locationId;
    },
    getLocationFromId: function (id) {
      return locations.find((location) => location.locationId == id);
    },
    setLocations: function (id, destinations) {
      for (let i = 0; i < destinations.length; i++) {
        let ul = document.getElementById("dropdown-" + id);
        let li = document.createElement("li");
        ul.appendChild(li);
        let button = document.createElement("button");
        button.innerHTML = destinations[i].name + ", " + destinations[i].municipality;
        button.classList.add("dropdown-item");
        li.appendChild(button);
        button.onclick = function (event) {
          event.preventDefault();
          controller.selectLocation(event.target, id);
        };
      }
    },
    selectLocation: function (location, id) {
      document.getElementById(id).value = location.innerHTML.trim();
      document.getElementById("dropdown-" + id).classList.toggle("show");
    },
    filterFunction: function (id) {
      var input, filter, citys, i;
      input = document.getElementById(id);
      filter = input.value.toUpperCase();
      citys = document.querySelectorAll("#dropdown-" + id + " li button");
      for (i = 0; i < citys.length; i++) {
        let txtValue = citys[i].textContent || citys[i].innerText;
        if (txtValue.toUpperCase().indexOf(filter) > -1) {
          citys[i].style.display = "";
        } else {
          citys[i].style.display = "none";
        }
      }
    },
    submitDriverTrip: function () {
      const from = document.getElementById("from");
      const to = document.getElementById("to");
      const fromId = controller.getLocationId(from.value.trim());
      const toId = controller.getLocationId(to.value.trim());
      const seats = document.getElementById("seats").value;
      const startTime = new Date(document.getElementById("startTime").value).getTime();
      const form = { fromLocationId: fromId, toLocationId: toId, startTime: startTime, seatCapacity: seats };
      document.getElementById("registeredTripsModal").textContent =
        "From: " +
        from.value +
        " To: " +
        to.value +
        " Date: " +
        new Date(startTime).toLocaleDateString() +
        " Number of available seats: " +
        seats;

      //Call the REST API to register trip, see file rest.js for definitions.
      base.rest.createTrip(form).then(function (trip) {
        // Trip is the response from the server, it will have this form:
        // { driverId: "int", fromLocationId: "int", toLocationId: "int",startTime: "date", endTime: "date", seatCapacity :"int", };
        const vm = new DriverTripViewModel(trip);
        model.push(vm); // append the trip to the end of the model array
        vm.render(view.template()); // append the trip to the table
        document.getElementById("from").classList.remove("is-invalid");
        document.getElementById("to").classList.remove("is-invalid");
        document.getElementById("driver-form").reset();
        const myModal = new bootstrap.Modal(document.getElementById("driverModal"));
        myModal.show();
      });
    },
  };

  return controller;
};