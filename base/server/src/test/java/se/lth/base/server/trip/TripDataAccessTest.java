package se.lth.base.server.trip;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;
import se.lth.base.server.tripPassenger.TripPassengerDataAccess;

import static org.junit.Assert.assertEquals;

import java.util.List;

/**
 * @author Isak Wahlqvist
 * @author Anton Tingelholm
 */
public class TripDataAccessTest extends BaseDataAccessTest {

    private TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());
    private TripPassengerDataAccess tripPassengerDao = new TripPassengerDataAccess(
            Config.instance().getDatabaseDriver());

    @Test
    public void addTrip() {
        Trip data = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, 10200, 10500, 5));
        assertEquals(TEST.getId(), data.getDriverId());
        assertEquals(10200, data.getStartTime());
        assertEquals(5, data.getSeatCapacity());
    }

    /**
     * Test method for retrieving available trips from the database.
     * 
     * Test procedure: 1. Creates sample of trips and add them to the database. 2. Retrieves available trips with
     * parameters given parameters. 3. Validates the ID of the retrieved trip and checks the size of the list. 4.
     * Retrieves available trips with new parameters. 5. Goes through the list and checks the locations for each trip.
     * 6. Checks if the sum of all trip-ids are correct.
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
     * Test method for retrieving all trips belonging to a driver.
     * 
     * Test procedure: 1. Create sample of trips and add them to the database. 2. Retrieve list of trips belonging to
     * driver with Test ID. 3. Check that length of list equals number of added trips. 4. Check that trip IDs match.
     * 
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
     * Test method for retrieving all trips belonging to a passenger.
     * 
     * Test procedure: 1. Create sample of trips and add them to the database. 2. Book trips with test ID. 3. Retrieve
     * list of trips booked with Test ID. 3. Check that length of list equals number of booked trips. 4. Check that trip
     * IDs match.
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

}
