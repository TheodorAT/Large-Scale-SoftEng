package se.lth.base.server.tripPassenger;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;
import se.lth.base.server.trip.Trip;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

/**
 * Tests for TripPassengerResourceTest
 * 
 * @author Anton Tingelholm
 */

public class TripPassengerResourceTest extends BaseResourceTest {

    private static final GenericType<List<Trip>> TRIP_LIST = new GenericType<List<Trip>>() {
    };

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

    /**
     * Test for the createTripPassenger() function.
     */
    @Test
    public void createTripPassenger() {
        logout();
        login(DRIVER_CREDENTIALS);
        Trip t = new Trip(1, 1, 1, 2, 1, 2, 2);
        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
        target("trip").request().post(e, Trip.class);

        logout();
        login(TEST_CREDENTIALS);
        int tripId = t.getId();
        Entity<Integer> ti = Entity.entity(tripId, MediaType.APPLICATION_JSON);
        TripPassenger tripPassenger = target("tripPassenger").request().post(ti, TripPassenger.class);

        assertEquals((Integer) TEST.getId(), tripPassenger.getPassengerId());
        assertEquals(tripId, tripPassenger.getTripId());
    }

    /**
     * Tests that a driver cannot book a trip as a passenger. Expects a ForbiddenException to be thrown.
     * 
     * @desc Test the route for booking a trip as the driver. It should return ForbiddenException.
     * 
     * @task ETS-1353
     * 
     * @story ETS-1339
     */
    @Test(expected = javax.ws.rs.ForbiddenException.class)
    public void bookTripAsDriver() {
        login(DRIVER_CREDENTIALS);
        Trip t = new Trip(1, 1, 1, 2, 1, 2, 2);
        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
        Trip trip = target("trip").request().post(e, Trip.class);

        Entity<Integer> tripId = Entity.entity(trip.getId(), MediaType.APPLICATION_JSON);
        target("tripPassenger").request().post(tripId, TripPassenger.class);
    }

    @Test
    public void cancelPassengerTrip() {
        logout();
        login(DRIVER_CREDENTIALS);
        Trip t = new Trip(1, 1, 1, 2, 1, 2, 2);
        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
        target("trip").request().post(e, Trip.class);

        logout();
        login(TEST_CREDENTIALS);
        int tripId = t.getId();
        Entity<Integer> ti = Entity.entity(tripId, MediaType.APPLICATION_JSON);
        TripPassenger tripPassenger = target("tripPassenger").request().post(ti, TripPassenger.class);

        assertEquals((Integer) TEST.getId(), tripPassenger.getPassengerId());

        target("tripPassenger").path(Integer.toString(1)).request().delete(Void.class);

        // why only admin
        logout();
        login(ADMIN_CREDENTIALS);

        List<Trip> trips = target("trip").path("passenger").path(Integer.toString(TEST.getId())).request()
                .get(TRIP_LIST);

        assertEquals(trips.size(), 0);

    }

    @Test
    public void getAvailableSeats() {
        logout();
        login(DRIVER_CREDENTIALS);
        Trip t = new Trip(1, DRIVER.getId(), 1, 2, 1, 2, 2);
        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
        target("trip").request().post(e, Trip.class);

        int availableSeats = target("tripPassenger/availableSeats").request().post(e, Integer.class);
        assertEquals(2, availableSeats);

        logout();
        login(TEST_CREDENTIALS);
        int tripId = t.getId();
        Entity<Integer> ti = Entity.entity(tripId, MediaType.APPLICATION_JSON);
        target("tripPassenger").request().post(ti, TripPassenger.class);

        availableSeats = target("tripPassenger/availableSeats").request().post(e, Integer.class);
        assertEquals(1, availableSeats);

    }

}
