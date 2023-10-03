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
      //TODO: check status if cancelled add status button with "Cancelled".
      if (viewModel.trip.startTime > now) {
        let button = view.createCancelButton(viewModel.trip.id);
        //If button already has already been added, it needs to be replaced
        td[7].children[0].replaceWith(button);
      } else {
        let status = view.createStatusButtons(viewModel.trip.id, "Completed", "bg-success");
        td[7].replaceWith(status);
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
    createCancelButton: function (id) {
      let button = document.createElement("button");
      button.innerHTML = "Cancel";
      button.id = id;
      button.classList.add("btn", "btn-danger");
      return button;
    },
    createStatusButtons: function (id, title, type) {
      let badge = document.createElement("span");
      badge.innerHTML = title;
      badge.id = id;
      badge.classList.add("badge", type);
      return badge;
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
        let role = currentUser.role.name;
        //Admin gets all trips, should not be possible to book yourself as passenger if you are a driver, therefor no duplicates
        if (role == "DRIVER" || role == "ADMIN") {
          base.rest.getDriverTrips().then(function (trips) {
            model = trips.map((f) => new MyTripsViewModel(f));
            view.render();
          });
        }
        if (role == "USER" || role == "ADMIN") {
          base.rest.getPassengerTrips().then(function (trips) {
            model = trips.map((f) => new MyTripsViewModel(f));
            view.render();
          });
        }
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
            let badge = view.createStatusButtons(event.target.id, "Cancelled", "bg-danger");
            document.getElementById(event.target.id).replaceWith(badge);
            //If user, delete passengerTrip and remove row
            //If driver, update status to cancelled
            if (currentUser.role.name == "USER") {
              let tripRow = document.getElementById(event.target.id).parentNode.parentNode;
              //base.rest.deletePassengerTrip() TODO: waiting for back-end
              tripRow.remove();
            }
            //else: base.rest.cancelDriverTrip(), update trip status? TODO: waiting for back-end
          }),
      );
    },
  };

  return controller;
};
