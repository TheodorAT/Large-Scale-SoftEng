package se.lth.base.server.trip;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Isak Wahlqvist
 */
public class TripDataAccessTest extends BaseDataAccessTest {

    private TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

    @Test
    public void addTrip() {
        Trip data = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, 10200, 0, 5));
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
        Trip trip1 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, 10200, 10400, 5));
        Trip trip2 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 2, 3, 10600, 10800, 3));
        Trip trip3 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 2, 3, 11000, 11200, 2));
        Trip trip4 = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 2, 3, 13000, 14200, 4));

        List<Trip> result = tripDao.availableTrips(1, 2);
        assertEquals(result.get(0).getId(), trip1.getId());
        assertEquals(result.size(), 1);

        result = tripDao.availableTrips(2, 3);
        int sumOfIds = 0;
        for (int i = 0; i < result.size(); i++) {
            assertEquals(result.get(i).getFromLocationId(), 2);
            assertEquals(result.get(i).getToLocationId(), 3);
            sumOfIds += result.get(i).getId();
        }
        assertEquals(sumOfIds, trip2.getId() + trip3.getId() + trip4.getId());
    }
}
