/*
 * Unit tests for the user admin controller and user classes.
 * Author: Amanda Nystedt
 */

describe("user specs", function () {
  const none = new base.User({ username: "-", role: "NONE", id: 0 });
  const admin = new base.User({ username: "Admin", role: "ADMIN", id: 1 });
  const test = new base.User({ username: "Test", role: "USER", id: 2 });

  describe("User class", function () {
    /**
     * @desc test that isAdmin returns true if user is admin
     * @task ETS-1405
     * @story ETS-1404
     */
    it("isAdmin should return true for ADMIN", function () {
      expect(admin.isAdmin()).toBe(true);
      expect(test.isAdmin()).toBe(false);
      expect(none.isAdmin()).toBe(false);
    });
    /**
     * @desc test that isNone returns true if user is none
     * @task ETS-1405
     * @story ETS-1404
     */
    it("isNone should return true for NONE", function () {
      expect(admin.isNone()).toBe(false);
      expect(test.isNone()).toBe(false);
      expect(none.isNone()).toBe(true);
    });
  });

  describe("userAdminController", function () {
    let node;
    let controller;

    let startUsers = [admin, test];
    let roles = [admin.role, test.role];

    // Creates the controller by loading the admin.html and put it in the node variable
    beforeEach(function (done) {
      controller = base.userAdminController();
      const nodePromise = specHelper.fetchHtml("admin/user-admin.html", document.body);
      nodePromise
        .then(function (n) {
          node = n;
          return node;
        })
        .then(function () {
          const usersPromise = Promise.resolve(startUsers.slice(0));
          spyOn(base.rest, "getUsers").and.returnValue(usersPromise);
          const rolesPromise = Promise.resolve(roles);
          spyOn(base.rest, "getRoles").and.returnValue(rolesPromise);
          controller.load();
          return Promise.all([usersPromise, rolesPromise]);
        })
        .finally(done);
    });
    // Remove the node from the DOM
    afterEach(function () {
      document.body.removeChild(node);
    });

    describe("add admin", function () {
      const userData = {
        username: "newadmin",
        password: "password123",
        role: "ADMIN",
        first_name: "Adminfirst",
        last_name: "Adminlast",
        email: "admin@email.com",
      };
      const newUser = new base.User({
        username: userData.username,
        role: userData.role,
        first_name: userData.first_name,
        last_name: userData.last_name,
        email: userData.email,
        id: 3,
      });
      const userPromise = Promise.resolve(newUser);

      beforeEach(function () {
        document.getElementById("input-firstname").value = userData.first_name;
        document.getElementById("input-lastname").value = userData.last_name;
        document.getElementById("input-username").value = userData.username;
        document.getElementById("input-password").value = userData.password;
        document.getElementById("input-email").value = userData.email;
        spyOn(base.rest, "addUser").and.returnValue(userPromise);
      });
      /**
       * @desc test that addUser is called with the user data
       * @task ETS-1327
       * @story ETS-756
       */
      it("should post the user data", function (done) {
        document.getElementById("addNewBtn").click();
        userPromise
          .then(function () {
            expect(base.rest.addUser).toHaveBeenCalledWith(userData);
          })
          .finally(done);
      });
      /**
       * @desc test that added user is added to the user table
       * @task ETS-1214
       * @story ETS-858
       */
      it("should add user in user list", function (done) {
        document.getElementById("addNewBtn").click();
        userPromise
          .then(function () {
            expect(base.rest.addUser).toHaveBeenCalledWith(userData);
            let table = document.getElementById("user-table");
            expect(table.rows.length).toBe(4);
            let lastRow = table.rows[table.rows.length - 1];
            const userIdDataCell = lastRow.querySelector(".user-id-data");
            if (userIdDataCell) {
              userIdColumnValue = userIdDataCell.textContent;
              expect(userIdColumnValue).toBe("3");
            }
          })
          .finally(done);
      });
      /**
       * @desc test that an added user can be deleted and removed from table
       * @task ETS-1215
       * @story ETS-728
       */
      it("should be possible to delete the added user", function (done) {
        document.getElementById("addNewBtn").click();
        userPromise
          .then(function () {
            const deleteUserPromise = Promise.resolve({});
            spyOn(base.rest, "deleteUser").and.returnValue(deleteUserPromise);
            let table = document.getElementById("user-table");
            let row = table.rows[table.rows.length - 1];
            row.querySelector(".delete-user").click();
            document.getElementById("modal-delete-user").click();
            return deleteUserPromise;
          })
          .then(function () {
            let table = document.getElementById("user-table");
            expect(base.rest.deleteUser).toHaveBeenCalledWith("3");
            expect(table.rows.length).toBe(3);
            let firstRow = table.rows[table.rows.length - 2];
            let secondRow = table.rows[table.rows.length - 1];
            const firstUsername = firstRow.querySelector(".username-data").textContent;
            const secondUsername = secondRow.querySelector(".username-data").textContent;
            expect(firstUsername).toBe(startUsers[0].username);
            expect(secondUsername).toBe(startUsers[1].username);
          })
          .finally(done);
      });
    });
    describe("delete user", function () {
      /**
       * @desc test that deleteUser is called with userId when delete button is clicked and that user is removed from table
       * @task ETS-1215
       * @story ETS-728
       */
      it("should be possible to delete a selected user", function (done) {
        const deleteUserPromise = Promise.resolve({});
        spyOn(base.rest, "deleteUser").and.returnValue(deleteUserPromise);
        let table = document.getElementById("user-table");
        let row = table.rows[table.rows.length - 1];
        row.querySelector(".delete-user").click();
        document.getElementById("modal-delete-user").click();
        deleteUserPromise
          .then(function () {
            expect(base.rest.deleteUser).toHaveBeenCalledWith("2");
            expect(table.rows.length).toBe(2);
          })
          .finally(done);
      });
    });
  });
});
