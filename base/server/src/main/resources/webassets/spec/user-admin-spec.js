/*
 * Unit tests for the user admin controller and user classes.
 * Author: Rasmus Ros, rasmus.ros@cs.lth.se
 */

describe("user specs", function () {
  const none = new base.User({ username: "-", role: "NONE", id: 0 });
  const admin = new base.User({ username: "Admin", role: "ADMIN", id: 1 });
  const test = new base.User({ username: "Test", role: "USER", id: 2 });

  describe("User class", function () {
    it("isAdmin should return true for ADMIN", function () {
      expect(admin.isAdmin()).toBe(true);
      expect(test.isAdmin()).toBe(false);
      expect(none.isAdmin()).toBe(false);
    });
    it("isNone should return true for NONE", function () {
      expect(admin.isNone()).toBe(false);
      expect(test.isNone()).toBe(false);
      expect(none.isNone()).toBe(true);
    });
  });

  /* describe("userAdminController", function () {
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

    describe("add user", function () {
      const credentials = { username: "new user", password: "qwerty123", role: roles[4].name };
      const newUser = new base.User({ username: credentials.username, role: credentials.role, id: 4 });
      const userPromise = Promise.resolve(newUser);

      beforeEach(function () {
        document.getElementById("new-admin").click();
        document.getElementById("input-username").value = credentials.username;
        document.getElementById("input-password").value = credentials.password;
        spyOn(base.rest, "addUser").and.returnValue(userPromise);
      });

      it("should post username, password, and role", function (done) {
        document.getElementById("submit-admin").click();
        userPromise
          .then(function () {
            expect(base.rest.addUser).toHaveBeenCalledWith(credentials);
          })
          .finally(done);
      });

      it("should be possible to delete the added user", function (done) {
        document.getElementById("submit-admin").click();
        userPromise
          .then(function () {
            const deleteUserPromise = Promise.resolve({});
            spyOn(base.rest, "deleteUser").and.returnValue(deleteUserPromise);
            document.getElementById("delete-user").click();
            return deleteUserPromise;
          })
          .then(function () {
            const items = document.querySelectorAll("#user-list button");
            expect(items.length).toBe(3);
            expect(items[0].textContent).toBe(startUsers[0].username);
            expect(items[1].textContent).toBe(startUsers[1].username);
            expect(items[2].textContent).not.toBe(newUser.username); // should be add user button
          })
          .finally(done);
      });
    });

    describe("delete user", function () {
      it("should be possible to delete a selected user", function (done) {
        const deleteUserPromise = Promise.resolve({});
        spyOn(base.rest, "deleteUser").and.returnValue(deleteUserPromise);
        document.getElementById("modal-delete-user").click();
        deleteUserPromise
          .then(function () {
            const items = document.querySelectorAll("#user-table");
            expect(items.length).toBe(2);
          })
          .finally(done);
      });
    });
    
      it("should update the left user list menu with new new username", function (done) {
        const userCredentials = { username: "New name", password: "new password", role: startUsers[0].role.name };
        const userPromise = Promise.resolve(
          new base.User({
            username: userCredentials.username,
            role: userCredentials.role,
            id: startUsers[0].id,
          }),
        );
        spyOn(base.rest, "putUser").and.returnValue(userPromise);
        document.getElementById("input-username").value = userCredentials.username;
        document.getElementById("input-password").disabled = false;
        document.getElementById("input-password").value = userCredentials.password;
        document.getElementById("submit-admin").click();
        userPromise
          .then(function () {
            const userBtns = document.querySelectorAll("user-id-data");
            expect(userBtns.length).toBe(3);
            expect(userBtns[0].textContent).toBe(userCredentials.username);
          })
          .finally(done);
      });
    });*/
});
