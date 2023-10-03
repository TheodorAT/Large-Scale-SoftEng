/*
 * Model/view/controller for the My trips tab.
 * Author: Bianca Widstam
 */
var base = base || {};

base.myTripsController = function () {
  "use strict";

  // List of all trip data
  let model = [];
  let locations = [];
  let currentUser = {};

  const MyTripsViewModel = function (_trip) {
    this.trip = _trip;
    const viewModel = this;

    this.render = function (pastTemplate, updomingTemplate) {
      let template;
      let now = new Date().getTime();
      // Depending if the trip is old or new it should update the past or upcoming table
      viewModel.trip.startTime < now ? (template = pastTemplate) : (template = updomingTemplate);
      this.update(template.content.querySelector("tr"));
      const clone = document.importNode(template.content, true);
      template.parentElement.appendChild(clone);
    };
    // Update a single table row to display a trip
    this.update = function (trElement) {
      const td = trElement.children;
      let fromlocation = controller.getLocationFromId(viewModel.trip.fromLocationId);
      let tolocation = controller.getLocationFromId(viewModel.trip.toLocationId);
      td[0].textContent = fromlocation.name + ", " + fromlocation.municipality;
      td[1].textContent = tolocation.name + ", " + tolocation.municipality;
      const start = viewModel.trip.startTime;
      td[2].textContent = start.toLocaleDateString() + " " + start.toLocaleTimeString();
      const end = viewModel.trip.endTime;
      td[3].textContent = end.toLocaleDateString() + " " + end.toLocaleTimeString();
      const duration = new Date(end - start).toLocaleTimeString();
      td[4].textContent = duration;
      td[5].textContent = viewModel.trip.seatCapacity;
      td[6].textContent = viewModel.trip.driverId;
      let now = new Date().getTime();
      if (viewModel.trip.startTime > now) {
        let button = document.createElement("button");
        button.innerHTML = "Cancel";
        button.id = viewModel.trip.id;
        button.classList.add("btn", "btn-danger");
        //If button already has already been added, it needs to be replaced
        if (td[7].children[0]) {
          td[7].children[0].remove();
        }
        td[7].appendChild(button);
      }
    };
  };

  const view = {
    // Creates HTML for each trip in model
    render: function () {
      const pt = this.pastTemplate();
      const ut = this.upcomingTemplate();
      model.forEach((d) => d.render(pt, ut));
      controller.loadButtons();
    },
    pastTemplate: function () {
      return document.getElementById("past-trips-template");
    },
    upcomingTemplate: function () {
      return document.getElementById("upcoming-trips-template");
    },
    createCancelledButtons: function (id) {
      let badge = document.createElement("span");
      badge.innerHTML = "Cancelled";
      badge.id = id;
      badge.classList.add("badge", "bg-danger");
      document.getElementById(id).replaceWith(badge);
    },
  };

  const controller = {
    load: function () {
      // Loads all first destinations and then the drivertrips from the server through the REST API, see res.js for definition.
      // It will replace the model with the trips, and then render them through the view.

      // should return a trip with information {From,	To,	Time of Departure,Expected Time of Arrival, Seats, Driver, passangers}

      let userPromise = base.rest.getUser();
      let locationPromise = base.rest.getLocations();
      Promise.all([userPromise, locationPromise]).then(function (array) {
        currentUser = array[0];
        locations = array[1];
        console.log(currentUser);
        if (currentUser.role.name == "DRIVER") {
          base.rest.getDriverTrips().then(function (trips) {
            model = trips.map((f) => new MyTripsViewModel(f));
            view.render();
          });
        } else if (currentUser.role.name == "USER") {
          base.rest.getPassengerTrips().then(function (trips) {
            model = trips.map((f) => new MyTripsViewModel(f));
            view.render();
          });
        } // else if admin, get all trips?
      });
    },
    getLocationFromId: function (id) {
      return locations.find((location) => location.locationId == id);
    },
    loadButtons: function () {
      const cancelButtons = document.getElementById("mytrips").querySelectorAll("button");
      cancelButtons.forEach(
        (b) =>
          (b.onclick = function (event) {
            console.log("click", event.target.id);
            view.createCancelledButtons(event.target.id);
            //base.rest.cancelTrip(), update trip status?
          }),
      );
    },
  };

  return controller;
};
