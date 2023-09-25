package se.lth.base.server.location;

import se.lth.base.server.Config;
import se.lth.base.server.user.*;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * The LocationResource class provides endpoints for managing locations within
 * the system. The class is part of the REST API, which allows for HTTP
 * communication to interface with trip data and functionalities.
 * 
 * @author Isak Wahlqvist
 */
@Path("location")
public class LocationResource {

    private final User user;
    private final LocationDataAccess locationDao = new LocationDataAccess(Config.instance().getDatabaseDriver());

    public LocationResource(@Context ContainerRequestContext context) {
        this.user = (User) context.getProperty(User.class.getSimpleName());
    }

    /**
     * Retrieves a list of all locations from the database.
     *
     * HTTP Request Type: GET Path: "location/all"
     *
     * @return A List of Location objects representing all locations.
     */
    @Path("all")
    @GET
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Location> getAll() {
        List<Location> result = locationDao.getAll();
        return result;
    }
}
