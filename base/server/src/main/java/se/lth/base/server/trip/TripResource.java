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

    /**
     * Retrieve a list of trips matching specified parameters.
     * 
     * @param fromLocationId
     *            ID of the starting as parameter when searching for the trips.
     * 
     * @param toLocationId
     *            ID of the destination as parameter when searching for the trips.
     * 
     * @return A list of Trip objects representing trips that match the parameters. Returned to the client in JSON
     *         format.
     *
     */
    @GET
    @Path("/search") // Will need to update, (update in test as well)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Trip> searchTrips(
            // possible to add parameters below, such as date/time/seats. Needs to be added in method availableTrips
            // (TripDataAccess) as well. (requires also modify respective test method)
            @QueryParam("fromLocationId") int fromLocationId, @QueryParam("toLocationId") int toLocationId) {
        List<Trip> matchingTrips = tripDao.availableTrips(fromLocationId, toLocationId);
        return matchingTrips;
    }

    @Path("driver")
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Trip> getTripsFromDriver() {
        List<Trip> result = tripDao.getTripsFromDriver(user.getId());
        return result;
    }
}
