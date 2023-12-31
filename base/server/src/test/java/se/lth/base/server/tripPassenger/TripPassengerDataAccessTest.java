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
     * Test method for the bookTrip() function, checks that it succesfully returns the correct tripPassenger object.
     * 
     * @desc Test method for the bookTrip() function, checks that it succesfully returns the correct tripPassenger
     *       object.
     * 
     * @task ETS-1087
     * 
     * @story ETS-593
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

    /**
     * Tests that a passenger can cancel a trip they have booked. Also tests that a passenger cannot cancel a trip they
     * have not booked.
     * 
     * @desc Test the cancelling of a trip as a passenger.
     * 
     * @task ETS-1296
     * 
     * @story ETS-731
     */
    @Test
    public void cancelPassengerTrips() {
        Trip trip = new Trip(1, 1, 1, 2, 1, 2, 2);
        tripDao.addTrip(1, trip);
        TripPassenger tripPassenger = tripPassengerDao.bookTrip(trip.getId(), TEST.getId());

        assertTrue(tripPassengerDao.cancelPassengerTrip(TEST.getId(), trip.getId()));
        assertFalse(tripPassengerDao.cancelPassengerTrip(TEST.getId(), trip.getId()));
    }

    /**
     * Test method for checking that getAvailableSeats() returns the correct amount for a trip.
     * 
     * @desc Test for checking that getAvailableSeats() returns the correct amount of available seats for a trip.
     * 
     * @task ETS-1331
     * 
     * @story ETS-1330
     */
    @Test
    public void getAvailableSeats() {
        Trip trip = new Trip(1, DRIVER.getId(), 1, 2, 1, 2, 2);
        tripDao.addTrip(DRIVER.getId(), trip);

        assertEquals(2, tripPassengerDao.getAvailableSeats(trip.getId()));

        tripPassengerDao.bookTrip(trip.getId(), TEST.getId());
        assertEquals(1, tripPassengerDao.getAvailableSeats(trip.getId()));
    }

}
