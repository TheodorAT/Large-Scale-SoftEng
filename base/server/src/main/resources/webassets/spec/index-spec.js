/*
 * Spec for mainController, for the portion that can be easily unit tested.
 * Author: Rasmus Ros, rasmus.ros@cs.lth.se
 */
describe("mainController", function () {
  const none = new base.User({ username: "-", role: "NONE" });
  const test = new base.User({ username: "Test", role: "USER" });
  const admin = new base.User({ username: "Admin", role: "ADMIN" });

  const r1 = jasmine.createSpyObj("r1", ["load"]);
  const r2 = jasmine.createSpyObj("r2", ["load"]);
  const fakeRoute = {
    r1: {
      partial: "r1.html",
      controller: function () {
        return r1;
      },
    },
    r2: {
      partial: "r2.html",
      controller: function () {
        return r2;
      },
    },
  };

  let node;
  // Creates the controller by loading the index.html and put it in the node variable
  beforeEach(function (done) {
    specHelper
      .fetchHtml("index.html", document.body)
      .then(function (n) {
        node = n;
      })
      .finally(done);

    // This disables the loading of all controllers and their partial HTML
    base.mainController.routingTable = fakeRoute;
  });
  // Remove the node from the DOM
  afterEach(function () {
    document.body.removeChild(node);
    window.onhashchange = null;
    window.location.hash = "";
  });
  /**
   * @desc test that default route is set if none is specified
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should set route to default if none is specified", function (done) {
    const userPromise = Promise.resolve(test);
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    base.mainController.load();
    userPromise
      .then(function () {
        expect(window.location.hash).toBe("#/r1");
      })
      .finally(done);
  });
  /**
   * @desc test that default route is set if user tries to navigate to a missing route
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should redirect bad route to default", function (done) {
    const userPromise = Promise.resolve(test);
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    window.location.hash = "/missing";
    base.mainController.load();
    userPromise
      .then(function () {
        expect(window.location.hash).toBe("#/r1");
      })
      .finally(done);
  });
  /**
   * @desc test that controller of r1 is loaded
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should load controller of r1", function (done) {
    const userPromise = Promise.resolve(admin);
    const fetchPromise = Promise.resolve({ text: () => "html" });
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    spyOn(window, "fetch").and.returnValue(fetchPromise);

    window.location.hash = "/r1";
    base.mainController.load();
    Promise.all([userPromise, fetchPromise])
      .then(function () {
        expect(r1.load).toHaveBeenCalled();
      })
      .finally(done);
  });
  /**
   * @desc test that partial of r2 is fectched
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should fetch partial of r2", function (done) {
    const userPromise = Promise.resolve(admin);
    const fetchPromise = Promise.resolve({ text: () => "html" });
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    spyOn(window, "fetch").and.returnValue(fetchPromise);

    window.location.hash = "/r2";
    base.mainController.load();
    Promise.all([userPromise, fetchPromise])
      .then(function () {
        expect(window.location.hash).toBe("#/r2");
        expect(window.fetch).toHaveBeenCalledWith("r2.html");
      })
      .finally(done);
  });
  /**
   * @desc test that the username is shown in the right side corner of navigation header
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should render username", function (done) {
    const userPromise = Promise.resolve(test);
    spyOn(base.mainController, "changeRoute");
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    base.mainController.load();
    userPromise
      .then(function () {
        expect(document.getElementById("username").textContent).toBe(test.username);
      })
      .finally(done);
  });
  /**
   * @desc test that if user is none the system redirects to login page
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should redirect to login if user is none", function (done) {
    const userPromise = Promise.resolve(none);
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    spyOn(base, "changeLocation");
    spyOn(base.mainController, "changeRoute");
    base.mainController.load();
    userPromise
      .then(function () {
        expect(base.changeLocation).toHaveBeenCalledWith("/login/login.html");
      })
      .finally(done);
  });
  /**
   * @desc test that user is redirected to login page after logging out
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should redirect to login after logout", function (done) {
    const userPromise = Promise.resolve(admin);
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    spyOn(base, "changeLocation");
    spyOn(base.mainController, "changeRoute");
    base.mainController.load();
    userPromise.then(function () {
      const logoutPromise = Promise.resolve({});
      spyOn(base.rest, "logout").and.returnValue(logoutPromise);
      document.getElementById("logout").click();
      logoutPromise
        .then(function () {
          expect(base.changeLocation).toHaveBeenCalledWith("/login/login.html");
        })
        .finally(done);
    });
  });

  /**
   * @desc test element is marked active in the navigation header
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should mark an element active in nav", function (done) {
    const userPromise = Promise.resolve(admin);
    spyOn(base.mainController, "changeRoute");
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    base.mainController.load();
    userPromise
      .then(function () {
        expect(document.querySelector("#main-nav .active")).toBeDefined();
      })
      .finally(done);
  });
  /**
   * @desc test that the admin tabs are hidden for users that are not admins
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should hide admin tabs from user", function (done) {
    const userPromise = Promise.resolve(test);
    spyOn(base.mainController, "changeRoute");
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    base.mainController.load();
    userPromise
      .then(function () {
        const list = document.querySelectorAll("#main-nav .admin-only");
        list.forEach(function (ao) {
          expect(ao.style.display).toBe("none");
        });
      })
      .finally(done);
  });
  /**
   * @desc test that the admin tabs are visible for admin users
   * @task ETS-1405
   * @story ETS-1404
   */
  it("should show admin tabs to admin", function (done) {
    const userPromise = Promise.resolve(admin);
    spyOn(base.mainController, "changeRoute");
    spyOn(base.rest, "getUser").and.returnValue(userPromise);
    base.mainController.load();
    userPromise
      .then(function () {
        const list = document.querySelectorAll("#main-nav .admin-only");
        list.forEach(function (ao) {
          expect(ao.style.display).not.toBe("none");
        });
      })
      .finally(done);
  });
});
