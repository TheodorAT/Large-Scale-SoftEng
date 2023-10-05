package se.lth.base.server.tripPassenger;

import se.lth.base.server.Config;
import se.lth.base.server.user.*;
import se.lth.base.server.trip.*;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Used to interact with the TripPassengerDataAccess class through HTTP calls.
 * 
 * @author Anton Tingelholm
 */
@Path("tripPassenger")
public class TripPassengerResource {

    private final User user;
    private final TripPassengerDataAccess tripPassengerDao = new TripPassengerDataAccess(
            Config.instance().getDatabaseDriver());

    public TripPassengerResource(@Context ContainerRequestContext context) {
        this.user = (User) context.getProperty(User.class.getSimpleName());
    }

    /**
     * Calls on the bookTrip function from TripPassengerDataAccess using HTTP, which inserts a TripPassenger object in
     * to the database, with current userId as passengerId.
     * 
     * @param tripId
     * 
     * @return TripPassenger This returns the TripPassenger objects.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public TripPassenger createTripPassenger(int tripId) {
        return tripPassengerDao.bookTrip(tripId, user.getId());
    }

    @Path("/availableSeats")
    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public int getAvailableSeats(Trip trip) {
        return tripPassengerDao.getAvailableSeats(trip);
    }
}
