package se.lth.base.server.trip;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Rasmus Ros, rasmus.ros@cs.lth.se
 */
public class TripDataAccessTest extends BaseDataAccessTest {

    private TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

    @Test
    public void addTrip() {
        Trip data = tripDao.addTrip(TEST.getId(),
                new Trip(-1, -1, 1, 2, new Timestamp(10200), new Timestamp(10400), 5));
        assertEquals(TEST.getId(), data.getDriverId());
        assertEquals(10200, data.getStartTime().getTime());
        assertEquals(10400, data.getEndTime().getTime());
        assertEquals(5, data.getSeatCapacity());
    }

    /**
     * Test method for retrieving available trips from the database.
     * 
     * Test procedure: 
     *  1. Creates sample of trips and add them to the database
     *  2. Retrieves available trips with parameters fromLocationId=1 and toLocationId=2
     *  3. Validates the ID of the retrieved trip and checks the size of the list.
     *  4. Retrieves available trips with parameters fromLocationId=2 and toLocationId=3
     *  5. Goes through the list and checks the locations for each trip.
     *  6. Checks if the sum of all trip-ids are correct.
     */
    @Test
    public void availableTrips(){
        Trip trip1 = tripDao.addTrip(TEST.getId(),
                new Trip(-1, -1, 1, 2, new Timestamp(10200), new Timestamp(10400), 5));  
        Trip trip2 = tripDao.addTrip(TEST.getId(),
                new Trip(-1, -1, 2, 3, new Timestamp(10600), new Timestamp(10800), 3));
        Trip trip3 = tripDao.addTrip(TEST.getId(),
                new Trip(-1, -1, 2, 3, new Timestamp(11000), new Timestamp(11200), 2));
        Trip trip4 = tripDao.addTrip(TEST.getId(),
                new Trip(-1, -1, 2, 3, new Timestamp(13000), new Timestamp(14200), 4));

        List<Trip> result = tripDao.availableTrips(1, 2);
        assertEquals(result.get(0).getId(), trip1.getId());
        assertEquals(result.size(),1);

        result = tripDao.availableTrips(2, 3);
        int sumOfIds = 0;
        for (int i = 0; i < result.size(); i++){
            assertEquals(result.get(i).getFromLocationId(), 2);
            assertEquals(result.get(i).getToLocationId(), 3);
            sumOfIds += result.get(i).getId();
        }
        assertEquals(sumOfIds, trip2.getId() + trip3.getId() + trip4.getId());
    }
}
