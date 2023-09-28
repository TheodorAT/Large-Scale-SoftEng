package se.lth.base.server.tripPassenger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;
import se.lth.base.server.trip.Trip;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * Tests for TripPassengerResourceTest
 * 
 * @author Anton Tingelholm
 */

public class TripPassengerResourceTest extends BaseResourceTest {

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

    /**
     * Test for the createTripPassenger() function.
     */
    @Test
    public void createTripPassenger() {

        Trip t = new Trip(1, 1, 1, 2, 1, 2, 2);
        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
        target("trip").request().post(e, Trip.class);

        int tripId = t.getId();
        Entity<Integer> ti = Entity.entity(tripId, MediaType.APPLICATION_JSON);
        TripPassenger tripPassenger = target("tripPassenger").request().post(ti, TripPassenger.class);

        assertEquals((Integer) TEST.getId(), tripPassenger.getPassengerId());
        assertEquals(tripId, tripPassenger.getTripId());

    }

}
