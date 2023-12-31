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

  const MyTripsViewModel = function (_trip, _seats, _driver) {
    this.trip = _trip;
    this.seats = _seats;
    this.driver = _driver;
    const viewModel = this;

    this.render = function (pastTemplate, updomingTemplate) {
      let template;
      let now = new Date().getTime();
      // Depending if the trip is old or new it should update the past or upcoming table
      viewModel.trip.startTime < now ? (template = pastTemplate) : (template = updomingTemplate);
      viewModel.update(template);
      const clone = document.importNode(template.content, true);
      template.parentElement.appendChild(clone);
      controller.loadButtons();
    };
    // Update a single table row to display a trip, and
    this.update = function (template) {
      const trElement = template.content.querySelector("tr");
      const td = trElement.children;
      let fromlocation = controller.getLocationFromId(viewModel.trip.fromLocationId);
      let tolocation = controller.getLocationFromId(viewModel.trip.toLocationId);
      td[0].textContent = fromlocation.name + ", " + fromlocation.municipality;
      td[1].textContent = tolocation.name + ", " + tolocation.municipality;
      const start = viewModel.trip.startTime;
      td[2].textContent = start.toLocaleDateString() + " " + start.toLocaleTimeString();
      const end = viewModel.trip.endTime;
      td[3].textContent = end.toLocaleDateString() + " " + end.toLocaleTimeString();
      const totalMilliseconds = new Date(end).getTime() - new Date(start).getTime();
      const hours = Math.floor(totalMilliseconds / 3600000); // Extract hours
      const minutes = Math.floor((totalMilliseconds % 3600000) / 60000); // Extract minutes
      // Format the hours and minutes as "HH:mm"
      const formattedTime = `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}`;
      td[4].textContent = formattedTime;
      td[5].textContent = this.seats + " / " + viewModel.trip.seatCapacity;
      td[6].textContent =
        viewModel.trip.driverId == 0
          ? "Waiting for a driver"
          : this.driver.first_name + " " + this.driver.last_name + ", " + this.driver.phone_number;
      td[6].id = viewModel.trip.driverId;
      let button = view.createStatus(viewModel.trip);
      td[7].children[0] ? td[7].children[0].replaceWith(button) : td[7].appendChild(button);
    };
  };

  const view = {
    // Creates partial HTML-code for each trip in model
    render: function () {
      const pt = this.pastTemplate();
      const ut = this.upcomingTemplate();
      model.forEach((d) => d.render(pt, ut));
    },
    pastTemplate: function () {
      return document.getElementById("past-trips-template");
    },
    upcomingTemplate: function () {
      return document.getElementById("upcoming-trips-template");
    },

    createStatus: function (trip) {
      let button;
      let now = new Date().getTime();
      switch (trip.status_id) {
        case 1:
          //ACTIVE(1) if active it should display a cancel button
          button = view.createButton(trip.id, "Cancel", "btn-danger");
          break;
        case 2:
          //CANCELLED(2)  if cancelled, it should display cancelled
          button = view.createStatusButtons(trip.id, "Cancelled", "bg-danger");
          break;
        case 3:
          // REQUESTED(3) if requested, it should display requested
          button = view.createButton(trip.id, "Cancel", "btn-danger");
          break;
      }
      //if past trip it should display completed if it has not been cancelled
      if (trip.startTime < now) {
        button = view.createStatusButtons(trip.id, "Completed", "bg-success");
      }
      return button;
    },
    createButton: function (id, title, type) {
      let button = document.createElement("button");
      button.innerHTML = title;
      button.id = id;
      button.classList.add("btn", type);
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
        let driverPromise = base.rest.getDriverTrips();
        let passengerPromise = base.rest.getPassengerTrips();
        //Admin gets all trips
        switch (role) {
          case "ADMIN":
            Promise.all([driverPromise, passengerPromise]).then(function (array) {
              let driverTrips = array[0];
              let passengerTrips = array[1];
              let trips = driverTrips.concat(passengerTrips);
              controller.renderTrips(trips);
            });
            break;
          case "DRIVER":
            driverPromise.then(function (trips) {
              controller.renderTrips(trips);
            });
            break;
          case "USER":
            passengerPromise.then(function (trips) {
              controller.renderTrips(trips);
            });
        }
      });
    },
    renderTrips: function (trips) {
      // Collect all trip IDs
      const tripIds = trips.map((trip) => trip.id);
      // Collect all driver IDs
      const driverIds = trips.map((trip) => trip.driverId);
      // Filter out driverIDs that are not equal to 0
      const filteredDriverIds = driverIds.filter((driverId) => driverId !== 0);
      // Fetch available seats for all trips
      Promise.all(tripIds.map(base.rest.getAvailableSeats)).then((seats) => {
        // Fetch driver for all driver IDs
        Promise.all(filteredDriverIds.map(base.rest.getDriver)).then((users) => {
          model = trips.map((trip, index) => {
            const availableSeats = seats[index];
            // If driverId is 0, set driver to null
            const driver = driverIds[index] !== 0 ? users.shift() : null;
            return new MyTripsViewModel(trip, availableSeats, driver);
          });

          view.render();
        });
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
            //If the user is a passenger on the trip, delete passengerTrip and remove row, if requested? what happens then? should it be deleted
            //If the user is the driver on the trip, cancel driverTrip and remove row (should be in requested if passengers)
            // Cancels the trips from the server through the REST API, see rest.js for definition.
            let tripRow = event.target.parentNode.parentNode;
            let driverId = tripRow.children[6].id;
            if (driverId != currentUser.id) {
              base.rest.cancelPassengerTrip(event.target.id).then(function () {
                tripRow.remove();
              });
            } else {
              base.rest.cancelDriverTrip(event.target.id).then(function () {
                let button = view.createStatusButtons(event.target.id, "Cancelled", "bg-danger");
                let td = tripRow.children;
                td[7].children[0] ? td[7].children[0].replaceWith(button) : td[7].appendChild(button);
              });
            }
          }),
      );
    },
  };

  return controller;
};
