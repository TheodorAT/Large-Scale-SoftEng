/*
 * Author: emad Issawi, Osama Hajjouz
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.
base.searchTripController = function () {
  "use strict"; // add this to avoid some potential bugs

  let model = [];
  let currentUser = {};
  let locations = [];

  const TripViewModel = function (_trip) {
    this.trip = _trip;
    const viewModel = this;

    this.render = function (template) {
      this.update(template.content.querySelector("tr"));
      const clone = document.importNode(template.content, true);
      template.parentElement.appendChild(clone);
      controller.loadButtons();
    };
    this.update = function (trElement) {
      const td = trElement.children;
      td[0].textContent = viewModel.trip.id;
      let fromlocation = controller.getLocationFromId(viewModel.trip.fromLocationId);
      let tolocation = controller.getLocationFromId(viewModel.trip.toLocationId);
      td[1].textContent = fromlocation.name + ", " + fromlocation.municipality;
      td[2].textContent = tolocation.name + ", " + tolocation.municipality;
      const start = viewModel.trip.startTime;
      td[3].textContent = start.toLocaleDateString() + " " + start.toLocaleTimeString();
      const end = viewModel.trip.endTime;
      td[4].textContent = end.toLocaleDateString() + " " + end.toLocaleTimeString();
      td[5].textContent = new Date(end - start).toLocaleTimeString();
      td[6].textContent = viewModel.trip.seatCapacity;
      td[7].textContent = viewModel.trip.driverId;
      let now = new Date().getTime();
      // Book Button //
      //If button already has already been added, it needs to be replaced
      if (td[8].children[0]) {
        td[8].children[0].remove();
      }
      let button1 = document.createElement("button");
      button1.innerHTML = "Book";
      button1.id = viewModel.trip.id;
      button1.classList.add("btn", "btn-danger");
      td[8].appendChild(button1);
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

      base.rest.getUser().then(function (user) {
        currentUser = user;
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
    loadButtons: function () {
      let bookButtons = document.getElementById("search-trip").querySelectorAll("button");
      bookButtons.forEach(
        (b) =>
          (b.onclick = function (event) {
            //console.log("click", event.target.id);
            const tripId = event.target.id;
            base.rest
              .bookTrip(tripId)
              .then((bookedTrip) => {
                alert("Trip booked successfully!");
              })
              .catch((error) => {
                alert("Failed to book trip: " + error.message);
              });
          }),
      );
    },
    loadTrips: function () {
      const from = document.getElementById("from");
      const to = document.getElementById("to");
      const fromId = controller.getLocationId(from.value.trim());
      const toId = controller.getLocationId(to.value.trim());
      const startTime = new Date(document.getElementById("datetime").value).getTime();
      const form = { fromLocationId: fromId, toLocationId: toId, startTime: startTime };
      base.rest.getTrips(form).then(function (trips) {
        trips.forEach((trip) => {
          const vm = new TripViewModel(trip);
          model.push(vm); // append the trip to the end of the model array
          vm.render(view.template()); // append the trip to the table
        });
        if (trips.length == 0) {
          const myModal = new bootstrap.Modal(document.getElementById("searchModal"));
          myModal.show();
        }
      });
      document.getElementById("from").classList.remove("is-invalid");
      document.getElementById("to").classList.remove("is-invalid");
      document.getElementById("searchtrip-form").reset();
    },
  };
  return controller;
};
