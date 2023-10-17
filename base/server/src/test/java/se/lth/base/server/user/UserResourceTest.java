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

    @Test
    public void notAuthenticatedCurrentUser() {
        User user = target("user").request().get(User.class);
        assertEquals(Role.NONE, user.getRole());
    }

    @Test(expected = ForbiddenException.class)
    public void notAuthenticatedGetAllUsers() {
        target("user").path("all").request().get(USER_LIST);
    }

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

    @Test(expected = ForbiddenException.class)
    public void getAllUsersAsUser() {
        login(TEST_CREDENTIALS);
        target("user").path("all").request().get(USER_LIST);
    }

    @Test(expected = ForbiddenException.class)
    public void getUserAsUser() {
        login(TEST_CREDENTIALS);
        target("user").path(Integer.toString(ADMIN.getId())).request().get(User.class);
    }

    @Test(expected = NotFoundException.class)
    public void deleteYourselfAsUser() {
        login(TEST_CREDENTIALS);
        target("user").path(Integer.toString(TEST.getId())).request().delete(Void.class); // Include response type to
                                                                                          // trigger exception
        // Now that we have deleted we try to get the deleted. Expect not found
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(TEST.getId())).request().get(Void.class);
    }

    @Test(expected = ForbiddenException.class)
    public void deleteOtherUserAsUser() {
        Credentials newCredentials = new Credentials("pelle", "passphrase1", Role.USER, "User", "User",
                "user@user03.se", "+4600000001");
        User newUser = createNewUser(newCredentials);
        login(TEST_CREDENTIALS);
        target("user").path(Integer.toString(newUser.getId())).request().delete(Void.class);
    }

    @Test
    public void getAllUsers() {
        login(ADMIN_CREDENTIALS);
        List<User> users = target("user").path("all").request().get(USER_LIST);
        assertTrue(users.stream().mapToInt(User::getId).anyMatch(id -> id == ADMIN.getId()));
        assertTrue(users.stream().mapToInt(User::getId).anyMatch(id -> id == TEST.getId()));
    }

    @Test
    public void getRoles() {
        login(ADMIN_CREDENTIALS);
        Set<Role> roles = target("user").path("roles").request().get(new GenericType<Set<Role>>() {
        });
        assertEquals(Role.ALL_ROLES, roles);
    }

    /**
     * Test that we can add a new user and that we can login as the new user.
     * @desc test adding users and logging in
     * @task ETS-1035
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

    @Test
    public void getUser() {
        login(ADMIN_CREDENTIALS);
        User responseTest = target("user").path(Integer.toString(TEST.getId())).request().get(User.class);
        assertEquals(TEST.getId(), responseTest.getId());
        assertEquals(TEST.getName(), responseTest.getName());
        assertEquals(TEST.getRole(), responseTest.getRole());
    }

    @Test(expected = WebApplicationException.class)
    public void dontDeleteYourselfAsAdmin() {
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(ADMIN.getId())).request().delete(Void.class);
    }

    @Test(expected = NotFoundException.class)
    public void deleteTestUser() {
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(TEST.getId())).request().delete(Void.class);
        target("user").path(Integer.toString(TEST.getId())).request().get(User.class);
    }

    @Test(expected = NotFoundException.class)
    public void deleteMissing() {
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(-1)).request().delete(Void.class);
    }

    @Test(expected = NotFoundException.class)
    public void updateMissing() {
        login(ADMIN_CREDENTIALS);
        target("user").path(Integer.toString(-1)).request().put(Entity.json(TEST_CREDENTIALS), User.class);
    }

    @Test(expected = WebApplicationException.class)
    public void dontDemoteYourself() {
        login(ADMIN_CREDENTIALS);
        Credentials update = new Credentials("admin", "password", Role.USER, "User", "User", "user@user05.se",
                "+4600000001");
        target("user").path(Integer.toString(ADMIN.getId())).request().put(Entity.json(update), User.class);
    }

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
     * @param newCredentials the credentials of the new user
     * @return the new user
     */
    public User createNewUser(Credentials newCredentials) {
        login(ADMIN_CREDENTIALS);
        User newUser = target("user").request().post(Entity.json(newCredentials), User.class);
        return newUser;
    }

    @Test
    public void updateUserRoleAsAdmin() {
        login(ADMIN_CREDENTIALS);
        User user = target("user").path(Integer.toString(TEST.getId())).path("changerole").path(Role.ADMIN.toString())
                .request().put(Entity.json(""), User.class);
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test(expected = ForbiddenException.class)
    public void updateUserRoleAsUserToAdmin() {
        login(TEST_CREDENTIALS);
        User user = target("user").path(Integer.toString(TEST.getId())).path("changerole").path(Role.ADMIN.toString())
                .request().put(Entity.json(""), User.class);
    }

    @Test
    public void updateUserRole() {
        login(TEST_CREDENTIALS);
        User user = target("user").path(Integer.toString(TEST.getId())).path("changerole").path(Role.DRIVER.toString())
                .request().put(Entity.json(""), User.class);
        assertEquals(Role.DRIVER, user.getRole());
    }

    @Test
    public void changeUserPassword() {
        login(TEST_CREDENTIALS);
        Credentials newPassword = new Credentials("Test", "newPassword123", Role.USER);

        Map<String, Credentials> credentialsMap = new HashMap<>();
        credentialsMap.put("oldCredentials", TEST_CREDENTIALS);
        credentialsMap.put("newCredentials", newPassword);

        target("user").path("password").request().put(Entity.json(credentialsMap));

        logout();
        login(newPassword);
    }

    @Test(expected = DataAccessException.class)
    public void changeUserInvalidNewPassword() {
        login(TEST_CREDENTIALS);
        Credentials newPassword = new Credentials("Test", "pass", Role.USER);

        Map<String, Credentials> credentialsMap = new HashMap<>();
        credentialsMap.put("oldCredentials", TEST_CREDENTIALS);
        credentialsMap.put("newCredentials", newPassword);

        target("user").path("password").request().put(Entity.json(credentialsMap));
        logout();
        login(newPassword);
    }

    @Test(expected = DataAccessException.class)
    public void changeUserInvalidOldPassword() {
        login(TEST_CREDENTIALS);
        Credentials wrongOldPassword = new Credentials("Test", "Wrongpassword", Role.USER);
        Credentials newPassword = new Credentials("Test", "pass", Role.USER);

        Map<String, Credentials> credentialsMap = new HashMap<>();
        credentialsMap.put("oldCredentials", wrongOldPassword);
        credentialsMap.put("newCredentials", newPassword);

        target("user").path("password").request().put(Entity.json(credentialsMap));
        logout();
        login(newPassword);
    }
}
