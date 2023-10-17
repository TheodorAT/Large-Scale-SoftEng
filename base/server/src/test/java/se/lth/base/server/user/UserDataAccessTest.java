package se.lth.base.server.user;

import org.junit.Test;

import javassist.NotFoundException;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;
import se.lth.base.server.database.DataAccessException;
import se.lth.base.server.user.*;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.BadRequestException;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Rasmus Ros, rasmus.ros@cs.lth.se
 */
public class UserDataAccessTest extends BaseDataAccessTest {

    private final UserDataAccess userDao = new UserDataAccess(Config.instance().getDatabaseDriver());

    /**
     * Test that a user can be added and retrieved.
     * @desc test adding user to database and retrieving it
     * @task ETS-1035
     * @story ETS-742
     */
    @Test
    public void addNewUser() {
        userDao.addUser(
                new Credentials("Generic", "qwerty", Role.USER, "User", "User", "GenericUser@user.se", "+4600000001"));
        List<User> users = userDao.getUsers();
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("Generic") && u.getRole().equals(Role.USER)));
    }

    /**
     * Test adding two users with the same username
     * @desc test adding two users with the same username, expect exception
     * @task ETS-1035
     * @story ETS-742
     */
    @Test(expected = DataAccessException.class)
    public void addDuplicatedUser() {
        userDao.addUser(
                new Credentials("Gandalf", "mellon", Role.USER, "User", "User", "user@user00.se", "+4600000001"));
        userDao.addUser(
                new Credentials("Gandalf", "vapenation", Role.USER, "User", "User", "user@user01.se", "+4600000001"));
    }

    /**
     * Test adding a user with a too short username
     * @desc test adding a user with a too short username, expect exception
     * @task ETS-1035
     * @story ETS-742
     */
    @Test(expected = DataAccessException.class)
    public void addShortUser() {
        userDao.addUser(new Credentials("Gry", "no", Role.USER, "User", "User", "user@user02.se", "+4600000001"));
    }

    @Test
    public void getUsersContainsAdmin() {
        assertTrue(userDao.getUsers().stream().anyMatch(u -> u.getRole().equals(Role.ADMIN)));
    }

    /**
     * Test that removing non-existing user returns false
     * @desc test that removing non-existing user returns false
     * @task ETS-973
     * @story ETS-740
     */
    @Test
    public void removeNoUser() {
        assertFalse(userDao.deleteUser(-1));
    }

    /**
     * Test that a user can remove themselves
     * @desc test that a user can remove themselves
     * @task ETS-974
     * @story ETS-740
     */
    @Test
    public void removeUser() {
        User user = userDao
                .addUser(new Credentials("Sven", "a", Role.ADMIN, "User", "User", "user@user3.se", "+4600000001"));
        assertTrue(userDao.getUsers().stream().anyMatch(u -> u.getName().equals("Sven")));
        userDao.deleteUser(user.getId());
        assertTrue(userDao.getUsers().stream().noneMatch(u -> u.getName().equals("Sven")));
    }

    @Test(expected = DataAccessException.class)
    public void authenticateNoUser() {
        userDao.authenticate(new Credentials("Waldo", "?", Role.NONE, "User", "User", "user@user4.se", "+4600000001"));
    }

    @Test
    public void authenticateNewUser() {
        userDao.addUser(new Credentials("Pelle", "!2", Role.USER, "User", "User", "user@user5.se", "+4600000001"));
        Session pellesSession = userDao.authenticate(new Credentials("Pelle", "!2", Role.NONE));
        assertEquals("Pelle", pellesSession.getUser().getName());
        assertNotNull(pellesSession.getSessionId());
    }

    @Test
    public void authenticateNewUserTwice() {
        userDao.addUser(new Credentials("Elin", "password", Role.USER, "User", "User", "user@user7.se", "+4600000001"));

        Session authenticated = userDao.authenticate(
                new Credentials("Elin", "password", Role.NONE, "User", "User", "user@user8.se", "+4600000001"));
        assertNotNull(authenticated);
        assertEquals("Elin", authenticated.getUser().getName());

        Session authenticatedAgain = userDao.authenticate(
                new Credentials("Elin", "password", Role.NONE, "User", "User", "user@user9.se", "+4600000001"));
        assertNotEquals(authenticated.getSessionId(), authenticatedAgain.getSessionId());
    }

    @Test
    public void removeNoSession() {
        assertFalse(userDao.removeSession(UUID.randomUUID()));
    }

    @Test
    public void removeSession() {
        userDao.addUser(
                new Credentials("MormorElsa", "kanelbulle", Role.USER, "User", "User", "user@user9.se", "+4600000001"));
        Session session = userDao.authenticate(new Credentials("MormorElsa", "kanelbulle", Role.NONE, "User", "User",
                "user@user10.se", "+4600000001"));
        assertTrue(userDao.removeSession(session.getSessionId()));
        assertFalse(userDao.removeSession(session.getSessionId()));
    }

    @Test(expected = DataAccessException.class)
    public void failedAuthenticate() {
        userDao.addUser(new Credentials("steffe", "kittylover1996!", Role.USER, "User", "User", "user@user11.se",
                "+4600000001"));
        userDao.authenticate(new Credentials("steffe", "cantrememberwhatitwas! nooo!", Role.NONE, "User", "User",
                "user@user12.se", "+4600000001"));
    }

    @Test
    public void checkSession() {
        userDao.addUser(new Credentials("uffe", "genius programmer", Role.ADMIN, "User", "User", "user@user13.se",
                "+4600000001"));
        Session session = userDao.authenticate(new Credentials("uffe", "genius programmer", Role.NONE));
        Session checked = userDao.getSession(session.getSessionId());
        assertEquals("uffe", checked.getUser().getName());
        assertEquals(session.getSessionId(), checked.getSessionId());
    }

    @Test
    public void checkRemovedSession() {
        userDao.addUser(new Credentials("lisa", "y", Role.ADMIN, "User", "User", "user@user15.se", "+4600000001"));
        Session session = userDao.authenticate(new Credentials("lisa", "y", Role.NONE));
        Session checked = userDao.getSession(session.getSessionId());
        assertEquals(session.getSessionId(), checked.getSessionId());
        userDao.removeSession(checked.getSessionId());
        try {
            userDao.getSession(checked.getSessionId());
            fail("Should not validate removed session");
        } catch (DataAccessException ignored) {
        }
    }

    @Test
    public void getUser() {
        User user = userDao.getUser(1);
        assertEquals(1, user.getId());
    }

    @Test(expected = DataAccessException.class)
    public void getMissingUser() {
        userDao.getUser(-1);
    }

    @Test(expected = DataAccessException.class)
    public void updateMissingUser() {
        userDao.updateUser(10,
                new Credentials("admin", "password", Role.ADMIN, "User", "User", "user@user17.se", "+4600000001"));
    }

    @Test
    public void updateUser() {
        User user = userDao.updateUser(2,
                new Credentials("test2", "newpass", Role.USER, "User", "User", "user@user18.se", "+4600000001"));
        assertEquals(2, user.getId());
        assertEquals("test2", user.getName());
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    public void updateWithoutPassword() {
        Session session1 = userDao.authenticate(new Credentials("Test", "password", Role.USER));
        userDao.updateUser(2, new Credentials("test2", null, Role.USER));
        Session session2 = userDao.authenticate(new Credentials("test2", "password", Role.USER));
        System.out.println(session1);
        System.out.println(session2);
    }

    @Test
    public void updateUserRole() {
        userDao.updateUserRole(2, Role.ADMIN);
        User user = userDao.getUser(2);
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    public void changePassword() {
        Credentials oldCredentials = new Credentials("Sven", "password", Role.USER, "User", "User", "user@user3.se",
                "+4600000001");
        User user = userDao.addUser(oldCredentials);
        Session session1 = userDao.authenticate(new Credentials("Sven", "password", Role.USER));
        Credentials newCredentials = new Credentials("Sven", "newPassword123123", Role.USER);
        userDao.updateUserPassword(user.getId(), oldCredentials, newCredentials);
        Session session2 = userDao.authenticate(newCredentials);
    }

    @Test(expected = DataAccessException.class)
    public void changePasswordIncorrectOldCredentials() {
        Credentials oldCredentials = new Credentials("Sven", "password", Role.USER, "User", "User", "user@user3.se",
                "+4600000001");
        User user = userDao.addUser(oldCredentials);
        Session session1 = userDao.authenticate(new Credentials("Sven", "password", Role.USER));
        Credentials incorrectOldCredentials = new Credentials("Sven", "wrongPassword", Role.USER, "User", "User",
                "user@user3.se", "+4600000001");
        Credentials newCredentials = new Credentials("Sven", "newPassword123123", Role.USER);
        userDao.updateUserPassword(user.getId(), incorrectOldCredentials, newCredentials);
        Session session2 = userDao.authenticate(newCredentials);
    }

    @Test(expected = DataAccessException.class)
    public void changeInvalidPassword() {
        Credentials oldCredentials = new Credentials("Sven", "password", Role.USER, "User", "User", "user@user3.se",
                "+4600000001");
        User user = userDao.addUser(oldCredentials);
        Session session1 = userDao.authenticate(new Credentials("Sven", "password", Role.USER));
        Credentials newCredentials = new Credentials("Sven", "pass", Role.USER);
        userDao.updateUserPassword(user.getId(), oldCredentials, newCredentials);
        Session session2 = userDao.authenticate(newCredentials);
    }
}
