package se.lth.base.server.tripPassenger;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;
import se.lth.base.server.trip.Trip;
import se.lth.base.server.trip.TripDataAccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for TripPassengerDataAccess
 * 
 * @author Anton Tingelholm
 */

public class TripPassengerDataAccessTest extends BaseDataAccessTest {

    private TripPassengerDataAccess tripPassengerDao = new TripPassengerDataAccess(
            Config.instance().getDatabaseDriver());
    private TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

    /**
     * Test method for the bookTrip() function.
     */
    @Test
    public void bookTrip() {
        Trip trip = new Trip(1, 1, 1, 2, 1, 2, 2);
        tripDao.addTrip(1, trip);
        TripPassenger tripPassenger = tripPassengerDao.bookTrip(trip.getId(), TEST.getId());
        assertEquals(TEST.getId(), tripPassenger.getPassengerId());
        assertEquals(1, tripPassenger.getTripId());
    }

    /**
     * Test method for booking a trip as the driver. It should throw an IllegalArgumentException.
     * 
     * @desc Test booking a trip as the driver. Expected to throw an IllegalArgumentException.
     * 
     * @task ETS-1353
     * 
     * @story ETS-1339
     */
    @Test(expected = IllegalArgumentException.class)
    public void bookTripAsDriver() {
        Trip trip = new Trip(1, 1, 1, 2, 1, 2, 2);
        tripDao.addTrip(TEST.getId(), trip);
        tripPassengerDao.bookTrip(trip.getId(), TEST.getId());
    }

    @Test
    public void cancelPassengerTrips() {
        Trip trip = new Trip(1, 1, 1, 2, 1, 2, 2);
        tripDao.addTrip(1, trip);
        TripPassenger tripPassenger = tripPassengerDao.bookTrip(trip.getId(), TEST.getId());

        assertTrue(tripPassengerDao.cancelPassengerTrip(TEST.getId(), trip.getId()));
        assertFalse(tripPassengerDao.cancelPassengerTrip(TEST.getId(), trip.getId()));
    }

    @Test
    public void getAvailableSeats() {
        Trip trip = new Trip(1, DRIVER.getId(), 1, 2, 1, 2, 2);
        tripDao.addTrip(DRIVER.getId(), trip);

        assertEquals(2, tripPassengerDao.getAvailableSeats(trip));

        tripPassengerDao.bookTrip(trip.getId(), TEST.getId());
        assertEquals(1, tripPassengerDao.getAvailableSeats(trip));
    }

}
