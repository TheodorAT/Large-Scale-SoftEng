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

  const test = new base.User({ username: "Test", role: "USER" });
  const admin = new base.User({ username: "Admin", role: "ADMIN" });

  const trips = [
    new base.Trip({
      id: 1,
      driverId: 1,
      fromLocationId: 1,
      toLocationId: 2,
      startTime: new Date().getTime() + 1000000,
      endTime: new Date().getTime() + 3000000,
      seatCapacity: 2,
      status_id: 1,
    }),
    new base.Trip({
      id: 2,
      driverId: 2,
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
      status_id: 1,
    }),
    new base.Trip({
      id: 4,
      driverId: 4,
      fromLocationId: 3,
      toLocationId: 4,
      startTime: new Date().getTime() + 12000000,
      endTime: new Date().getTime() + 30200000,
      seatCapacity: 4,
      status_id: 1,
    }),
  ];

  let node;
  let locationPromise;
  let adminPromise;
  let tripDriverPromise;
  let tripPassengerPromise;

  // Creates the controller by loading the my-trip.html and put it in the node variable
  beforeEach(function (done) {
    controller = base.myTripsController();
    //specHelper.spyOnRest();
    specHelper
      .fetchHtml("mytrips/my-trips.html", document.body)
      .then(function (n) {
        node = n;
      })
      .finally(done);
    locationPromise = Promise.resolve(locations.slice(0));
    spyOn(base.rest, "getLocations").and.returnValue(locationPromise);
    adminPromise = Promise.resolve(admin);
    spyOn(base.rest, "getUser").and.returnValues(adminPromise);
    tripDriverPromise = Promise.resolve(trips.slice(0, 2));
    spyOn(base.rest, "getDriverTrips").and.returnValue(tripDriverPromise);
    tripPassengerPromise = Promise.resolve([]);
    spyOn(base.rest, "getPassengerTrips").and.returnValue(tripPassengerPromise);
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
  it("should fetch passenger and driver trips on load when admin", function (done) {
    controller.load();
    let promise = [locationPromise, adminPromise];
    Promise.all(promise)
      .then(function () {
        expect(base.rest.getLocations).toHaveBeenCalled();
        expect(base.rest.getUser).toHaveBeenCalled();
        Promise.all([tripDriverPromise, tripPassengerPromise])
          .then(function () {
            expect(base.rest.getPassengerTrips).toHaveBeenCalled();
            expect(base.rest.getDriverTrips).toHaveBeenCalled();
          })
          .finally(done);
      })
      .finally(done);
  });
  /**
   * @desc test that only passenger trips are fetched on load and displayed in table, when user is passenger
   * @task ETS-1267
   * @story ETS-723
   */
  it("should  fetch passenger trips on load when passenger", function (done) {
    userPromise = Promise.resolve(test);
    base.rest.getUser = jasmine.createSpy().and.returnValue(userPromise);
    controller.load();
    let promise = [locationPromise, userPromise];
    Promise.all(promise)
      .then(function () {
        expect(base.rest.getLocations).toHaveBeenCalled();
        expect(base.rest.getUser).toHaveBeenCalled();
        tripPassengerPromise
          .then(function () {
            expect(base.rest.getPassengerTrips).toHaveBeenCalled();
          })
          .finally(done);
      })
      .finally(done);
  });
  /**
   * @desc test that desination and orgin of trip is displayed in table
   * @task ETS-1267
   * @story ETS-723
   */
  it("should have display destination and origin in table", function (done) {
    controller.load();
    let promise = [locationPromise, adminPromise];
    Promise.all(promise)
      .then(function () {
        expect(base.rest.getLocations).toHaveBeenCalled();
        expect(base.rest.getUser).toHaveBeenCalled();
        Promise.all([tripDriverPromise, tripPassengerPromise])
          .then(function () {
            const tr = node.querySelector("tbody tr");
            const tds = tr.querySelectorAll("td");
            expect(tds.length).toBe(8);
            expect(tds[0].textContent).toBe(locations[0].name + ", " + locations[0].municipality);
            expect(tds[1].textContent).toBe(locations[1].name + ", " + locations[1].municipality);
          })
          .finally(done);
      })
      .finally(done);
  });
});
