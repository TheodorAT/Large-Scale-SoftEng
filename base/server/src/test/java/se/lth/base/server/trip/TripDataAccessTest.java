package se.lth.base.server.trip;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;
import se.lth.base.server.database.DataAccessException;
import se.lth.base.server.trip.Trip;
import se.lth.base.server.trip.TripDataAccess;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Rasmus Ros, rasmus.ros@cs.lth.se
 */
public class TripDataAccessTest extends BaseDataAccessTest {

    private TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

    @Test
    public void addTrip() {
        Trip data = tripDao.addTrip(TEST.getId(), new Trip(-1, -1, 1, 2, new Timestamp(10200), new Timestamp(10400), 5));
        assertEquals(TEST.getId(), data.getDriverId());
        assertEquals(10200, data.getStartTime().getTime());
        assertEquals(10400, data.getEndTime().getTime());
        assertEquals(5, data.getSeatCapacity());
    }
}
