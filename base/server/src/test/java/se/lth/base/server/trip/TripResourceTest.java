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
    private static final GenericType<List<TripPassenger>> TRIP_PASSENGER_LIST = new GenericType<List<TripPassenger>>() {
    };

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

    /**
     * Tests the addTrip method of the TripResource class.
     * 
     * @desc Test the addTrip method by creating a trip as a driver
     * 
     * @task ETS-988
     * 
     * @story ETS-592
     */
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

    /**
     * Tests that a passenger cannot add a trip. Expects a ForbiddenException to be thrown.
     * 
     * @desc Test the addTrip method by creating a trip as a passenger
     * 
     * @task ETS-988
     * 
     * @story ETS-592
     */
    @Test(expected = javax.ws.rs.ForbiddenException.class)
    public void addTripAsPassenger() {
        Trip t = new Trip(1, 1, 1, 2, 10200, 0, 4);

        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);

        Trip trip = target("trip").request().post(e, Trip.class);

    }

    /**
     * Test method to validate the retrieval of available trips based on location parameters. 
     * 
     * @desc validate the retrieval of available trips based on location parameters
     * 
     * @task ETS-895
     * 
     * @story ETS-610
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
     * Test method for getTripsFromDriver(), validates the retrieval of all trips created by the current driver user.
     * 
     * @desc validates that getTripsFromDriver() returns the correct list of booked trips for a specific passenger id.
     * 
     * @task ETS-1306
     * 
     * @story ETS-27
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
     * Test method for getTripsFromDriver(), validates the retrieval of all trips created by a specific driverId.
     * 
     * @desc validates that getTripsFromDriver() returns the correct list of created trips for a specific driverId.
     * 
     * @task ETS-1306
     * 
     * @story ETS-27
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
     * Test method for getTripsAsPassenger(), validates the retrieval of all trips booked by the current passenger user.
     * 
     * @desc validates that getTripsAsPassenger() returns the correct list of booked trips by the current passenger
     *       user.
     * 
     * @task ETS-1306
     * 
     * @story ETS-27
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
     * Test method for getTripsAsPassenger(), validates the retrieval of all trips booked by a specific passengerId.
     * 
     * 
     * @desc validates that getTripsAsPassenger() returns the correct list of booked trips for a specific passenger id.
     * 
     * @task ETS-1306
     * 
     * @story ETS-27
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

    /**
     * Helper method to add a two test trips to the database. Works by login in as driver sending requests to create the
     * trips, then logging out and logging in as passenger and sending requests to book the trips.
     */
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

    /**
     * Tests the availability of trips after 1 day.
     * 
     * @desc Test the matching algorithm by checking that only trips within 1 day are returned
     * 
     * @task ETS-753
     * 
     * @story ETS-828
     */
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

    /**
     * Tests cancelling a trip as a driver.
     * 
     * @desc test creating trips and cancelling one
     * 
     * @task ETS-1296
     * 
     * @story ETS-731
     */
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

        target("trip").path("driver").path(Integer.toString(trip1.getId())).request().delete();

        Trip new_first_trip = target("trip").path("" + trip1.getId()).request().get(Trip.class);

        assertEquals(new_first_trip.getStatus(), TripStatus.CANCELLED.getTripStatus());
    }

    /**
     * Check whether the trip is removed from the database after the passenger cancels a requested trip.
     * 
     * @desc Test the deletion of a requested trip by passenger
     * 
     * @task ETS-1345
     * 
     * @story ETS-1339
     */
    @Test
    public void cancelRequestedTrip() {
        Trip returnedTrip = createSampleTrip("trip/passenger/request", TEST_CREDENTIALS);
        List<Trip> trips = target("trip").path("requests").request().get(TRIP_LIST);
        assertEquals(1, trips.size());

        target("tripPassenger").path(returnedTrip.getId() + "").request().delete();
        trips = target("trip").path("requests").request().get(TRIP_LIST);
        assertEquals(0, trips.size());
    }

    /**
     * @desc Test the requestTrip method by creating a trip as a passenger
     * 
     * @task ETS-1345
     * 
     * @story ETS-1339
     */
    @Test
    public void requestTrip() {
        Trip returnedTrip = createSampleTrip("trip/passenger/request", TEST_CREDENTIALS);
        List<TripPassenger> tripPassengers = target("tripPassenger").path("" + returnedTrip.getId()).request()
                .get(TRIP_PASSENGER_LIST);
        assertEquals(0, returnedTrip.getDriverId());
        assertEquals(TEST.getId(), tripPassengers.get(0).getPassengerId());
    }

    /**
     * @desc Test the updateTripDriver by creating a trip and then updating the driver
     * 
     * @task ETS-1346
     * 
     * @story ETS-1339
     */
    @Test
    public void updateDriver() {
        Trip returnedTrip = createSampleTrip("trip/passenger/request", TEST_CREDENTIALS);

        assertEquals(0, returnedTrip.getDriverId());
        assertEquals(TripStatus.REQUESTED.getTripStatus(), returnedTrip.getStatus());
        assertEquals(0, returnedTrip.getSeatCapacity());

        Trip updatedTrip = updateTripDriver(returnedTrip.getId(), DRIVER_CREDENTIALS);
        assertEquals(4, updatedTrip.getSeatCapacity());
        assertEquals(DRIVER.getId(), updatedTrip.getDriverId());
        assertEquals(TripStatus.ACTIVE.getTripStatus(), updatedTrip.getStatus());
    }

    /**
     * Tests the updateTripDriver method in the TripResource class when updating a trip that already has a driver.
     * 
     * @desc Test the updateTripDriver method in the TripResource class for a trip with a driver
     * 
     * @task ETS-1346
     * 
     * @story ETS-1339
     */
    @Test
    public void updateDriverTripWithDriver() {
        Trip returnedTrip = createSampleTrip("trip", ADMIN_CREDENTIALS);

        assertEquals(ADMIN.getId(), returnedTrip.getDriverId());

        Trip updatedTrip = updateTripDriver(returnedTrip.getId(), DRIVER_CREDENTIALS);

        // Driver did not change
        assertEquals(ADMIN.getId(), updatedTrip.getDriverId());
    }

    /**
     * Tests the getTripsWithoutDriver method of the TripResource class. Creates sample of driverless trips,
     * and checks if the request retrieved the same and only trips.
     * 
     * @desc Test retrieving trips without driver
     * 
     * @task ETS-1347
     * 
     * @story ETS-1339
     */
    @Test
    public void getTripsWithoutDriver() {
        createSampleTrip("trip/passenger/request", TEST_CREDENTIALS);
        createSampleTrip("trip/passenger/request", TEST_CREDENTIALS);
        createSampleTrip("trip/passenger/request", TEST_CREDENTIALS);

        List<Trip> tripsWithoutDriver = target("trip/requests").request().get(new GenericType<List<Trip>>() {
        });

        assertEquals(3, tripsWithoutDriver.size());
        for (int i = 0; i < tripsWithoutDriver.size(); i++) {
            assertEquals(0, tripsWithoutDriver.get(i).getDriverId());
        }
    }

    /**
     * Creates a sample trip by sending a POST request to the specified path with the given credentials and trip data.
     * 
     * @param path
     *            the path to send the POST request to
     * @param credentials
     *            the credentials to use for authentication
     * 
     * @return the created trip object
     */
    private Trip createSampleTrip(String path, Credentials credentials) {
        login(credentials);

        Trip trip = new Trip(0, 0, 1, 2, 10200, 0, 4);
        Entity<Trip> e = Entity.entity(trip, MediaType.APPLICATION_JSON);

        Trip returnedTrip = target(path).request().post(e, Trip.class);
        return returnedTrip;
    }

    /**
     * Updates the driver of a trip
     *
     * @param tripId
     *            the ID of the trip to update
     * @param credentials
     *            the credentials to use for new driver
     * 
     * @return the updated Trip object
     */
    private Trip updateTripDriver(int tripId, Credentials credentials) {
        logout();
        login(credentials);

        Entity<Integer> eSeatCapacity = Entity.entity(4, MediaType.APPLICATION_JSON);

        return target("trip").path("" + tripId).request().put(eSeatCapacity, Trip.class);
    }

    /**
     * Tests the getTrip method of the TripResource class. Creates a sample trip with admin credentials, retrieves the
     * trip using the created trip's ID, and checks if the retrieved trip's ID matches the created trip's ID.
     * 
     * @desc test the getTrip method of the TripResource class
     * 
     * @task ETS-1354
     * 
     * @story ETS-1339
     */
    @Test
    public void getTrip() {
        Trip returnedTrip = createSampleTrip("trip", ADMIN_CREDENTIALS);

        int tripId = returnedTrip.getId();

        Trip updatedTrip = target("trip").path("" + returnedTrip.getId()).request().get(Trip.class);

        assertEquals(tripId, updatedTrip.getId());
    }
}
