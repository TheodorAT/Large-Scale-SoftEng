package se.lth.base.server.tripPassenger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;
import se.lth.base.server.Config;
import se.lth.base.server.trip.Trip;

import org.junit.Before;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static junit.framework.TestCase.assertEquals;

public class TripPassengerResourceTest extends BaseResourceTest {

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

    @Test
    public void bookTrip() {

        /*
         * Trip trip = new Trip(1, 1, 1, 2, new Timestamp(10), new Timestamp(20), 2);
         * 
         * Entity<Trip> e = Entity.entity(trip, MediaType.APPLICATION_JSON);
         * 
         * TripPassenger t = target("tripPassenger").request().post(e, TripPassenger.class);
         * 
         * assertEquals((Integer) TEST.getId(), t.getPassengerId()); assertEquals((Integer) 1, t.getTripId());
         * 
         */

    }

}
