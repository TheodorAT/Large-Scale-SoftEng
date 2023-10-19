/*
 * Model/view/controller for the DriverTrip tab.
 * Author: Bianca Widstam, Amanda Nystedt
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.

base.driverTripController = function () {
  "use strict";

  let locations = [];

  const view = {
    // Opens the modal/dialog when trip is registered
    render: function () {
      const myModal = new bootstrap.Modal(document.getElementById("driverModal"));
      myModal.show();
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
      let time = date.toLocaleTimeString().split(":").slice(0, 2).join(":");
      document.getElementById("startTime").setAttribute("min", date.toLocaleDateString() + "T" + time);

      document.getElementById("mytrips").onclick = function (event) {
        event.preventDefault();
        base.changeLocation("#/my-trips");
      };
      // Loads all locations from the server through the REST API, see rest.js for definition.
      base.rest.getLocations().then(function (l) {
        locations = l;
        controller.setLocations("from", l);
        controller.setLocations("to", l);
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
    // Filters the locations in dropdown
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
      const status = 1;
      const form = {
        fromLocationId: fromId,
        toLocationId: toId,
        startTime: startTime,
        seatCapacity: seats,
        status_id: status,
      };
      //Call the REST API to register trip, see file rest.js for definitions.
      base.rest.createTrip(form).then(function (trip) {
        // Trip is the response from the server, it will have this form:
        // { driverId: "int", fromLocationId: "int", toLocationId: "int",startTime: "date", endTime: "date", seatCapacity :"int", };
        document.getElementById("registeredTripsModal").textContent =
          "From: " +
          from.value +
          " To: " +
          to.value +
          " Date: " +
          new Date(startTime).toLocaleDateString() +
          " Number of available seats: " +
          seats;
        document.getElementById("from").classList.remove("is-invalid");
        document.getElementById("to").classList.remove("is-invalid");
        document.getElementById("driver-form").reset();
        view.render();
      });
    },
  };

  return controller;
};
