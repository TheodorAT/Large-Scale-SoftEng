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
      td[6].id = viewModel.trip.driverId;
      let now = new Date().getTime();
      //Status: ACTIVE(1), CANCELLED(2), REQUESTED(3);
      let button;
      switch (viewModel.trip.status_id) {
        case 1:
          //ACTIVE(1) if active it should display a cancel button
          button = view.createButton(viewModel.trip.id, "Cancel",  "btn-danger");
          break;
        case 2:
          //CANCELLED(2)  if cancelled, should be able to book?
          button = view.createStatusButtons(viewModel.trip.id, "Cancelled", "bg-danger");
          break;
        case 3:
            //REQUESTED(3) if requested, should be able to book
            button = view.createButton(viewModel.trip.id, "Book", "btn-primary");
          break;
        default:
          button = view.createStatusButtons(viewModel.trip.id, "Completed", "bg-success");
      }
      if (viewModel.trip.startTime < now) {
        button = view.createStatusButtons(viewModel.trip.id, "Completed", "bg-success");
      }
      td[7].children[0] ? td[7].children[0].replaceWith(button) : td[7].appendChild(button);
    };
  };

  const view = {
    // Creates partial HTML-code for each trip in model
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
    createButton: function (id, title, type) {
      let button = document.createElement("button");
      button.innerHTML = title;
      button.id = id;
      button.classList.add("btn",type);
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
      // Loads first destinations and the user and then the trips from the server through the REST API, see rest.js for definition.
      // It will replace the model with the trips, and then render them through the view.
      let userPromise = base.rest.getUser();
      let locationPromise = base.rest.getLocations();
      Promise.all([userPromise, locationPromise]).then(function (array) {
        currentUser = array[0];
        locations = array[1];
        let role = currentUser.role.name;
        //Admin gets all trips, should not be possible to book yourself as passenger if you are a driver, therefore no duplicates
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
            //If the user is a passenger on the trip, delete passengerTrip and remove row
            //If the user is the driver on the trip, update status to cancelled
            // Cancels the trips from the server through the REST API, see rest.js for definition.
            let tripRow = document.getElementById(event.target.id).parentNode.parentNode;
            let driverId = tripRow.children[6].id;
            if (driverId != currentUser.id) {
              base.rest.cancelPassengerTrip(event.target.id).then(function () {
                tripRow.remove();
              });
            } else {
              base.rest.cancelDriverTrip(event.target.id).then(function () {
                controller.load();
              });
            }
          }),
      );
    },
  };

  return controller;
};
