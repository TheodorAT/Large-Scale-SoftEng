/*
 * Unit tests for the mytrip controller
 * Author: Bianca Widstam
 */
describe("myTripsController", function () {
  const locations = [
    new base.Location({ locationId: 1, municipality: "Skåne", name: "Lund", latitude: 10000, longitude: 23213 }),
    new base.Location({ locationId: 2, municipality: "Halland", name: "Halmstad", latitude: 10030, longitude: 23313 }),
    new base.Location({ locationId: 3, municipality: "Skåne", name: "Malmö", latitude: 10000, longitude: 23213 }),
    new base.Location({ locationId: 4, municipality: "Halland", name: "Laholm", latitude: 10030, longitude: 23313 }),
  ];

  const test = new base.User({ id: 1, username: "Test", role: "USER" });
  const admin = new base.User({ id: 2, username: "Admin", role: "ADMIN" });

  const trips = [
    new base.Trip({
      id: 1,
      driverId: 3,
      fromLocationId: 1,
      toLocationId: 2,
      startTime: new Date().getTime() + 1000000,
      endTime: new Date().getTime() + 3000000,
      seatCapacity: 2,
      status_id: 2,
    }),
    new base.Trip({
      id: 2,
      driverId: 3,
      fromLocationId: 2,
      toLocationId: 3,
      startTime: new Date().getTime() + 1000000,
      endTime: new Date().getTime() + 3000000,
      seatCapacity: 3,
      status_id: 1,
    }),
    new base.Trip({
      id: 3,
      driverId: 3,
      fromLocationId: 3,
      toLocationId: 4,
      startTime: new Date().getTime() + 1000000,
      endTime: new Date().getTime() + 3000000,
      seatCapacity: 4,
      status_id: 3,
    }),
    new base.Trip({
      id: 4,
      driverId: 3,
      fromLocationId: 3,
      toLocationId: 4,
      startTime: new Date().getTime() + 12000000,
      endTime: new Date().getTime() + 30200000,
      seatCapacity: 4,
      status_id: 1,
    }),
  ];

  let node;
  let tripDriverPromise;
  let tripPassengerPromise;
  let availableSeatsPromise;
  let driverInfoPromise;

  // Creates the controller by loading the my-trip.html and put it in the node variable
  // Loads all the trips for admin
  beforeEach(function (done) {
    controller = base.myTripsController();
    //specHelper.spyOnRest();
    const nodePromise = specHelper.fetchHtml("mytrips/my-trips.html", document.body);
    nodePromise
      .then(function (n) {
        node = n;
        return node;
      })
      .then(function () {
        const locationPromise = Promise.resolve(locations.slice(0));
        spyOn(base.rest, "getLocations").and.returnValue(locationPromise);
        const adminPromise = Promise.resolve(admin);
        spyOn(base.rest, "getUser").and.returnValues(adminPromise);
        tripDriverPromise = Promise.resolve(trips.slice(0, 2));
        spyOn(base.rest, "getDriverTrips").and.returnValue(tripDriverPromise);
        tripPassengerPromise = Promise.resolve(trips.slice(2, 4));
        spyOn(base.rest, "getPassengerTrips").and.returnValue(tripPassengerPromise);
        availableSeatsPromise = Promise.resolve(1);
        spyOn(base.rest, "getAvailableSeats").and.returnValue(availableSeatsPromise);
        driverInfoPromise = Promise.resolve(1);
        spyOn(base.rest, "getDriver").and.returnValue(driverInfoPromise);
        controller.load();
        return Promise.all([adminPromise, locationPromise]).then(function () {
          return Promise.all([tripDriverPromise, tripPassengerPromise]).then(function () {
            return Promise.all([availableSeatsPromise,driverInfoPromise]);
          });
        });
      })
      .finally(done);
  });
  // Remove the node from the DOM
  afterEach(function () {
    document.body.removeChild(node);
  });
  /**
   * @desc test that both passenger and driver trips are fetched and displayed in table, on load when user is admin
   * @task ETS-1267
   * @story ETS-723
   */
  it("should fetch passenger and driver trips on load when admin", function () {
    const rows = node.querySelectorAll("tbody tr");
    expect(rows.length).toBe(trips.length);
  });
  /**
   * @desc Test that the 'From' location is displayed correctly for each trip in the table.
   * @task ETS-1267
   * @story ETS-723
   */
  it("should display the correct 'From' location", function () {
    const rows = node.querySelectorAll("tbody tr");
    for (let i = 0; i < trips.length; i++) {
      const row = rows[i];
      const trip = trips[i];
      const fromLocationCell = row.querySelector("td:nth-child(1)");
      expect(fromLocationCell.textContent).toContain(locations[trip.fromLocationId - 1].name);
    }
  });
  /**
   * @desc Test that the 'To' location is displayed correctly for each trip in the table.
   * @task ETS-1267
   * @story ETS-723
   */
  it("should display the correct 'To' location", function () {
    const rows = node.querySelectorAll("tbody tr");
    for (let i = 0; i < trips.length; i++) {
      const row = rows[i];
      const trip = trips[i];
      const toLocationCell = row.querySelector("td:nth-child(2)");
      expect(toLocationCell.textContent).toContain(locations[trip.toLocationId - 1].name);
    }
  });
  /**
   * @desc Test that the start time is displayed correctly for each trip in the table.
   * @task ETS-1267
   * @story ETS-723
   */
  it("should display the correct start time", function () {
    const rows = node.querySelectorAll("tbody tr");
    for (let i = 0; i < trips.length; i++) {
      const row = rows[i];
      const trip = trips[i];
      const startTimeCell = row.querySelector("td:nth-child(3)");
      expect(startTimeCell.textContent).toContain(new Date(trip.startTime).toLocaleString());
    }
  });

  /**
   * @desc Test that the end time is displayed correctly for each trip in the table.
   * @task ETS-1267
   * @story ETS-723
   */
  it("should display the correct end time", function () {
    const rows = node.querySelectorAll("tbody tr");
    for (let i = 0; i < trips.length; i++) {
      const row = rows[i];
      const trip = trips[i];
      const endTimeCell = row.querySelector("td:nth-child(4)");
      expect(endTimeCell.textContent).toContain(new Date(trip.endTime).toLocaleString());
    }
  });

  /**
   * @desc Test that the trip duration is displayed correctly for each trip in the table.
   * @task ETS-1267
   * @story ETS-723
   */
  it("should display the correct duration", function () {
    const rows = node.querySelectorAll("tbody tr");
    for (let i = 0; i < trips.length; i++) {
      const row = rows[i];
      const trip = trips[i];
      const durationCell = row.querySelector("td:nth-child(5)");

      // Calculate the expected duration in HH:mm format
      const totalMilliseconds = new Date(trip.endTime) - new Date(trip.startTime);
      const hours = Math.floor(totalMilliseconds / 3600000); // Extract hours
      const minutes = Math.floor((totalMilliseconds % 3600000) / 60000); // Extract minutes
      const expectedDuration = `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}`;

      expect(durationCell.textContent).toBe(expectedDuration);
    }
  });

  /**
   * @desc Test that the number of available seats is displayed correctly for each trip in the table.
   * @task ETS-1337
   * @story ETS-1330
   */
  it("should display the correct number of seats", function () {
    const rows = node.querySelectorAll("tbody tr");
    for (let i = 0; i < trips.length; i++) {
      const row = rows[i];
      const trip = trips[i];
      const seatsCell = row.querySelector("td:nth-child(6)");
      expect(seatsCell.textContent).toContain("1 / " + trip.seatCapacity);
    }
  });
  /**
   * @desc Test that the trip status is displayed correctly for trips with different statuses.
   * @task ETS-1340
   * @story ETS-723
   */
  it("should display the correct status for different trip statuses", function () {
    const rows = node.querySelectorAll("tbody tr");
    for (let i = 0; i < trips.length; i++) {
      const row = rows[i];
      const trip = trips[i];
      const statusCell = row.querySelector("td:nth-child(8)");

      if (trip.status_id === 1) {
        // ACTIVE(1)
        expect(statusCell.querySelector(".btn-danger").textContent).toBe("Cancel");
      } else if (trip.status_id === 2) {
        // CANCELLED(2)
        expect(statusCell.querySelector(".bg-danger").textContent).toBe("Cancelled");
      } else if (trip.status_id === 3) {
        // REQUESTED(3)
        expect(statusCell.querySelector(".btn-danger").textContent).toBe("Cancel");
      } else {
        // If past trip
        expect(statusCell.querySelector(".bg-success").textContent).toBe("Completed");
      }
    }
  });
});
