package se.lth.base.server.tripPassenger;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;
import se.lth.base.server.trip.Trip;
import se.lth.base.server.trip.TripDataAccess;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;

public class TripPassengerDataAccessTest extends BaseDataAccessTest {

    private TripPassengerDataAccess tripPassengerDao = new TripPassengerDataAccess(
            Config.instance().getDatabaseDriver());
    private TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

    @Test
    public void bookTrip() {
        Trip trip = new Trip(1, 1, 1, 2, 1, 2, 2);
        tripDao.addTrip(1, trip);
        TripPassenger tripPassenger = tripPassengerDao.bookTrip(trip.getId(), TEST.getId());
        assertEquals(TEST.getId(), tripPassenger.getPassengerId());
        assertEquals(1, tripPassenger.getTripId());
    }
}
