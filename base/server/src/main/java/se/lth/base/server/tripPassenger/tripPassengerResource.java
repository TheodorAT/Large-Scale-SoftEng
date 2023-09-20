package se.lth.base.server.tripPassenger;

import se.lth.base.server.Config;
import se.lth.base.server.user.*;
import se.lth.base.server.trip.*;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("tripPassenger")
public class TripPassengerResource {
    
    private final User user;
    private final TripPassengerDataAccess tripPassengerDao = new TripPassengerDataAccess(Config.instance().getDatabaseDriver());

    public TripPassengerResource(@Context ContainerRequestContext context) {
        this.user = (User) context.getProperty(User.class.getSimpleName());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public TripPassenger createTripPassenger(Trip trip) {
        return tripPassengerDao.bookTrip(trip.getId(), user.getId());
    }

    
}
