package se.lth.base.server.tripPassenger;

import se.lth.base.server.Config;
import se.lth.base.server.trip.TripDataAccess;
import se.lth.base.server.user.*;
import se.lth.base.server.trip.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("tripPassenger")
public class tripPassengerResource {
    
    private final User user;
    private final Trip trip;
    private final TripPassengerDataAccess tripPassengerDao = new TripPassengerDataAccess(Config.instance().getDatabaseDriver());

    public TripPassengerResource(@Context ContainerRequestContext context) {
        this.user = (User) context.getProperty(User.class.getSimpleName());
        this.trip = (Trip) context.getProperty(Trip.class.getSimpleName());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public TripPassenger createTripPassenger(TripPassenger tripPassenger) {
        return tripPassengerDao.bookTrip(trip.getId(), user.getId());
    }
}
