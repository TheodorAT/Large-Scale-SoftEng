/*
 * Model/view/controller for the foo tab.
 * Author: Rasmus Ros, rasmus.ros@cs.lth.se
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.
base.searchShuttlesController = function () {
  "use strict"; // add this to avoid some potential bugs

  // List of all foo data, will be useful to have when update functionality is added in lab 2.
  let model = [];
  let locations = [];
  const TripsViewModel = function (_trip) {
    this.trip = _trip;

    const viewModel = this;

    this.render = function (template) {
      this.update(template.content.querySelector("tr"));
      const clone = document.importNode(template.content, true);
      template.parentElement.appendChild(clone);
    };
    // Update a single table row to display a trip
    this.update = function (trElement) {
      const td = trElement.children;
      td[0].textContent = viewModel.trip.id;
      let fromlocation = controller.getLocationFromId(viewModel.trip.fromLocationId);
      let tolocation = controller.getLocationFromId(viewModel.trip.toLocationId);
      td[1].textContent = fromlocation.name;
      td[2].textContent = tolocation.name;
      const start = viewModel.trip.startTime;
      td[3].textContent = start.toLocaleDateString() + " " + start.toLocaleTimeString();
      const end = viewModel.trip.endTime;
      td[4].textContent = end.toLocaleDateString() + " " + end.toLocaleTimeString();

      td[6].textContent = viewModel.trip.seatCapacity;
    };
  };

  const view = {
    render: function () {
      const t = this.template();
      model.forEach((d) => d.render(t));
    },

    template: function () {
      return document.getElementById("trips-template");
    },
  };

  const controller = {
    load: function () {
      document.querySelector("#search-btn").addEventListener("click", handleSearch);
      document.getElementById("search-form").onsubmit = function (event) {
        event.preventDefault();
        controller.submitTrip();
        return false;
      };
      document.getElementById("from-input").onkeyup = function (event) {
        controller.filterFunction("from-input");
      };
      document.getElementById("destination").onkeyup = function (event) {
        controller.filterFunction("destination");
      };
      // Loads all foos from the server through the REST API, see res.js for definition.
      // It will replace the model with the foos, and then render them through the view.
      base.rest.getLocations().then(function (l) {
        locations = l;
        controller.setLocations("from-input", l);
        controller.setLocations("destination", l);
        base.rest.getDriverTrips().then(function (trips) {
          model = trips.map((f) => new TripsViewModel(f));
          view.render();
        });
      });
    },
    setLocations: function (id, destinations) {
      for (let i = 0; i < destinations.length; i++) {
        let ul = document.getElementById("dropdown-" + id);
        let li = document.createElement("li");
        ul.appendChild(li);
        let button = document.createElement("button");
        button.innerHTML = destinations[i].name;
        button.value = destinations[i].locationId;
        button.classList.add("dropdown-item");
        li.appendChild(button);
        button.onclick = function (event) {
          event.preventDefault();
          controller.selectLocation(event.target, id);
        };
      }
    },
    getLocationFromId: function (id) {
      return locations.find((location) => location.locationId == id);
    },
    selectLocation: function (location, id) {
      document.getElementById(id).value = location.innerHTML;
      document.getElementById(id).name = location.value;
      document.getElementById("dropdown-" + id).classList.toggle("show");
    },
    submitTrip: function () {
      const fromInput = document.querySelector("#from-input").value;
      const destinationInput = document.querySelector("#destination").value;
      const datetimeInput = document.querySelector("#datetime-input").value;
      const form = { fromLocationId: fromInput, toLocationId: destinationInput, startTime: datetimeInput };
      const fromCity = controller.getLocationFromId(fromInput).name;
      const toCity = controller.getLocationFromId(destinationInput).name;

      // Make an API call to fetch available shuttles
      base.rest.getTrips(fromInput, destinationInput, datetimeInput).then((trips) => {
        const vm = new TripsViewModel(trip);
        model.push(vm);
        vm.render(view.template());
        document.getElementById("from-input").value = "";
        document.getElementById("destination-input").value = "";
        document.getElementById("datetime-input").value = "";
      });
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
  };

  return controller;
};

/*

function handleSearch() {
    const fromInput = document.querySelector("#from-input").value;
    const destinationInput = document.querySelector("#destination-input").value;
    const datetimeInput = document.querySelector("#datetime-input").value;

    // Make an API call to fetch available shuttles
    base.rest.getShuttles(fromInput, destinationInput, datetimeInput)
        .then((shuttles) => {
            viewShuttleInTable(shuttles);
        })
        .catch((error) => {
            console.error("Error fetching shuttles:", error);
        });
}


// Function to view shuttle data in the table
function viewShuttleInTable(shuttles) {
    const tableBody = document.querySelector("#shuttle-list tbody");
    tableBody.innerHTML = "";

    shuttles.forEach((shuttle) => {
        const row = tableBody.insertRow();
        row.innerHTML = `
      <td>${shuttle.from}</td>
      <td>${shuttle.to}</td>
      <td>${shuttle.startTime}</td>
      <td>${shuttle.arrivalTime}</td>
      <td>${shuttle.duration}</td>
      <td>${shuttle.seatsLeft}</td>
      <td>${shuttle.driver}</td>
      <td>${shuttle.rating}</td>
      <td>${shuttle.car}</td>
      <td>${shuttle.baggage}</td>
    `;
    });
}*/
