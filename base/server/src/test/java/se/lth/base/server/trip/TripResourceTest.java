package se.lth.base.server.trip;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;
import se.lth.base.server.trip.Trip;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotEquals;

public class TripResourceTest extends BaseResourceTest {

    private static final GenericType<List<Trip>> Trip_LIST = new GenericType<List<Trip>>() {
    };

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }


    @Test
    public void addTrip() {
        Map<String, String> parametersMap = Map.of(
            "fromLocation", "-1", 
            "toLocation", "-1", 
            "startTime", "10200", 
            "endTime", "10400"
        );
        System.out.println(Entity.json(parametersMap));
        Trip trip = target("trip").request().post(Entity.json(parametersMap), Trip.class);
        assertEquals(TEST.getId(), trip.getDriverId());
        assertEquals("10200", trip.getStartTime().getTime());
        assertEquals("10400", trip.getEndTime().getTime());
    }
}
