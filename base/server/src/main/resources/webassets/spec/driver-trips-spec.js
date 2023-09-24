/*
 * Unit tests for the driverTrip controller
 * Author: Bianca Widstam
 */
describe("driverTripController", function () {
  // (int id, int driverId, int fromLocationId, int toLocationId, long startTime, long endTime,int seatCapacity)
  const trips = [
    new base.Trip({
      id: 1,
      driverId: 1,
      fromLocationId: 1,
      toLocationId: 2,
      startTime: 120000,
      endTime: 140000,
      seatCapacity: 2,
    }),
    new base.Trip({
      id: 2,
      driverId: 2,
      fromLocationId: 2,
      toLocationId: 3,
      startTime: 140000,
      endTime: 16000,
      seatCapacity: 3,
    }),
    new base.Trip({
      id: 3,
      driverId: 3,
      fromLocationId: 3,
      toLocationId: 4,
      startTime: 130000,
      endTime: 170000,
      seatCapacity: 4,
    }),
  ];
  //(int locationId, String municipality, String name, double latitude, double longitude)
  const locations = [
    new base.Location({ locationId: 1, municipality: "Skåne", name: "Lund", latitude: 10000, longitude: 23213 }),
    new base.Location({ locationId: 2, municipality: "Halland", name: "Halmstad", latitude: 10030, longitude: 23313 }),
    new base.Location({ locationId: 3, municipality: "Skåne", name: "Malmö", latitude: 10000, longitude: 23213 }),
    new base.Location({ locationId: 4, municipality: "Halland", name: "Laholm", latitude: 10030, longitude: 23313 }),
  ];

  let node;
  let tripPromise;
  let locationPromise;

  // Creates the controller by loading the driver-trip.html and put it in the node variable
  beforeEach(function (done) {
    controller = base.driverTripController();
    //specHelper.spyOnRest();
    specHelper
      .fetchHtml("drivertrips/driver-trips.html", document.body)
      .then(function (n) {
        node = n;
      })
      .finally(done);
    locationPromise = Promise.resolve(locations.slice(0));
    spyOn(base.rest, "getLocations").and.returnValue(locationPromise);
    tripPromise = Promise.resolve(trips.slice(0));
    spyOn(base.rest, "getDriverTrips").and.returnValue(tripPromise);
  });
  // Remove the node from the DOM
  afterEach(function () {
    document.body.removeChild(node);
  });

  it("should fetch locations and trips on load", function (done) {
    controller.load();
    locationPromise
      .then(function () {
        expect(base.rest.getLocations).toHaveBeenCalled();
      })
      .then(function () {
        expect(base.rest.getDriverTrips).toHaveBeenCalled();
      })
      .finally(done);
  });

  it("should populate trip table on load", function (done) {
    controller.load();
    locationPromise
      .then(function () {
        expect(base.rest.getLocations).toHaveBeenCalled();
      })
      .then(function () {
        const rows = node.querySelectorAll("tbody tr");
        expect(rows.length).toBe(trips.length);
      })
      .finally(done);
  });

  it("should call submitDriverTrip after button click", function () {
    controller.load();
    const form = { from: "Malmö", to: "Lund", seats: 1, startTime: "2018-06-12T19:30" };
    spyOn(controller, "submitDriverTrip");
    document.getElementById("from").value = form.from;
    document.getElementById("to").value = form.to;
    document.getElementById("seats").value = form.seats;
    document.getElementById("startTime").value = form.startTime;
    document.getElementById("registerbtn").click();
    expect(controller.submitDriverTrip).toHaveBeenCalledWith();
  });

  it("should not submit if there is no input in form", function () {
    controller.load();
    spyOn(controller, "submitDriverTrip");
    node.querySelector("button").click();
    expect(controller.submitDriverTrip).not.toHaveBeenCalled();
  });
});
