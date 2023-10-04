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
    }),
    new base.Trip({
      id: 2,
      driverId: 2,
      fromLocationId: 2,
      toLocationId: 3,
      startTime: new Date().getTime() + 1000000,
      endTime: new Date().getTime() + 3000000,
      seatCapacity: 3,
    }),
    new base.Trip({
      id: 3,
      driverId: 3,
      fromLocationId: 3,
      toLocationId: 4,
      startTime: new Date().getTime() + 1000000,
      endTime: new Date().getTime() + 3000000,
      seatCapacity: 4,
    }),
    new base.Trip({
      id: 4,
      driverId: 4,
      fromLocationId: 3,
      toLocationId: 4,
      startTime: new Date().getTime() + 12000000,
      endTime: new Date().getTime() + 30200000,
      seatCapacity: 4,
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
    tripDriverPromise = Promise.resolve(trips.slice(0, 1));
    spyOn(base.rest, "getDriverTrips").and.returnValue(tripDriverPromise);
    tripPassengerPromise = Promise.resolve(trips.slice(1, 2));
    spyOn(base.rest, "getPassengerTrips").and.returnValue(tripPassengerPromise);
  });
  // Remove the node from the DOM
  afterEach(function () {
    document.body.removeChild(node);
  });

  it("should fetch passenger and driver trips on load when admin", function (done) {
    controller.load();
    let promise = [locationPromise, adminPromise];
    Promise.all(promise)
      .then(function () {
        expect(base.rest.getLocations).toHaveBeenCalled();
        expect(base.rest.getUser).toHaveBeenCalled();
        tripDriverPromise
          .then(function () {
            expect(base.rest.getDriverTrips).toHaveBeenCalled();
          })
          .finally(done);
        tripPassengerPromise
          .then(function () {
            expect(base.rest.getPassengerTrips).toHaveBeenCalled();
          })
          .finally(done);
      })
      .finally(done);
  });

  it("should fetch passenger trips on load when passenger", function (done) {
    userPromise = Promise.resolve(test);
    base.rest.getUser = jasmine.createSpy().and.returnValue(userPromise);
    controller.load();
    let promise = [locationPromise, userPromise];
    Promise.all(promise)
      .then(function () {
        expect(base.rest.getLocations).toHaveBeenCalled();
        expect(base.rest.getUser).toHaveBeenCalled();
        tripDriverPromise
          .then(function () {
            expect(base.rest.getDriverTrips).not.toHaveBeenCalled();
          })
          .finally(done);
        tripPassengerPromise
          .then(function () {
            expect(base.rest.getPassengerTrips).toHaveBeenCalled();
          })
          .finally(done);
      })
      .finally(done);
  });
  /*

    it('should populate table on load', function(done) {
        const foosPromise = Promise.resolve(foos.slice(0));
        spyOn(base.rest, 'getFoos').and.returnValue(foosPromise);
        controller.load();
        foosPromise.then(function() {
            // it should have populated the table with three rows
            const rows = node.querySelectorAll('tbody tr');
            expect(rows.length).toBe(foos.length);
        }).finally(done);
    });

    // Change here for lab 1
    it('should have total in table', function(done) {
        const foosPromise = Promise.resolve([foos[0]]);
        spyOn(base.rest, 'getFoos').and.returnValue(foosPromise);
        controller.load();
        foosPromise.then(function() {
            const tr = node.querySelector('tbody tr');
            const tds = tr.querySelectorAll('td');
            expect(tds.length).toBe(3);
            expect(tds[0].textContent).toBe(foos[0].payload);
            const d = foos[0].createdDate;
            expect(tds[1].textContent).toBe(d.toLocaleDateString() + ' ' + d.toLocaleTimeString());
            expect(tds[2].textContent).toBe(''+foos[0].total);
        }).finally(done);
     });

    describe('submit specs', function() {
        const mockFoo = new base.Foo({id: 4, userId: 1, payload: 'new data', created: 3});
        let fooPromise;

        beforeEach(function(done) {
            const foosPromise = Promise.resolve([]);
            fooPromise = Promise.resolve(mockFoo);
            spyOn(base.rest, 'getFoos').and.returnValue(foosPromise);
            spyOn(base.rest, 'addFoo').and.returnValue(fooPromise);
            controller.load();
            foosPromise.finally(done);
        });

        it('should call submitFoo after button click', function() {
            spyOn(controller, 'submitFoo');
            node.querySelector('input').value = mockFoo.payload;
            node.querySelector('button').click();
            expect(controller.submitFoo).toHaveBeenCalledWith();
        });

        it('should call rest api with foo', function(done) {
            node.querySelector('input').value = mockFoo.payload;
            node.querySelector('button').click();
            fooPromise.then(function() {
                expect(base.rest.addFoo).toHaveBeenCalledWith({payload: 'new data'});
            }).finally(done);
        });

        it('should not submit if there is no text in input', function() {
            spyOn(controller, 'submitFoo');
            node.querySelector('button').click();
            expect(controller.submitFoo).not.toHaveBeenCalled();
        });

        it('should add row after submit', function(done) {
            const input = node.querySelector('input');
            input.value = mockFoo.payload;
            controller.submitFoo(input);
            fooPromise.then(function() {
                expect(node.querySelectorAll('tbody tr').length).toBe(1);
                expect(node.querySelector('tbody td').textContent).toBe('new data');
            }).finally(done);
        });

        it('should clear input after submit', function(done) {
            const input = node.querySelector('input');
            input.value = mockFoo.payload;
            controller.submitFoo(input);
            fooPromise.then(function() {
                expect(input.value).toBe('');
                expect(node.querySelectorAll('tbody tr').length).toBe(1);
                expect(node.querySelector('tbody td').textContent).toBe('new data');
            }).finally(done);
        });
    }); */
});
