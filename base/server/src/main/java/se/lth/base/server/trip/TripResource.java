package se.lth.base.server.trip;

import se.lth.base.server.Config;
import se.lth.base.server.user.*;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The TripResource class provides endpoints for managing trips within the
 * system. The class is part of the REST API, which allows for HTTP
 * communication to interface with trip data and functionalities.
 * 
 * @author Isak Wahlqvist
 */
@Path("trip")
public class TripResource {

    private final User user;
    private final TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

    public TripResource(@Context ContainerRequestContext context) {
        this.user = (User) context.getProperty(User.class.getSimpleName());
    }

    /**
     * Creates a new trip for the currently authenticated driver.
     *
     * HTTP Request Type: POST Path: "trip"
     *
     * @param trip The Trip object representing the trip to be created.
     * 
     * @return The newly created Trip object.
     */
    @POST
    @RolesAllowed(Role.Names.DRIVER)
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Trip createTrip(Trip trip) {
        Trip result = tripDao.addTrip(user.getId(), trip);
        return result;
    }

    /**
     * Retrieve a list of trips matching specified parameters.
     * 
     * @param fromLocationId ID of the starting as parameter when searching for the
     *                       trips.
     * 
     * @param toLocationId   ID of the destination as parameter when searching for
     *                       the trips.
     * 
     * @param startTime      Start time as parameter when searching for the trips.
     * 
     * @return A list of Trip objects representing trips that match the parameters.
     *         Returned to the client in JSON format.
     *
     */
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Trip> searchTrips(@QueryParam("fromLocationId") int fromLocationId,
            @QueryParam("toLocationId") int toLocationId, @QueryParam("startTime") long startTime) {
        List<Trip> matchingTrips = tripDao.availableTrips(fromLocationId, toLocationId, startTime);
        return matchingTrips;
    }

    /**
     * Retrieves a list of trips where the current user is a passenger.
     *
     * HTTP Request Type: GET Path: "trip/passenger"
     * 
     * @return A List of Trip objects representing the users passenger trips.
     */
    @Path("passenger")
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Trip> getTripsAsPassenger() {
        List<Trip> result = tripDao.getTripsAsPassenger(user.getId());
        return result;
    }

    /**
     * Retrieves a list of trips where a specific user is a passenger.
     *
     * HTTP Request Type: GET Path: "trip/passenger/{passengerId}"
     *
     * @param passengerId The ID of the passenger whose trips are to be retrieved.
     * 
     * @return A List of Trip objects representing the passenger's trips.
     */
    @Path("passenger/{passengerId}")
    @GET
    @RolesAllowed(Role.Names.ADMIN)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Trip> getTripsAsPassenger(@PathParam("passengerId") int passengerId) {
        List<Trip> result = tripDao.getTripsAsPassenger(passengerId);
        return result;
    }

    /**
     * Retrieves a list of trips where the current user is the driver.
     *
     * HTTP Request Type: GET Path: "trip/driver"
     * 
     * @return A List of Trip objects representing the driver's trips.
     */
    @Path("driver")
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Trip> getTripsFromDriver() {
        List<Trip> result = tripDao.getTripsFromDriver(user.getId());

        return result;
    }

    /**
     * Retrieves a list of trips where a specific user is the driver.
     *
     * HTTP Request Type: GET Path: "trip/driver/{driverId}"
     *
     * @param driverId The ID of the driver whose trips are to be retrieved.
     * 
     * @return A List of Trip objects representing the driver's trips.
     */
    @Path("driver/{driverId}")
    @GET
    @RolesAllowed(Role.Names.ADMIN)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Trip> getTripsFromDriver(@PathParam("driverId") int driverId) {
        List<Trip> result = tripDao.getTripsFromDriver(driverId);
        return result;
    }

    /**
     * Cancels a drivers trip
     * 
     * @param tripId the unique ID for the trip which will be cancelled
     * 
     */
    @Path("driver/{tripId}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public void cancelDriverTrip(@PathParam("tripId") int tripId) {
        if (!tripDao.cancelDriverTrip(this.user.getId(), tripId)) {
            throw new WebApplicationException("Not found trip", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Updates the driver of a trip with the given trip ID. Only Drivers are allowed
     * to access this resource.
     * 
     * @param tripId the ID of the trip to update the driver for
     * 
     * @return the updated Trip object
     * 
     * @throws WebApplicationException if the trip is not found
     */
    @Path("{tripId}")
    @PUT
    @RolesAllowed(Role.Names.DRIVER)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Trip updateDriver(@PathParam("tripId") int tripId) {
        Trip trip = tripDao.updateDriver(this.user.getId(), tripId);
        if (trip == null) {
            throw new WebApplicationException("Trip not found", Response.Status.NOT_FOUND);
        }
        return trip;
    }

    /**
     * This method is used to request a trip by a passenger. It adds the trip to the
     * database and returns the added trip.
     * 
     * @param trip The trip object containing the details of the requested trip.
     * 
     * @return The added trip object.
     */
    @Path("passenger/request")
    @POST
    @RolesAllowed(Role.Names.USER)
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Trip requestTrip(Trip trip) {
        Trip result = tripDao.addTrip(0, trip);
        return result;
    }
}
