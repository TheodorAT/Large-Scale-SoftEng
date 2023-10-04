package se.lth.base.server.trip;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * @author Isak Wahlqvist
 */
public class TripDataAccessTest extends BaseDataAccessTest {

    private TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

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
}
