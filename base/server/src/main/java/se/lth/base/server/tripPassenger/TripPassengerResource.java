package se.lth.base.server.tripPassenger;

import se.lth.base.server.Config;
import se.lth.base.server.user.*;
import se.lth.base.server.trip.*;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
     * to the database, with current userId as passengerId. If the driver tries to book his own trip, a
     * ForbiddenException is thrown.
     * 
     * @param tripId
     * 
     * @return TripPassenger This returns the TripPassenger objects.
     * 
     * @exception WebApplicationException
     *                A forbidden exception is thrown if the driver tries to book his own trip.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public TripPassenger createTripPassenger(int tripId) {
        return tripPassengerDao.bookTrip(tripId, user.getId());
    }

    /**
     * Cancels a drivers trip, given tripId for the trip
     * 
     * @param tripId
     *            the unique ID for the trip to be deleted
     * 
     */
    @Path("{tripId}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void cancelPassengerTrip(@PathParam("tripId") int tripId) {
        if (!tripPassengerDao.cancelPassengerTrip(this.user.getId(), tripId)) {
            throw new WebApplicationException("Not found trip", Response.Status.NOT_FOUND);
        }
    }

    @Path("/availableSeats")
    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public int getAvailableSeats(Trip trip) {
        return tripPassengerDao.getAvailableSeats(trip);
    }

    /**
     * This method returns a list of passengers for a given trip. Using a GET request.
     * 
     * @param tripId
     *            The trip to get passengers for.
     * 
     * @return A list of passengers for the given trip.
     */
    @Path("{tripId}")
    @GET
    public List<TripPassenger> getPassengers(@PathParam("tripId") int tripId) {
        return tripPassengerDao.getPassengers(tripId);
    }
}
