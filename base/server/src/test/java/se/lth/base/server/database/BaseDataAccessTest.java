package se.lth.base.server.database;

import org.junit.After;
import org.junit.Before;
import se.lth.base.server.Config;
import se.lth.base.server.user.Credentials;
import se.lth.base.server.user.Role;
import se.lth.base.server.user.User;

import java.sql.SQLException;

/**
 * Base class for H2 database tests. The connection url configures an in-memory database.
 *
 * @author Rasmus Ros, rasmus.ros@cs.lth.se
 */
public abstract class BaseDataAccessTest {

    private static final String IN_MEM_DRIVER_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    protected static final User ADMIN = new User(1, Role.ADMIN, "Admin", "Admin", "Admin", "admin@admin.se",
            "+4600000000");
    protected static final Credentials ADMIN_CREDENTIALS = new Credentials("Admin", "password", Role.ADMIN, "User",
            "User", "user@user09.se", "+4600000001");
    protected static final User TEST = new User(2, Role.USER, "Test", "TestUser", "TestUser", "test@user.se",
            "+4600000000");
    protected static final Credentials TEST_CREDENTIALS = new Credentials("Test", "password", Role.USER, "User", "User",
            "user@user010.se", "+4600000001");
    protected static final User DRIVER = new User(3, Role.DRIVER, "Driver_Test", "Driver_first_name",
            "Driver_last_name", "test@driver.se", "+4600000000");
    protected static final Credentials DRIVER_CREDENTIALS = new Credentials("Driver", "password", Role.DRIVER);

    static {
        Config.instance().setDatabaseDriver(IN_MEM_DRIVER_URL);
    }

    @Before
    public void createDatabase() throws SQLException {
        new CreateSchema(IN_MEM_DRIVER_URL).createSchema();
    }

    @After
    public void deleteDatabase() throws SQLException {
        new CreateSchema(IN_MEM_DRIVER_URL).dropAll();
    }
}
