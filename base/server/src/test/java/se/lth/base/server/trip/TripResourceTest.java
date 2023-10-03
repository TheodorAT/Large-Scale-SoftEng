package se.lth.base.server.trip;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TripResourceTest extends BaseResourceTest {

    private static final GenericType<List<Trip>> TRIP_LIST = new GenericType<List<Trip>>() {
    };

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

 /*    @Test
    public void addTrip() {
        Trip t = new Trip(1, 1, 1, 2, 10200, 0, 4);

        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);

        Trip trip = target("trip").request().post(e, Trip.class);

        assertEquals(TEST.getId(), trip.getDriverId());
        assertEquals(10200, trip.getStartTime());

        // End time is 1 hour after start time
        assertEquals(3610200, trip.getEndTime());
    } */

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

    /**
     * Test method to validate the retrieval of available trips based on location parameters.
     * 
     * Test procedure: 1. Creating trips and performing HTTP POST request to add new trips to the database. 2.
     * Performing a HTTP GET request to retrieve the available trips matching the given parameteters. 3. Check the list
     * is not null or empty. 4. Validates the size of the list. 5. Validates the fromLocation and destination of the
     * first trip in the list. 6. TOOD - more tests to validate
     * 
     */
    /*
     * @Test public void availableTrips() { int fromLocationId = 1; int toLocationId = 2; Trip trip1 = new Trip(1, 1,
     * fromLocationId, toLocationId, 10200, 0, 4); Trip trip2 = new Trip(1, 1, fromLocationId, toLocationId, 10000, 0,
     * 4); Trip trip3 = new Trip(1, 1, fromLocationId + 1, toLocationId + 1, 10200, 0, 4);
     * 
     * for (int i = 0; i < 5; i++) { target("trip").request().post(Entity.entity(trip1, MediaType.APPLICATION_JSON),
     * Trip.class);
     * 
     * // adding other trips to the database if (i % 2 != 0) { target("trip").request().post(Entity.entity(trip2,
     * MediaType.APPLICATION_JSON), Trip.class); target("trip").request().post(Entity.entity(trip3,
     * MediaType.APPLICATION_JSON), Trip.class); } } // Adding the trips with start location 1 and end location 2 that
     * starts on or after 10200 List<Trip> trips = target("trip/search").queryParam("fromLocationId", fromLocationId)
     * .queryParam("toLocationId", toLocationId).queryParam("startTime", 10200).request() .get(new
     * GenericType<List<Trip>>() { });
     * 
     * assertNotNull(trips); assertFalse(trips.isEmpty()); assertEquals(trips.size(), 5);
     * assertEquals(trips.get(0).getFromLocationId(), fromLocationId); assertEquals(trips.get(0).getToLocationId(),
     * toLocationId); }
     */
}
