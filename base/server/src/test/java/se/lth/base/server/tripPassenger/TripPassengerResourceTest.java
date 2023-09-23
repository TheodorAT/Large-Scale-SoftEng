package se.lth.base.server.tripPassenger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import se.lth.base.server.BaseResourceTest;
import se.lth.base.server.trip.Trip;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class TripPassengerResourceTest extends BaseResourceTest {

    @Before
    public void loginTest() {
        login(TEST_CREDENTIALS);
    }

    @Test
    public void bookTrip() {

        Trip t = new Trip(1, 1, 1, 2, 1, 2, 2);
        Entity<Trip> e = Entity.entity(t, MediaType.APPLICATION_JSON);
        target("trip").request().post(e, Trip.class);

        TripPassenger tripPassenger = target("tripPassenger").request().post(e, TripPassenger.class);

        assertEquals((Integer) TEST.getId(), tripPassenger.getPassengerId());
        assertEquals((Integer) 1, tripPassenger.getTripId());

    }

}
