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
     * Test for the createTripPassenger() function, checks that it correctly executes the bookTrip() function.
     * 
     * @desc Checks that it correctly calls on the bookTrip() function.
     * 
     * @task ETS-1087
     * 
     * @story ETS-593
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
     * Tests that a driver cannot book a trip as a passenger. Expects a InternalServerErrorException to be thrown.
     * 
     * @desc Test the route for booking a trip as the driver. It should return InternalServerErrorException.
     * 
     * @task ETS-1353
     * 
     * @story ETS-1339
     */
    @Test(expected = javax.ws.rs.InternalServerErrorException.class)
    public void bookTripAsDriver() {
        login(DRIVER_CREDENTIALS);
        Trip t = new Trip(1, 1, 1, 2, 1, 2, 2);
        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
        Trip trip = target("trip").request().post(e, Trip.class);

        Entity<Integer> tripId = Entity.entity(trip.getId(), MediaType.APPLICATION_JSON);
        target("tripPassenger").request().post(tripId, TripPassenger.class);
    }

    /**
     * Tests that a BadRequestException is thrown when trying to book a trip with no available seats.
     * 
     * @desc Tests that a BadRequestException is thrown when trying to book a trip with no available seats.
     * 
     * @task ETS-1407
     * 
     * @story ETS-1330
     */
    @Test(expected = javax.ws.rs.BadRequestException.class)
    public void bookTripNoAvailableSeats() {
        logout();
        login(DRIVER_CREDENTIALS);
        Trip t = new Trip(1, 1, 1, 2, 1, 2, 1);
        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
        target("trip").request().post(e, Trip.class);

        logout();
        login(ADMIN_CREDENTIALS);
        int tripId = t.getId();
        Entity<Integer> ti = Entity.entity(tripId, MediaType.APPLICATION_JSON);
        target("tripPassenger").request().post(ti, TripPassenger.class);

        logout();
        login(TEST_CREDENTIALS);
        target("tripPassenger").request().post(ti, TripPassenger.class);
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

    /**
     * Test method for checking that getAvailableSeats() returns the correct amount of available seats for a trip.
     * 
     * @desc Test for checking that getAvailableSeats() returns the correct amount of available seats for a trip.
     * 
     * @task ETS-1331
     * 
     * @story ETS-1330
     */
    @Test
    public void getAvailableSeats() {
        logout();
        login(DRIVER_CREDENTIALS);
        Trip trip = new Trip(1, DRIVER.getId(), 1, 2, 1, 2, 2);
        Entity<Trip> e = Entity.entity(trip, MediaType.APPLICATION_JSON);
        target("trip").request().post(e, Trip.class);

        int tripId = trip.getId();
        Entity<Integer> t = Entity.entity(tripId, MediaType.APPLICATION_JSON);

        int availableSeats = target("tripPassenger/availableSeats/").path(Integer.toString(tripId)).request()
                .get(Integer.class);
        assertEquals(2, availableSeats);

        logout();
        login(TEST_CREDENTIALS);

        target("tripPassenger").request().post(t, TripPassenger.class);

        availableSeats = target("tripPassenger/availableSeats/").path(Integer.toString(tripId)).request()
                .get(Integer.class);
        assertEquals(1, availableSeats);

    }

}
