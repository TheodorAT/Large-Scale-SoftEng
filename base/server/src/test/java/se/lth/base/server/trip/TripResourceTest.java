package se.lth.base.server.trip;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;
import se.lth.base.server.trip.Trip;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
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
        Trip t = new Trip(1, 1, 1, 2, new Timestamp(10200), new Timestamp(12600), 4);

        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);

        Trip trip = target("trip").request().post(e, Trip.class);

        assertEquals(TEST.getId(), trip.getDriverId());
        assertEquals(10000, trip.getStartTime().getTime());
        assertEquals(12000, trip.getEndTime().getTime());
    }
}
