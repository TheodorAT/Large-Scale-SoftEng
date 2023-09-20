package se.lth.base.server.trip;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class TripResourceTest extends BaseResourceTest {

    private static final GenericType<List<Trip>> TRIP_LIST = new GenericType<List<Trip>>() {
    };

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

    @Test
    public void addTrip() {
        Trip t = new Trip(1, 1, 1, 2, 10000, 0, 4);

        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);

        Trip trip = target("trip").request().post(e, Trip.class);

        assertEquals(TEST.getId(), trip.getDriverId());
        assertEquals(10000, trip.getStartTime());

        // End time is 1 hour after start time
        assertEquals(3610000, trip.getEndTime());
    }

    /*
     * @Test public void getAllTripsFromDriverId() { Trip t = new Trip(1, 1, 1, 2, 10200, 12600, 4);
     * 
     * Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
     * 
     * Trip trip = target("trip").request().post(e, Trip.class);
     * 
     * assertEquals(TEST.getId(), trip.getDriverId());
     * 
     * List<Trip> trips = target("trip").path("driver").path(TEST.getId() + "").request().get(TRIP_LIST);
     * 
     * assertEquals(1, trips.size()); }
     */
}