package se.lth.base.server.location;

import org.junit.Test;
import se.lth.base.server.Config;
import se.lth.base.server.database.BaseDataAccessTest;

import static org.junit.Assert.assertEquals;

import java.util.List;

/**
 * @author Isak Wahlqvist
 */
public class LocationDataAccessTest extends BaseDataAccessTest {

    private LocationDataAccess locationDataAccess = new LocationDataAccess(Config.instance().getDatabaseDriver());

    @Test
    public void getAll() {
        List<Location> locations = locationDataAccess.getAll();
        // assertEquals(3, locations.size());
        // assertEquals("Test Location 1", locations.get(0).getName());
    }

    /**
     * Test calculating the haversine distance between the first 2 locations in the database.
     * 
     * @desc Test the calculateDistance method in LocationDataAccess.
     *
     * @task ETS-1413
     * 
     * @story ETS-592
     */
    @Test
    public void calculateDistance() {
        double distance = locationDataAccess.calculateDistance(1, 2);
        assertEquals(4.54, distance, 0.01);
    }
}
