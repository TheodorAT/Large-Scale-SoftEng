/*
 * Author: Emad Issawi, Osama Hajjouz, Bianca Widstam
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.
base.searchTripController = function () {
  "use strict"; // add this to avoid some potential bugs

  let model = [];
  let locations = [];

  const TripViewModel = function (_trip, _seats, _driverName) {
    this.trip = _trip;
    this.seats = _seats;
    this.driverName = _driverName;
    const viewModel = this;

    this.render = function (template) {
      viewModel.update(template);
      const clone = document.importNode(template.content, true);
      template.parentElement.appendChild(clone);
      controller.loadButtons();
    };
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
      td[6].textContent = this.driverName;
      td[6].id = viewModel.trip.driverId;
      // Book Button //
      //If button already has already been added, it needs to be replaced
      if (td[7].children[0]) {
        td[7].children[0].remove();
      }
      let button1 = document.createElement("button");
      button1.innerHTML = "Book";
      button1.id = viewModel.trip.id;
      button1.classList.add("btn", "btn-danger");
      td[7].appendChild(button1);
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
      return document.getElementById("searchtrip-template");
    },
  };

  const controller = {
    load: function () {
      document.getElementById("searchtrip-form").onsubmit = function (event) {
        event.preventDefault();
        const template = document.getElementById("searchtrip-template");
        const parentElement = template.parentElement;
        const rows = parentElement.querySelectorAll("tr");
        rows.forEach((row) => {
          parentElement.removeChild(row);
        });

        // Before submitting, needs to check if locations exists, otherwise mark the input invalid.
        const from = document.getElementById("from");
        const to = document.getElementById("to");
        let f = controller.getLocationId(from.value.trim());
        let t = controller.getLocationId(to.value.trim());
        f == undefined ? from.classList.add("is-invalid") : "";
        t == undefined ? to.classList.add("is-invalid") : "";
        if (f && t) {
          controller.loadTrips();
        }
        return false;
      };
      let date = new Date();
      let time = date.toLocaleTimeString().split(":").slice(0, 2).join(":");
      document.getElementById("datetime").setAttribute("min", date.toLocaleDateString() + "T" + time);

      document.getElementById("from").onkeyup = function (event) {
        controller.filterFunction("from");
      };
      document.getElementById("to").onkeyup = function (event) {
        controller.filterFunction("to");
      };
      base.rest.getLocations().then(function (l) {
        locations = l;
        controller.setLocations("from", l);
        controller.setLocations("to", l);
      });
      document.getElementById("mytrips").onclick = function (event) {
        event.preventDefault();
        base.changeLocation("#/my-trips");
      };
      //must manually hide, otherwise if using 'data-bs-dismiss' it will be removed completely from the DOM.
      document.getElementById("cancel-request").onclick = function (event) {
        document.getElementById("request-trip").hidden = true;
      };
      document.getElementById("requestbtn").onclick = function (event) {
        document.getElementById("request-trip").hidden = true;
        let from = document.getElementById("reqfrom");
        let to = document.getElementById("reqto");
        let startTime = document.getElementById("reqdate");
        base.rest
          .requestTrip({
            fromLocationId: from.getAttribute("from"),
            toLocationId: to.getAttribute("to"),
            startTime: startTime.getAttribute("time"),
          })
          .then((trip) => {
            controller.updateModal(
              "The trip was requested!",
              "From: " + from.innerText + " To: " + to.innerText + " Date: " + startTime.innerText,
              true,
            );
          });
      };
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
    updateModal: function (title, body, button) {
      document.getElementById("searchModalTitle").textContent = title;
      document.getElementById("searchModalBody").textContent = body;
      button ? (document.getElementById("mytrips").hidden = false) : (document.getElementById("mytrips").hidden = true);
      const searchModal = new bootstrap.Modal(document.getElementById("searchModal"));
      searchModal.show();
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
    loadButtons: function () {
      let bookButtons = document.getElementById("search-trip").querySelectorAll("button");
      bookButtons.forEach(
        (b) =>
          (b.onclick = function (event) {
            const tripId = event.target.id;
            let tr = event.target.parentElement.parentElement;
            base.rest
              .bookTrip(tripId)
              .then((bookedTrip) => {
                base.rest.getDriver(tr.children[6].id).then((driver) => {
                  document.getElementById("available-trips").hidden = true;
                  let from = tr.children[0].innerText;
                  let to = tr.children[1].innerText;
                  let start = tr.children[2].innerText;
                  document.getElementById("driverInfo").textContent =
                    "Name: " +
                    driver.first_name +
                    " " +
                    driver.last_name +
                    ". Tel: " +
                    driver.phone_number +
                    ". Email: " +
                    driver.email;
                  controller.updateModal(
                    "The trip was booked successfully!",
                    "Id: " + tripId + ". From: " + from + ". To: " + to + ". Date & time: " + start + ".",
                    true,
                  );
                });
              })
              .catch((error) => {
                document.getElementById("driverInfo").textContent = "";
                let msg = "Something went wrong, try again later.";
                if (error.message == "DUPLICATE") {
                  msg = "You have already booked this trip, try another trip";
                }
                if (error.message == "BAD_MAPPING") {
                  msg = "You cannot book a seat on your own trip.";
                }
                if (error.message == "UNKNOWN") {
                  msg = "You were unable to book this trip, since there are no available seats.";
                }
                controller.updateModal("Unfortunately, no trip was booked!", msg, false);
              });
          }),
      );
    },
    renderTrips: function (trips) {
      // Collect all trip IDs and driver IDS
      const tripIds = trips.map((trip) => trip.id);
      const driverIds = trips.map((trip) => trip.driverId);
      // Fetch available seats and driverNames for all trips
      const availableSeatsPromise = Promise.all(tripIds.map(base.rest.getAvailableSeats));
      const driverNamesPromise = Promise.all(driverIds.map(base.rest.getDriverName));

      Promise.all([availableSeatsPromise, driverNamesPromise]).then(([seats, driverNames]) => {
        model = trips.map((trip, index) => {
          const availableSeats = seats[index];
          const driverName = driverNames[index];
          return new TripViewModel(trip, availableSeats, driverName);
        });
        view.render();
      });
    },
    loadTrips: function () {
      const from = document.getElementById("from").value;
      const to = document.getElementById("to").value;
      const fromId = controller.getLocationId(from.trim());
      const toId = controller.getLocationId(to.trim());
      const startTime = new Date(document.getElementById("datetime").value).getTime();
      const form = { fromLocationId: fromId, toLocationId: toId, startTime: startTime };
      base.rest.getTrips(form).then(function (trips) {
        if (trips.length == 0) {
          document.getElementById("available-trips").hidden = true;
          document.getElementById("reqfrom").textContent = from;
          document.getElementById("reqto").textContent = to;
          document.getElementById("reqdate").textContent = new Date(startTime).toLocaleDateString();
          document.getElementById("reqdate").setAttribute("time", startTime);
          document.getElementById("reqfrom").setAttribute("from", fromId);
          document.getElementById("reqto").setAttribute("to", toId);
          document.getElementById("request-trip").hidden = false;
        } else {
          document.getElementById("request-trip").hidden = true;
          document.getElementById("available-trips").hidden = false;
          controller.renderTrips(trips);
        }
      });
      document.getElementById("from").classList.remove("is-invalid");
      document.getElementById("to").classList.remove("is-invalid");
      document.getElementById("searchtrip-form").reset();
    },
  };
  return controller;
};
