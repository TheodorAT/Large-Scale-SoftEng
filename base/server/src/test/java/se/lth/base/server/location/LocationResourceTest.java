package se.lth.base.server.location;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;

import javax.ws.rs.core.GenericType;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class LocationResourceTest extends BaseResourceTest {

    private static final GenericType<List<Location>> LOCATION_LIST = new GenericType<List<Location>>() {
    };

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

    @Test
    public void getAll() {
        List<Location> locations = target("location").path("all").request().get(LOCATION_LIST);

        // assertEquals(3, locations.size());
        // assertEquals("Test Location 1", locations.get(0).getName());
    }
}
