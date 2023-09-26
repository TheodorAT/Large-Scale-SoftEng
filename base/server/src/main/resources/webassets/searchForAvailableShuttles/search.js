/*
 * Model/view/controller for the DriverTrip tab.
 * Author: Bianca Widstam, Amanda Nystedt
 */
var base = base || {};
// Defines the base namespace, if not already declared. Through this pattern it doesn't matter which order
// the scripts are loaded in.
base.searchTripController = function () {
  "use strict"; // add this to avoid some potential bugs

  let model = [];

  let locations = [];

  const TripViewModel = function (_trip) {
    this.trip = _trip;
    const viewModel = this;

    this.render = function (template) {
      this.update(template.content.querySelector("tr"));
      const clone = document.importNode(template.content, true);
      template.parentElement.appendChild(clone);
    };
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
      td[5].textContent = (end - start) / 3600000 + "hours";
      td[6].textContent = viewModel.trip.seatCapacity;
      td[7].textContent = viewModel.trip.driverId;
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
    selectLocation: function (location, id) {
      document.getElementById(id).value = location.innerHTML;
      document.getElementById(id).name = location.value;
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
    submitDriver: function () {
      const from = document.getElementById("from").name;
      const to = document.getElementById("to").name;
      const datetime = new Date(document.getElementById("datetime").value).getTime();
      const form = { fromLocationId: from, toLocationId: to, startTime: datetime };
      const fromCity = controller.getLocationFromId(from).name;
      const toCity = controller.getLocationFromId(to).name;

      base.rest.getShuttles(form).then(function (trips) {
        trips.forEach((trip) => {
          console.log(trip);
          const vm = new TripViewModel(trip);
          model.push(vm); // append the trip to the end of the model array
          vm.render(view.template()); // append the trip to the table
        });
        document.getElementById("from").value = "";
        document.getElementById("to").value = "";
        document.getElementById("datetime").value = "";
      });
    },
  };

  return controller;
};
