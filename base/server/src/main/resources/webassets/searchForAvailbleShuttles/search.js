var base = base || {};
base.searchShuttlesController = function() {
    "use strict";

    const controller = {};
    //TODO: the controller object needs a load function
    controller.load = function() {
        //TODO
    };
    return controller;
};

document.querySelector("#search-btn").addEventListener("click", handleSearch);

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
}