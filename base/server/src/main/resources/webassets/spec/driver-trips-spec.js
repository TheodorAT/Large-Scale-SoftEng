/*
 * Unit tests for the driverTrip controller
 * Author: Bianca Widstam
 */

const locations = [
  new base.Location({ locationId: 1, municipality: "Skåne", name: "Lund", latitude: 10000, longitude: 23213 }),
  new base.Location({ locationId: 2, municipality: "Halland", name: "Halmstad", latitude: 10030, longitude: 23313 }),
  new base.Location({ locationId: 3, municipality: "Skåne", name: "Malmö", latitude: 10000, longitude: 23213 }),
  new base.Location({ locationId: 4, municipality: "Halland", name: "Laholm", latitude: 10030, longitude: 23313 }),
];

describe("driverTripController", function () {
  let node;
  let locationPromise;

  // Creates the controller by loading the driver-trip.html and put it in the node variable
  beforeEach(function (done) {
    controller = base.driverTripController();
    specHelper
      .fetchHtml("drivertrips/driver-trips.html", document.body)
      .then(function (n) {
        node = n;
      })
      .finally(done);
    locationPromise = Promise.resolve(locations.slice(0));
    spyOn(base.rest, "getLocations").and.returnValue(locationPromise);
  });
  // Remove the node from the DOM
  afterEach(function () {
    document.body.removeChild(node);
  });

  it("should fetch locations on load", function (done) {
    controller.load();
    locationPromise
      .then(function () {
        expect(base.rest.getLocations).toHaveBeenCalled();
      })
      .finally(done);
  });

  describe("submit trip specs", function () {
    beforeEach(function (done) {
      controller.load();
      locationPromise
        .then(function () {
          expect(base.rest.getLocations).toHaveBeenCalled();
        })
        .finally(done);
    });

    it("should call submitDriverTrip after button click", function () {
      const form = { from: "Malmö, Skåne", to: "Lund, Skåne", seats: 1, startTime: "2024-06-12T19:30" };
      spyOn(controller, "submitDriverTrip");
      spyOn(controller, "getLocationId").and.returnValues(1, 3);
      document.getElementById("from").value = form.from;
      document.getElementById("to").value = form.to;
      document.getElementById("seats").value = form.seats;
      document.getElementById("startTime").value = form.startTime;
      document.getElementById("registerbtn").click();
      expect(controller.getLocationId).toHaveBeenCalledWith("Malmö, Skåne");
      expect(controller.getLocationId).toHaveBeenCalledWith("Lund, Skåne");
      expect(controller.submitDriverTrip).toHaveBeenCalledWith();
    });

    it("should not submit if there is no input in form", function () {
      spyOn(controller, "submitDriverTrip");
      node.querySelector("button").click();
      expect(controller.submitDriverTrip).not.toHaveBeenCalled();
    });
  });
});
