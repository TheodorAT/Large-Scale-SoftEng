var base = base || {};
base.rest = (function () {
  "use strict";

  // The available classes are defined here, feel free to move them to other files when this gets unwieldy.
  // Note the pattern where the class constructor takes an object that is parsed from JSON, and extends
  // itself using the JSON object with Object.assign.
  // In this way, we don't have to write: this.id = json.id; this.payload = json.payload etc.

  const Trip = function (json) {
    Object.assign(this, json);
    this.startTime = new Date(this.startTime);
    this.endTime = new Date(this.endTime);
  };
  const Location = function (json) {
    Object.assign(this, json);
  };

  const Role = function (role) {
    this.name = role;
    this.label = this.name[0] + this.name.toLowerCase().slice(1);
  };

  const User = function (json) {
    Object.assign(this, json);
    this.role = new Role(json.role);
    this.json = json;

    this.isAdmin = function () {
      return this.role.name === "ADMIN";
    };
    this.isNone = function () {
      return this.role.name === "NONE";
    };
  };

  // Expose the classes to base module, they are primarily used by the tests.
  base.User = User;
  base.Role = Role;
  base.Trip = Trip;
  base.Location = Location;

  // This method extends the functionality of fetch by adding default error handling.
  // Using it is entirely optional.
  const baseFetch = function (url, config) {
    // We create config if it does not already exist
    config = config || {};
    // Setting 'same-origin' make sure that cookies are sent to the server (which it would not otherwise)
    config.credentials = "same-origin";
    return fetch(url, config)
      .then(function (response) {
        if (!response.ok) {
          return new Promise((resolve) => resolve(response.json())).then(function (errorJson) {
            const status = errorJson.status;
            throw Error(`${errorJson.status} ${errorJson.error}\n${errorJson.message}`);
          });
        } else {
          return response;
        }
      })
      .catch(function (error) {
        alert(error);
        throw error;
      });
  };

  const jsonHeader = { "Content-Type": "application/json;charset=utf-8" };

  return {
    /*
     * Fetches the currently logged in user
     *
     * example: const me = base.rest.getUser();
     */
    getUser: function () {
      return baseFetch("/rest/user")
        .then((response) => response.json())
        .then((u) => new User(u));
    },

    /*
     * Login with given credentials.
     * username: name of the user
     * password: password in plaintext
     * rememberMe: boolean flag, whether the user
     *
     * example: base.rest.login('test', 'password', true);
     */
    login: function (username, password, rememberMe) {
      var loginObj = { username: username, password: password };
      return baseFetch("/rest/user/login?remember=" + rememberMe, {
        method: "POST",
        body: JSON.stringify(loginObj),
        headers: jsonHeader,
      });
    },

    createUser: function (user) {
      return baseFetch("/rest/user/", {
        method: "POST",
        body: JSON.stringify(user),
        headers: jsonHeader,
      });
    },

    /*
     * Logout the current user.
     *
     * example: base.rest.logout();
     */
    logout: function () {
      return baseFetch("/rest/user/logout", { method: "POST" });
    },

    /*
     * Gets the users available (admin only).
     * returns: A list of User
     *
     * example: const userList = base.rest.getUsers();
     */
    getUsers: function () {
      return baseFetch("/rest/user/all")
        .then((response) => response.json())
        .then((users) => users.map((u) => new User(u)));
    },

    /*
     * Gets the roles available (admin only).
     * returns: A list of Role
     *
     * example: const availableRoles = base.rest.getRoles();
     */
    getRoles: function () {
      return baseFetch("/rest/user/roles")
        .then((response) => response.json())
        .then((roles) => roles.map((r) => new Role(r)));
    },

    /*
     * Add a new user (admin only).
     * credentials: object with username, password, and role
     * returns: the updated User
     *
     * example: let user = base.rest.addUser(2, {'username': 'Test2', 'password': 'password2', 'role': 'USER');
     */
    addUser: function (credentials) {
      return baseFetch("/rest/user", {
        method: "POST",
        body: JSON.stringify(credentials),
        headers: jsonHeader,
      })
        .then((response) => response.json())
        .then((u) => new User(u));
    },

    /*
     * Replace a specific user with a given userId (admin only).
     * id: user to replace
     * credentials: object with username, password (optional), and role
     * returns: the updated user
     *
     * example: let user = base.rest.putUser(2, {'username': 'Test2', 'role': 'USER');
     */
    putUser: function (id, credentials) {
      return baseFetch("/rest/user/" + id, {
        method: "PUT",
        body: JSON.stringify(credentials),
        headers: jsonHeader,
      })
        .then((response) => response.json())
        .then((u) => new User(u));
    },

    /*
     * Delete a specific user with a given userId (admin only).
     * id: user to delete
     *
     * example: base.rest.deleteUser(2);
     */
    deleteUser: function (userId) {
      return baseFetch("/rest/user/" + userId, { method: "DELETE" });
    },

    /*
     * Fetches the locations
     * returns: an array of locations
     * example: const locations = base.rest.getLocations();
     */
    getLocations: function () {
      return baseFetch("/rest/location/all", { method: "GET" })
        .then((response) => response.json())
        .then((locations) => locations.map((l) => new Location(l)));
    },

    /*
     * Adds trip expects javascript object containing payload
     * trip: plain javascript object to add
     * returns: Trip object
     * example: const myTrip = base.rest.createTrip({fromLocationId: "id", toLocationId: "id", startTime: "time", seatCapacity: "seats"});
     */
    createTrip: function (trip) {
      return baseFetch("/rest/trip", {
        method: "POST",
        body: JSON.stringify(trip),
        headers: jsonHeader,
      })
        .then((response) => response.json())
        .then((f) => new Trip(f));
    },

    //TODO: get all trips with user_id, both as passenger and driver
    /* getAllTrips: function () {
      return baseFetch("/rest/trip/", {
        method: "GET",
      })
        .then((response) => response.json())
        .then((trips) => trips.map((f) => new Trip(f)));
    }, */

    /*
     * Fetches the trips of the driver
     * returns: an array of Trips
     * example: const trips = base.rest.getDriverTrips(1);
     */
    getDriverTrips: function () {
      return baseFetch("/rest/trip/driver", {
        method: "GET",
      })
        .then((response) => response.json())
        .then((trips) => trips.map((f) => new Trip(f)));
    },
  };
})();
