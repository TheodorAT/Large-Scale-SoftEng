package se.lth.base.server.trip;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;
import se.lth.base.server.tripPassenger.TripPassengerDataAccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * @author Isak Wahlqvist
 * @author Anton Tingelholm
 */
public class TripDataAccessTest extends BaseDataAccessTest {

    private TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());
    private TripPassengerDataAccess tripPassengerDao = new TripPassengerDataAccess(
            Config.instance().getDatabaseDriver());

    /**
     * Test case for adding a trip to the database. It tests if the trip is added successfully by checking if the driver
     * ID, start time and seat capacity are correct.
     * 
     * @desc Test adding a trip to the database.
     * 
     * @task ETS-988
     * 
     * @story ETS-592
     */
    @Test
    public void addTrip() {
        Trip data = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, 10200, 10500, 5));
        assertEquals(TEST.getId(), data.getDriverId());
        assertEquals(10200, data.getStartTime());
        assertEquals(5, data.getSeatCapacity());
    }

    /**
     * Test method for retrieving available trips from the database. It tests if the available trips received from the
     * database match the ones added by checking if locations and ids are correct.
     * 
     * Test procedure: 1. Creates sample of trips and add them to the database. 2. Retrieves available trips with
     * parameters given parameters. 3. Validates the ID of the retrieved trip and checks the size of the list. 4.
     * Retrieves available trips with new parameters. 5. Goes through the list and checks the locations for each trip.
     * 6. Checks if the sum of all trip-ids are correct.
     * 
     * @desc Test retrieving available trips from the database.
     * 
     * @task ETS-895
     * 
     * @story ETS-610
     */
    @Test
    public void availableTrips() {
        Trip trip1 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, 10000, 10400, 5));
        Trip trip2 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, 10200, 10400, 5));
        Trip trip3 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 2, 3, 10600, 10800, 3));
        Trip trip4 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 2, 3, 11000, 11200, 2));
        Trip trip5 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 2, 3, 13000, 14200, 4));

        List<Trip> result = tripDao.availableTrips(1, 2, 10100);
        assertEquals(result.get(0).getId(), trip2.getId());
        assertEquals(result.size(), 1);

        result = tripDao.availableTrips(2, 3, 11000);
        int sumOfIds = 0;
        for (int i = 0; i < result.size(); i++) {
            assertEquals(result.get(i).getFromLocationId(), 2);
            assertEquals(result.get(i).getToLocationId(), 3);
            sumOfIds += result.get(i).getId();
        }
        assertEquals(sumOfIds, trip4.getId() + trip5.getId());
    }

    /**
     * Test cancelling a trip
     * 
     * @desc Test cancelling a trip as a driver
     * 
     * @task ETS-1296
     * 
     * @story ETS-731
     */
    @Test
    public void cancelTrips() {
        Trip trip1 = tripDao.addTrip(TEST.getId(), new Trip(1, 1, 1, 2, 10000, 10400, 5));
        Trip trip2 = tripDao.addTrip(TEST.getId(), new Trip(2, 1, 1, 2, 10000, 10400, 5));
        Trip trip3 = tripDao.addTrip(TEST.getId(), new Trip(3, 1, 1, 2, 10000, 10400, 5));

        List<Trip> resultBefore = tripDao.getAllTrips();
        assertEquals(resultBefore.size(), 3);
        assertTrue(tripDao.cancelDriverTrip(TEST.getId(), 3));
        List<Trip> resultAfter = tripDao.getAllTrips();
        assertEquals(resultAfter.get(2).getStatus(), TripStatus.CANCELLED.getTripStatus());
    }

    /**
     * Test method for getTripsFromDriver(), validates the retrieval of all trips created by a driver.
     * 
     * @desc validates that getTripsFromDriver() returns the correct list of created trips by a driver.
     * 
     * @task ETS-1306
     * 
     * @story ETS-723
     */

    @Test
    public void getTripsFromDriver() {
        Trip trip1 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, 10000, 10400, 5));
        Trip trip2 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, 10200, 10400, 5));

        List<Trip> trips = tripDao.getTripsFromDriver(TEST.getId());
        assertEquals(trips.size(), 2);
        assertEquals(trips.get(0).getId(), trip1.getId());
        assertEquals(trips.get(1).getId(), trip2.getId());
    }

    /**
     * Test method for getTripsAsPassenger(), validates the retrieval of all trips booked by a passenger.
     * 
     * @desc validates that getTripsAsPassenger() returns the correct list of booked trips by a passenger.
     * 
     * @task ETS-1306
     * 
     * @story ETS-723
     */

    @Test
    public void getTripsAsPassenger() {

        Trip trip1 = tripDao.addTrip(DRIVER.getId(), new Trip(-1, -1, 1, 2, 10000, 10400, 5));
        Trip trip2 = tripDao.addTrip(DRIVER.getId(), new Trip(-1, -1, 1, 2, 10200, 10400, 5));

        tripPassengerDao.bookTrip(trip1.getId(), TEST.getId());
        tripPassengerDao.bookTrip(trip2.getId(), TEST.getId());

        List<Trip> trips = tripDao.getTripsAsPassenger(TEST.getId());
        assertEquals(trips.size(), 2);
        assertEquals(trips.get(0).getId(), trip1.getId());
        assertEquals(trips.get(1).getId(), trip2.getId());
    }

    /**
     * Tests the availableTrips method by adding 5 trips with 6 hours between each trip. The returned list should be 4
     * 
     * @desc Test availableTrips method
     * 
     * @task ETS-753
     * 
     * @story ETS-828
     */
    @Test
    public void availableTripsAfter1Day() {
        Trip[] trips = new Trip[5];
        for (int i = 0; i < 5; i++) {
            // Seperate trips start time by 6 hours (21600000 ms)
            trips[i] = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, i * 21600000, i * 21600000 + 10400, 5));
        }

        List<Trip> result = tripDao.availableTrips(1, 2, 10100);

        assertEquals(4, result.size());
    }

    /**
     * Tests the functionality of the requesting a trip by adding a trip with a driverId of 0. The returned trip should
     * have a driverId of 0 and a status of REQUESTED.
     * 
     * @desc Test requesting a trip by adding a trip with a driverId of 0
     * 
     * @task ETS-1345
     * 
     * @story ETS-1339
     */
    @Test
    public void requestTrip() {
        Trip driverlessTrip = tripDao.addTrip(0, new Trip(0, 0, 1, 2, 10000, 10400, 5));

        assertEquals(0, driverlessTrip.getDriverId());
        assertEquals(TripStatus.REQUESTED.getTripStatus(), driverlessTrip.getStatus());
    }

    /**
     * Tests the updateDriver method of the TripDataAccess class. Adds a driverless trip, updates the driver of the trip
     * and checks that the driver has been updated and the trip status has changed accordingly.
     * 
     * @desc Test updating the driver of a trip
     * 
     * @task ETS-1346
     * 
     * @story ETS-1339
     */
    @Test
    public void updateDriver() {
        Trip driverlessTrip = tripDao.addTrip(0, new Trip(-1, -1, 1, 2, 10000, 10400, 5));

        Trip trip = tripDao.updateDriver(TEST.getId(), driverlessTrip.getId(), 4);

        assertEquals(TEST.getId(), trip.getDriverId());
        assertEquals(TripStatus.REQUESTED.getTripStatus(), driverlessTrip.getStatus());

        assertEquals(0, driverlessTrip.getDriverId());
        assertEquals(TripStatus.ACTIVE.getTripStatus(), trip.getStatus());
    }

    /**
     * Tests the getTripsWithoutDriver method of the TripDataAccess class. Adds three driverless trip to the database,
     * and checks if the method retrieved the same and only trips.
     * 
     * @desc Test retrieving trips without driver from database
     * 
     * @task ETS-1347
     * 
     * @story ETS-1339
     */
    @Test
    public void getTripsWithoutDriver() {
        Trip reqeustTrip1 = tripDao.addTrip(0, new Trip(0, 0, 1, 2, 1000, 3000, 4));
        Trip requestTrip2 = tripDao.addTrip(0, new Trip(0, 0, 1, 2, 2000, 3000, 4));
        Trip requestTrip3 = tripDao.addTrip(0, new Trip(0, 0, 1, 2, 1400, 3100, 4));

        List<Trip> result = tripDao.getTripsWithoutDriver();
        assertEquals(3, result.size());
        int sumOfIds = 0;

        for (int i = 0; i < result.size(); i++) {
            sumOfIds += result.get(i).getId();
            assertEquals(1, result.get(i).getFromLocationId());
            assertEquals(2, result.get(i).getToLocationId());
            assertEquals(0, result.get(i).getDriverId());
        }
        assertEquals(sumOfIds, reqeustTrip1.getId() + requestTrip2.getId() + requestTrip3.getId());
    }
}
