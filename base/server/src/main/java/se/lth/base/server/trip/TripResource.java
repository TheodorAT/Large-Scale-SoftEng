package se.lth.base.server.trip;

import se.lth.base.server.Config;
import se.lth.base.server.user.*;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("trip")
public class TripResource {

    private final User user;
    private final TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

    public TripResource(@Context ContainerRequestContext context) {
        this.user = (User) context.getProperty(User.class.getSimpleName());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Trip createTrip(Trip trip) {
        Trip result = tripDao.addTrip(user.getId(), trip);
        return result;
    }

    @Path("driver/{driverId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Trip> getTripsFromDriver(@PathParam("driverId") int driverId) {
        List<Trip> result = tripDao.getTripsFromDriver(driverId);
        return result;
    }
}
