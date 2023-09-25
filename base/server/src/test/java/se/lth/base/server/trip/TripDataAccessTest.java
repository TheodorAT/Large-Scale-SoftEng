package se.lth.base.server.trip;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;

import static org.junit.Assert.assertEquals;

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
}
