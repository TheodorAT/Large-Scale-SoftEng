package se.lth.base.server.trip;

import org.h2.engine.User;
import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;
import se.lth.base.server.tripPassenger.TripPassenger;
import se.lth.base.server.user.Credentials;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TripResourceTest extends BaseResourceTest {
    private static final GenericType<List<Trip>> TRIP_LIST = new GenericType<List<Trip>>() {
    };

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

    @Test
    public void addTrip() {
        logout();
        login(DRIVER_CREDENTIALS);
        Trip t = new Trip(1, 1, 1, 2, 10200, 0, 4);

        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);

        Trip trip = target("trip").request().post(e, Trip.class);

        assertEquals(DRIVER.getId(), trip.getDriverId());
        assertEquals(10200, trip.getStartTime());

        // End time is 1 hour after start time
        assertEquals(3610200, trip.getEndTime());
    }

    @Test(expected = javax.ws.rs.ForbiddenException.class)
    public void addTripAsPassenger() {
        Trip t = new Trip(1, 1, 1, 2, 10200, 0, 4);

        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);

        Trip trip = target("trip").request().post(e, Trip.class);

    }

    /**
     * Test method to validate the retrieval of available trips based on location parameters.
     * 
     * Test procedure: 1. Creating trips and performing HTTP POST request to add new trips to the database. 2.
     * Performing a HTTP GET request to retrieve the available trips matching the given parameteters. 3. Check the list
     * is not null or empty. 4. Validates the size of the list. 5. Validates the fromLocation and destination of the
     * first trip in the list. 6. TOOD - more tests to validate
     * 
     */
    @Test
    public void availableTrips() {
        logout();
        login(DRIVER_CREDENTIALS);
        int fromLocationId = 1;
        int toLocationId = 2;
        Trip trip1 = new Trip(1, 1, fromLocationId, toLocationId, 10200, 0, 4);
        Trip trip2 = new Trip(1, 1, fromLocationId, toLocationId, 10000, 0, 4);
        Trip trip3 = new Trip(1, 1, fromLocationId + 1, toLocationId + 1, 10200, 0, 4);

        for (int i = 0; i < 5; i++) {
            target("trip").request().post(Entity.entity(trip1, MediaType.APPLICATION_JSON), Trip.class);

            // adding other trips to the database
            if (i % 2 != 0) {
                target("trip").request().post(Entity.entity(trip2, MediaType.APPLICATION_JSON), Trip.class);
                target("trip").request().post(Entity.entity(trip3, MediaType.APPLICATION_JSON), Trip.class);
            }
        }

        logout();
        login(TEST_CREDENTIALS);
        // Adding the trips with start location 1 and end location 2 that starts on or
        // after 10200
        List<Trip> trips = target("trip/search").queryParam("fromLocationId", fromLocationId)
                .queryParam("toLocationId", toLocationId).queryParam("startTime", 10200).request()
                .get(new GenericType<List<Trip>>() {
                });

        assertNotNull(trips);
        assertFalse(trips.isEmpty());
        assertEquals(trips.size(), 5);
        assertEquals(trips.get(0).getFromLocationId(), fromLocationId);
        assertEquals(trips.get(0).getToLocationId(), toLocationId);
    }

    /**
     * Test method to validate the retrieval of all trips belonging to current driver user.
     * 
     * Test procedure: 1. Sign in to driver account. 2. Add trips for driver to database using HTTP POST request. 3.
     * Retrieve list of trips for driver using HTTP GET. 4. Compare size of list with number of added trips. 5. Compare
     * IDs of trips.
     */
    @Test
    public void getTripsFromDriver() {
        logout();
        login(DRIVER_CREDENTIALS);

        Trip t1 = new Trip(1, 1, 1, 2, 10200, 12600, 4);
        Trip t2 = new Trip(2, 1, 1, 2, 10300, 12700, 4);

        Entity<Trip> e1 = Entity.entity(t1, MediaType.APPLICATION_JSON);
        Entity<Trip> e2 = Entity.entity(t2, MediaType.APPLICATION_JSON);

        target("trip").request().post(e1, Trip.class);
        target("trip").request().post(e2, Trip.class);

        List<Trip> trips = target("trip").path("driver").request().get(TRIP_LIST);

        assertEquals(2, trips.size());
        assertEquals(t1.getId(), trips.get(0).getId());
        assertEquals(t2.getId(), trips.get(1).getId());
    }

    /**
     * Test method to validate the retrieval of all trips belonging to a specific driver.
     * 
     * Test procedure: 1. Sign in to driver account. 2. Add trips for driver to database using HTTP POST request. 3.
     * Validate that non admin user is not able to retrieve list using driver/{driverId} path. 4. Sign in to admin
     * account. 5. Retrieve list of trips for driverId using HTTP GET. 6. Compare size of list with number of added
     * trips. 7. Compare IDs of trips.
     */
    @Test
    public void getTripsFromDriverId() {
        logout();
        login(DRIVER_CREDENTIALS);

        Trip t1 = new Trip(1, DRIVER.getId(), 1, 2, 10200, 12600, 4);
        Trip t2 = new Trip(2, DRIVER.getId(), 1, 2, 10300, 12700, 4);

        Entity<Trip> e1 = Entity.entity(t1, MediaType.APPLICATION_JSON);
        Entity<Trip> e2 = Entity.entity(t2, MediaType.APPLICATION_JSON);

        target("trip").request().post(e1, Trip.class);
        target("trip").request().post(e2, Trip.class);

        assertThrows(javax.ws.rs.ForbiddenException.class, () -> {
            target("trip").path("driver").path(Integer.toString(DRIVER.getId())).request().get(TRIP_LIST);
        });

        logout();
        login(ADMIN_CREDENTIALS);

        List<Trip> trips = target("trip").path("driver").path(Integer.toString(DRIVER.getId())).request()
                .get(TRIP_LIST);

        assertEquals(2, trips.size());
        assertEquals(t1.getId(), trips.get(0).getId());
        assertEquals(t2.getId(), trips.get(1).getId());
    }

    /**
     * Test method to validate the retrieval of all trips booked by current passenger user.
     * 
     * Test procedure: 1. Sign in to driver account. 2. Add trips for driver to database using HTTP POST request. 3.
     * Switch to passenger account. 4. Add booked trips as passenger to database using HTTP POST. 4. Retrieve list of
     * booked trips as passenger. 5. Compare size of list with number of booked trips. 6. Compare IDs of trips.
     */
    @Test
    public void getTripsAsPassenger() {
        addTestTrips();

        List<Trip> trips = target("trip").path("passenger").request().get(TRIP_LIST);

        assertEquals(2, trips.size());
        assertEquals(1, trips.get(0).getId());
        assertEquals(2, trips.get(1).getId());
    }

    /**
     * Test method to validate the retrieval of all trips booked by a specific passengerId.
     * 
     * Test procedure: 1. Sign in to driver account. 2. Add trips for driver to database using HTTP POST request. 3.
     * Switch to passenger account. 4. Add booked trips as passenger to database using HTTP POST. 4. Validate that non
     * admin user is not able to retrieve list using passenger/{passengerId} path. 5. Sign in to admin account. 6.
     * Retrieve list of booked trips for passengerId. 7. Compare size of list with number of booked trips. 8. Compare
     * IDs of trips.
     */
    @Test
    public void getTripsAsPassengerId() {

        addTestTrips();

        assertThrows(javax.ws.rs.ForbiddenException.class, () -> {
            target("trip").path("passenger").path(Integer.toString(TEST.getId())).request().get(TRIP_LIST);
        });

        logout();
        login(ADMIN_CREDENTIALS);

        List<Trip> trips = target("trip").path("passenger").path(Integer.toString(TEST.getId())).request()
                .get(TRIP_LIST);

        assertEquals(2, trips.size());
        assertEquals(1, trips.get(0).getId());
        assertEquals(2, trips.get(1).getId());
    }

    private void addTestTrips() {
        logout();
        login(DRIVER_CREDENTIALS);

        Trip t1 = new Trip(1, DRIVER.getId(), 1, 2, 10200, 12600, 4);
        Trip t2 = new Trip(2, DRIVER.getId(), 1, 2, 10300, 12700, 4);

        Entity<Trip> e1 = Entity.entity(t1, MediaType.APPLICATION_JSON);
        Entity<Trip> e2 = Entity.entity(t2, MediaType.APPLICATION_JSON);

        target("trip").request().post(e1, Trip.class);
        target("trip").request().post(e2, Trip.class);

        logout();
        login(TEST_CREDENTIALS);

        Entity<Integer> eId1 = Entity.entity(t1.getId(), MediaType.APPLICATION_JSON);
        Entity<Integer> eId2 = Entity.entity(t2.getId(), MediaType.APPLICATION_JSON);

        target("tripPassenger").request().post(eId1, TripPassenger.class);
        target("tripPassenger").request().post(eId2, TripPassenger.class);
    }

    @Test
    public void availableTripsAfter1Day() {
        logout();
        login(DRIVER_CREDENTIALS);
        int fromLocationId = 1;
        int toLocationId = 2;

        for (int i = 0; i < 5; i++) {
            // Seperate trips start time by 6 hours (21600000 ms)
            Trip trip = new Trip(1, 1, fromLocationId, toLocationId, i * 21600000, 0, 4);
            target("trip").request().post(Entity.entity(trip, MediaType.APPLICATION_JSON), Trip.class);
        }

        logout();
        login(TEST_CREDENTIALS);
        // Adding the trips with start location 1 and end location 2 that starts on or
        // after 10200
        List<Trip> trips = target("trip/search").queryParam("fromLocationId", fromLocationId)
                .queryParam("toLocationId", toLocationId).queryParam("startTime", 10200).request()
                .get(new GenericType<List<Trip>>() {
                });

        assertNotNull(trips);
        assertFalse(trips.isEmpty());
        assertEquals(4, trips.size());
        assertEquals(fromLocationId, trips.get(0).getFromLocationId());
        assertEquals(toLocationId, trips.get(0).getToLocationId());
    }

    @Test
    public void cancelDriverTrip() {
        logout();
        login(DRIVER_CREDENTIALS);
        int fromLocationId = 1;
        int toLocationId = 2;
        Trip trip1 = new Trip(1, TEST.getId(), fromLocationId, toLocationId, 10200, 0, 4);
        Trip trip2 = new Trip(2, TEST.getId(), fromLocationId, toLocationId, 10200, 0, 4);
        Trip trip3 = new Trip(3, TEST.getId(), fromLocationId, toLocationId, 10200, 0, 4);

        target("trip").request().post(Entity.entity(trip1, MediaType.APPLICATION_JSON), Trip.class);
        target("trip").request().post(Entity.entity(trip2, MediaType.APPLICATION_JSON), Trip.class);
        target("trip").request().post(Entity.entity(trip3, MediaType.APPLICATION_JSON), Trip.class);

    }

    @Test
    public void requestTrip() {
        Trip returnedTrip = createSampleTrip("trip/passenger/request", TEST_CREDENTIALS);

        assertEquals(0, returnedTrip.getDriverId());
    }

    @Test
    public void updateDriver() {
        Trip returnedTrip = createSampleTrip("trip/passenger/request", TEST_CREDENTIALS);

        assertEquals(0, returnedTrip.getDriverId());
        assertEquals(TripStatus.REQUESTED.getTripStatus(), returnedTrip.getStatus());

        Trip updatedTrip = updateTripDriver(returnedTrip.getId(), DRIVER_CREDENTIALS);

        assertEquals(DRIVER.getId(), updatedTrip.getDriverId());
        assertEquals(TripStatus.ACTIVE.getTripStatus(), updatedTrip.getStatus());
    }

    @Test
    public void updateDriverTripWithDriver() {
        Trip returnedTrip = createSampleTrip("trip", ADMIN_CREDENTIALS);

        assertEquals(ADMIN.getId(), returnedTrip.getDriverId());

        Trip updatedTrip = updateTripDriver(returnedTrip.getId(), DRIVER_CREDENTIALS);

        // Driver did not change
        assertEquals(ADMIN.getId(), updatedTrip.getDriverId());
    }

    private Trip createSampleTrip(String path, Credentials credentials) {
        login(credentials);

        Trip trip = new Trip(0, 0, 1, 2, 10200, 0, 4);
        Entity<Trip> e = Entity.entity(trip, MediaType.APPLICATION_JSON);

        Trip returnedTrip = target(path).request().post(e, Trip.class);
        return returnedTrip;
    }

    private Trip updateTripDriver(int tripId, Credentials credentials) {
        logout();
        login(credentials);

        Entity<Integer> eId = Entity.entity(0, MediaType.APPLICATION_JSON);

        return target("trip").path("" + tripId).request().put(eId, Trip.class);
    }
}
