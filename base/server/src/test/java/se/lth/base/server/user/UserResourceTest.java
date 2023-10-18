package se.lth.base.server.user;

import org.junit.Test;
import se.lth.base.server.BaseResourceTest;
import se.lth.base.server.database.DataAccessException;
import se.lth.base.server.user.Credentials;
import se.lth.base.server.user.Role;
import se.lth.base.server.user.User;
import se.lth.base.server.user.UserResource;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class UserResourceTest extends BaseResourceTest {

    private static final GenericType<List<User>> USER_LIST = new GenericType<List<User>>() {
    };

    /**
     * Test case to ensure that a user who is not authenticated has the role NONE.
     *
     * @desc Test that a user who is not authenticated has the role NONE.
     *
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test
    public void notAuthenticatedCurrentUser() {
        User user = target("user").request().get(User.class);
        assertEquals(Role.NONE, user.getRole());
    }

    /**
     * Tests that a ForbiddenException is thrown when attempting to get all users without authentication.
     *
     * @desc Test that a ForbiddenException is thrown when attempting to get all users without authentication.
     *
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test(expected = ForbiddenException.class)
    public void notAuthenticatedGetAllUsers() {
        target("user").path("all").request().get(USER_LIST);
    }

    /**
     * Tests the login functionality with cookies.
     *
     * @desc Test the login functionality with cookies
     *
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test
    public void loginCookies() {
        Response response = target("user").path("login").request().post(Entity.json(TEST_CREDENTIALS));
        Cookie responseCookie = response.getCookies().get(UserResource.USER_TOKEN);
        assertEquals("localhost", responseCookie.getDomain());
        assertEquals(UserResource.USER_TOKEN, responseCookie.getName());
        assertEquals("/rest", responseCookie.getPath());

        User userWithNoCookie = target("user").request().get(User.class);
        assertEquals(Role.NONE, userWithNoCookie.getRole());

        User userWithCookie = target("user").request().cookie(responseCookie).get(User.class);
        assertEquals(Role.USER, userWithCookie.getRole());
    }

    /**
     * Tests the login functionality with remember me cookie.
     * 
     * @desc Test the login functionality with remember me cookie
     *
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test
    public void loginRememberMeCookie() {
        Response loginWithRememberMe = target("user").path("login").queryParam("remember", "true").request()
                .post(Entity.json(TEST_CREDENTIALS));
        int maxAge = loginWithRememberMe.getCookies().get(UserResource.USER_TOKEN).getMaxAge();
        assertTrue(maxAge > 0);

        Response loginWithoutRememberMe = target("user").path("login").request().post(Entity.json(TEST_CREDENTIALS));
        int noMaxAge = loginWithoutRememberMe.getCookies().get(UserResource.USER_TOKEN).getMaxAge();
        assertEquals(-1, noMaxAge);
    }

    /**
     * Tests the logout functionality of the UserResource class. It tests that a user without a session cannot logout,
     * that a logged in user can logout successfully, and that the user's role is set to NONE after logout.
     *
     * @desc Test the logout functionality of the UserResource class
     *
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test
    public void logout() {
        Response noSessionLogout = target("user").path("logout").request().post(Entity.json(""));
        assertEquals("", noSessionLogout.getCookies().get(UserResource.USER_TOKEN).getValue());

        Response loginResponse = target("user").path("login").request().post(Entity.json(ADMIN_CREDENTIALS));
        assertFalse(loginResponse.getCookies().get(UserResource.USER_TOKEN).getValue().isEmpty());

        Response logoutResponse = target("user").path("logout").request()
                .cookie(loginResponse.getCookies().get(UserResource.USER_TOKEN)).post(Entity.json(""));
        assertTrue(logoutResponse.getCookies().get(UserResource.USER_TOKEN).getValue().isEmpty());

        User currentUserAfterLogout = target("user").request().get(User.class);
        assertEquals(Role.NONE, currentUserAfterLogout.getRole());
    }

    /**
     * Tests the GET request for user/all does not return a list of all users when called by a user. Should return a
     * ForbiddenException.
     * 
     * @desc Test that all users can not be retrieved by a user.
     * 
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test(expected = ForbiddenException.class)
    public void getAllUsersAsUser() {
        login(TEST_CREDENTIALS);
        target("user").path("all").request().get(USER_LIST);
    }

    /**
     * Tests the GET request for user/{id} does not return a user when called by a user. Should return a
     * ForbiddenException.
     * 
     * @desc Test that a user can not be retrieved by a user. Using path user/{id}
     * 
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test(expected = ForbiddenException.class)
    public void getUserAsUser() {
        login(TEST_CREDENTIALS);
        target("user").path(Integer.toString(ADMIN.getId())).request().get(User.class);
    }

    /**
     * Test that we can delete a user and that we can't get the deleted user.
     * 
     * @desc test deleting a user
     * 
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test(expected = NotFoundException.class)
    public void deleteYourselfAsUser() {
        login(TEST_CREDENTIALS);
        target("user").path(Integer.toString(TEST.getId())).request().delete(Void.class); // Include response type to
                                                                                          // trigger exception
        // Now that we have deleted we try to get the deleted. Expect not found
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(TEST.getId())).request().get(Void.class);
    }

    /**
     * Test that we can't delete another user as a user.
     * 
     * @desc test deleting another user
     * 
     * @task ETS-974
     * 
     * @story ETS-740
     */
    @Test(expected = ForbiddenException.class)
    public void deleteOtherUserAsUser() {
        Credentials newCredentials = new Credentials("pelle", "passphrase1", Role.USER, "User", "User",
                "user@user03.se", "+4600000001");
        User newUser = createNewUser(newCredentials);
        login(TEST_CREDENTIALS);
        target("user").path(Integer.toString(newUser.getId())).request().delete(Void.class);
    }

    /**
     * Tests the GET request for user/all. Should return a list of all users.
     * 
     * @desc Test that all users can be retrieved by an admin.
     * 
     * @task ETS-1406
     * 
     * @story ETS-1404
     */
    @Test
    public void getAllUsers() {
        login(ADMIN_CREDENTIALS);
        List<User> users = target("user").path("all").request().get(USER_LIST);
        assertTrue(users.stream().mapToInt(User::getId).anyMatch(id -> id == ADMIN.getId()));
        assertTrue(users.stream().mapToInt(User::getId).anyMatch(id -> id == TEST.getId()));
    }

    /**
     * Tests the GET request for user/roles. Should return a set of all roles.
     * 
     * @desc Test that all roles can be retrieved by an admin.
     * 
     * @task ETS-1406
     * 
     * @story ETS-1404
     */
    @Test
    public void getRoles() {
        login(ADMIN_CREDENTIALS);
        Set<Role> roles = target("user").path("roles").request().get(new GenericType<Set<Role>>() {
        });
        assertEquals(Role.ALL_ROLES, roles);
    }

    /**
     * Test that we can add a new user and that we can login as the new user.
     * 
     * @desc test adding users and logging in
     * 
     * @task ETS-1035
     * 
     * @story ETS-742
     */
    @Test
    public void testAddUser() {
        Credentials newCredentials = new Credentials("pelle", "passphrase1", Role.USER, "User", "User",
                "user@user04.se", "+4600000001");
        User newUser = createNewUser(newCredentials);
        assertEquals(newCredentials.getUsername(), newUser.getName());
        assertEquals(newCredentials.getRole(), newUser.getRole());
        assertTrue(newUser.getId() > 0);

        // Test if we can login as new user
        login(newCredentials);
        User currentUser = target("user").request().get(User.class);
        assertEquals(newUser.getId(), currentUser.getId());
    }

    /**
     * Tests the GET request for user/{id}. Should return the user with the id {id}.
     * 
     * @desc Test that a user can be retrieved by id.
     * 
     * @task ETS-1406
     * 
     * @story ETS-1404
     */
    @Test
    public void getUser() {
        login(ADMIN_CREDENTIALS);
        User responseTest = target("user").path(Integer.toString(TEST.getId())).request().get(User.class);
        assertEquals(TEST.getId(), responseTest.getId());
        assertEquals(TEST.getName(), responseTest.getName());
        assertEquals(TEST.getRole(), responseTest.getRole());
    }

    /**
     * Test deleting yourself as admin
     * 
     * @desc test deleting yourself as admin, expect exception
     * 
     * @task ETS-973
     * 
     * @story ETS-728
     */
    @Test(expected = WebApplicationException.class)
    public void dontDeleteYourselfAsAdmin() {
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(ADMIN.getId())).request().delete(Void.class);
    }

    /**
     * Test deleting another user as admin
     * 
     * @desc assure that an admin can delete another user
     * 
     * @task ETS-973
     * 
     * @story ETS-728
     */
    @Test(expected = NotFoundException.class)
    public void deleteTestUser() {
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(TEST.getId())).request().delete(Void.class);
        target("user").path(Integer.toString(TEST.getId())).request().get(User.class);
    }

    /**
     * Test deleting a user that does not exist
     * 
     * @desc assure that an admin cannot delete a user that doesn't exist
     * 
     * @task ETS-973
     * 
     * @story ETS-728
     */
    @Test(expected = NotFoundException.class)
    public void deleteMissing() {
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(-1)).request().delete(Void.class);
    }

    /**
     * Tests that updating a non-existing user results in a NotFoundException being thrown.
     * 
     * @desc test updating a non-existing user
     * 
     * @task ETS-1406
     * 
     * @story ETS-1404
     */
    @Test(expected = NotFoundException.class)
    public void updateMissing() {
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(-1)).request().put(Entity.json(TEST_CREDENTIALS), User.class);
    }

    /**
     * Tests that admin cannot demote themselves. Using request PUT on user/{id}
     * 
     * @desc test that admin cannot demote themselves
     * 
     * @task ETS-1406
     * 
     * @story ETS-1404
     */
    @Test(expected = WebApplicationException.class)
    public void dontDemoteYourself() {
        login(ADMIN_CREDENTIALS);
        Credentials update = new Credentials("admin", "password", Role.USER, "User", "User", "user@user05.se",
                "+4600000001");
        target("user").path(Integer.toString(ADMIN.getId())).request().put(Entity.json(update), User.class);
    }

    /**
     * Tests that users info can be updated. Using request PUT on user/{id}
     * 
     * @desc tests that users info can be updated
     * 
     * @task ETS-1406
     * 
     * @story ETS-1404
     */
    @Test
    public void updateUser() {
        login(ADMIN_CREDENTIALS);
        Credentials newTest = new Credentials("test2", null, Role.ADMIN, "User", "User", "user@user06.se",
                "+4600000001");
        User user = target("user").path(Integer.toString(TEST.getId())).request().put(Entity.json(newTest), User.class);
        assertEquals(TEST.getId(), user.getId());
        assertEquals(newTest.getUsername(), user.getName());
        assertEquals(newTest.getRole(), user.getRole());
    }

    /**
     * Helper method to create a new user
     * 
     * @param newCredentials
     *            the credentials of the new user
     * 
     * @return the new user
     */
    public User createNewUser(Credentials newCredentials) {
        login(ADMIN_CREDENTIALS);
        User newUser = target("user").request().post(Entity.json(newCredentials), User.class);
        return newUser;
    }

    /**
     * Test that an admin can change another users role
     * 
     * @desc Tests admins ability to change another users role
     * 
     * @task ETS-1283
     * 
     * @story ETS-738
     */
    @Test
    public void updateUserRoleAsAdmin() {
        login(ADMIN_CREDENTIALS);
        User user = target("user").path(Integer.toString(TEST.getId())).path("changerole").path(Role.ADMIN.toString())
                .request().put(Entity.json(""), User.class);
        assertEquals(Role.ADMIN, user.getRole());
    }

    /**
     * Test that a user cannot change their role to admin
     * 
     * @desc Tests to ensure that changing a users own role to admin is forbidden
     * 
     * @task ETS-1283
     * 
     * @story ETS-738
     */
    @Test(expected = ForbiddenException.class)
    public void updateUserRoleAsUserToAdmin() {
        login(TEST_CREDENTIALS);
        target("user").path(Integer.toString(TEST.getId())).path("changerole").path(Role.ADMIN.toString()).request()
                .put(Entity.json(""), User.class);
    }

    /**
     * Test that a user can change their own role. In this case to driver
     * 
     * @desc Tests the changing a users own role
     * 
     * @task ETS-1283
     * 
     * @story ETS-738
     */
    @Test
    public void updateUserRole() {
        login(TEST_CREDENTIALS);
        User user = target("user").path(Integer.toString(TEST.getId())).path("changerole").path(Role.DRIVER.toString())
                .request().put(Entity.json(""), User.class);
        assertEquals(Role.DRIVER, user.getRole());
    }

    /**
     * Tests the method updatePassword in UserResource class. The test method creates a current credential and a new
     * credential for the updatePassword method, then checks if User can login with new password.
     * 
     * @desc Test changing password of User.
     * 
     * @task ETS-1310
     * 
     * @story ETS-739
     */
    @Test
    public void changeUserPassword() {
        login(TEST_CREDENTIALS);
        Credentials newPassword = new Credentials("Test", "newPassword123", Role.USER);

        checkChangeCredentials(TEST_CREDENTIALS, newPassword);
    }

    /**
     * Tests the method updatePassword in UserResource class. The test method creates a current credential and a new
     * credential wtih invalid password for the updatePassword method, then checks if User can login with new password.
     * Expects DataAccessException.
     * 
     * @desc Test changing password of User to an invalid password.
     * 
     * @task ETS-1310
     * 
     * @story ETS-739
     */
    @Test(expected = DataAccessException.class)
    public void changeUserInvalidNewPassword() {
        login(TEST_CREDENTIALS);
        Credentials newPassword = new Credentials("Test", "pass", Role.USER);

        checkChangeCredentials(TEST_CREDENTIALS, newPassword);
    }

    /**
     * Tests the method updatePassword in UserResource class. The test method creates a current credential with
     * incorrect password and a new credential for the updatePassword method, then checks if User can login with new
     * password. Expects DataAccessException.
     * 
     * @desc Test changing password of User with invalid current credentials.
     * 
     * @task ETS-1310
     * 
     * @story ETS-739
     */
    @Test(expected = DataAccessException.class)
    public void changeUserInvalidOldPassword() {
        login(TEST_CREDENTIALS);
        Credentials wrongOldPassword = new Credentials("Test", "Wrongpassword", Role.USER);
        Credentials newPassword = new Credentials("Test", "pass", Role.USER);

        checkChangeCredentials(wrongOldPassword, newPassword);
    }

    /**
     * Helper method to check if a user's credentials can be changed successfully. Can throw a DataAccessException if
     * the old credentials are invalid.
     * 
     * @param oldCredentials
     *            The passed input of the users old credentials.
     * @param newCredentials
     *            The new credentials to be set for the user.
     */
    private void checkChangeCredentials(Credentials oldCredentials, Credentials newCredentials) {
        Map<String, Credentials> credentialsMap = new HashMap<>();
        credentialsMap.put("oldCredentials", oldCredentials);
        credentialsMap.put("newCredentials", newCredentials);

        target("user").path("password").request().put(Entity.json(credentialsMap));
        logout();
        login(newCredentials);
    }
}
