package se.lth.base.server.user;

import se.lth.base.server.Config;
import se.lth.base.server.database.DataAccessException;
import se.lth.base.server.database.ErrorType;
import se.lth.base.server.trip.Trip;
import se.lth.base.server.trip.TripDataAccess;
import se.lth.base.server.user.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Path("user")
public class UserResource {

    public static final String USER_TOKEN = "USER_TOKEN";

    private final ContainerRequestContext context;
    private final User user;
    private final Session session;
    private final UserDataAccess userDao = new UserDataAccess(Config.instance().getDatabaseDriver());
    private final TripDataAccess tripDao = new TripDataAccess(Config.instance().getDatabaseDriver());

    public UserResource(@Context ContainerRequestContext context) {
        this.context = context;
        this.user = (User) context.getProperty(User.class.getSimpleName());
        this.session = (Session) context.getProperty(Session.class.getSimpleName());
    }

    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public User currentUser() {
        return user;
    }

    @Path("login")
    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response login(Credentials credentials, @QueryParam("remember") @DefaultValue("false") boolean rememberMe) {
        Session newSession = userDao.authenticate(credentials);
        int maxAge = rememberMe ? (int) TimeUnit.DAYS.toSeconds(7) : NewCookie.DEFAULT_MAX_AGE;
        return Response.noContent().cookie(newCookie(newSession.getSessionId().toString(), maxAge, null)).build();
    }

    private NewCookie newCookie(String value, int maxAge, Date expiry) {
        return new NewCookie(USER_TOKEN, value, // value
                "/rest", // path
                context.getUriInfo().getBaseUri().getHost(), // host
                NewCookie.DEFAULT_VERSION, // version
                "", // comment
                maxAge, // max-age
                expiry, // expiry
                false, // secure
                true); // http-only

    }

    @Path("logout")
    @POST
    @PermitAll
    public Response logout() {
        userDao.removeSession(session.getSessionId());
        return Response.noContent().cookie(newCookie("", 0, new Date(0L))).build();
    }

    @Path("roles")
    @GET
    @RolesAllowed(Role.Names.ADMIN)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Set<Role> getRoles() {
        return Role.ALL_ROLES;
    }

    /**
     * Creates a new user account using the provided credentials.
     *
     * @param credentials
     *            The user credentials provided as JSON data.
     * 
     * @return The newly created user object.
     * 
     * @throws WebApplicationException
     *             If the provided password is invalid or if there is an issue with creating the user account, a
     *             BAD_REQUEST response is thrown.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public User createUser(Credentials credentials) {
        if (!credentials.hasPassword() || !credentials.validPassword()) {
            // TODO: Update valid password to check for at least one non-letter character
            throw new WebApplicationException(
                    "Password invalid, please make sure your password contains minimnum 8 letters",
                    Response.Status.BAD_REQUEST);
        }
        return userDao.addUser(credentials);
    }

    @Path("all")
    @GET
    @RolesAllowed(Role.Names.ADMIN)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<User> getUsers() {
        return userDao.getUsers();
    }

    /**
     * Returns a user object belonging to the userId
     * 
     * @param userId
     * 
     * @return User object corresponding to the userId
     * 
     * @throws WebApplicationException
     *             if the logged in user is not an admin, or does not have a trip booked where the userId is the driver.
     */
    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public User getUser(@PathParam("id") int userId) {
        if (user.getRole() == Role.ADMIN) {
            return userDao.getUser(userId);
        }

        List<Trip> trips = tripDao.getTripsAsPassenger(user.getId());
        Trip trip;
        for (int i = 0; i < trips.size(); i++) {
            trip = trips.get(i);
            if (userId == trip.getDriverId()) {
                return userDao.getUser(userId);
            }
        }

        throw new WebApplicationException("Mising permission to fetch user/driver info", Response.Status.FORBIDDEN);
    }

    @Path("{id}")
    @RolesAllowed(Role.Names.ADMIN)
    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public User putUser(@PathParam("id") int userId, Credentials credentials) {
        if (credentials.hasPassword() && !credentials.validPassword()) {
            throw new WebApplicationException("Password too short", Response.Status.BAD_REQUEST);
        }
        if (userId == user.getId() && user.getRole().getLevel() > credentials.getRole().getLevel()) {
            throw new WebApplicationException("Cant't demote yourself", Response.Status.BAD_REQUEST);
        }
        return userDao.updateUser(userId, credentials);
    }

    /**
     * Delete a user by their unique identifier.
     *
     * @param userId
     *            The unique identifier of the user to be deleted.
     * 
     * @throws WebApplicationException
     *             If the operation is forbidden (e.g., self-deletion), or the specified user does not exist, a suitable
     *             exception with an appropriate HTTP status code is thrown.
     */
    @Path("{id}")
    @DELETE
    public void deleteUser(@PathParam("id") int userId) {
        if (userId == currentUser().getId() && currentUser().getRole().equals(Role.ADMIN)) {
            throw new WebApplicationException("Don't delete yourself", Response.Status.FORBIDDEN);
        }
        // Keeps users from deleting other users
        if (userId != currentUser().getId() && !currentUser().getRole().equals(Role.ADMIN)) {
            throw new WebApplicationException("You don't have permission to delete someone else",
                    Response.Status.FORBIDDEN);
        }
        if (!userDao.deleteUser(userId)) {
            throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
        }
    }

    /**
     * Changes the role of a user, identified by their user ID.
     * 
     * This endpoint can only be accessed by an administrator or a user changing their own role. The role of a user is
     * updated to the new specified role. A user is not allowed to change their role to ADMIN.
     * 
     * @param userId
     *            the ID of the user whose role is to be changed.
     * @param role
     *            the new role to be assigned to the user.
     * 
     * @throws WebApplicationException
     *             with a FORBIDDEN status if:
     *             <ul>
     *             <li>The user is not an admin and is trying to change another users role</li>
     *             <li>The user is trying to change their role to ADMIN</li>
     *             </ul>
     */
    @Path("{id}/changerole/{role}")
    @PUT
    public User updateUserRole(@PathParam("id") int userId, @PathParam("role") Role role) {
        User currentUser = currentUser();
        if (userId != currentUser.getId() && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new WebApplicationException("You don't have permission to change role", Response.Status.FORBIDDEN);
        }

        if (role == Role.ADMIN && userId == currentUser.getId()) {
            throw new WebApplicationException("You can't change your own role to admin", Response.Status.FORBIDDEN);
        }
        return userDao.updateUserRole(userId, role);
    }

    /**
     * Updates a user's password via a PUT request to the "password" resource.
     *
     * @param credentialsMap
     *            A Map containing old and new credentials.
     * 
     *
     * @return Response object indicating the result of the operation.
     * 
     *
     * @throws WebApplicationException
     *             If there's an error during password update.
     * 
     * 
     */
    @Path("password")
    @PUT
    @RolesAllowed(Role.Names.USER)
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response updatePassword(Map<String, Credentials> credentialsMap) {
        Credentials newCredentials = credentialsMap.get("newCredentials");
        Credentials oldCredentials = credentialsMap.get("oldCredentials");

        // Check if the new password is valid, otherwise throws new exception
        if (!newCredentials.validPassword()) {
            throw new WebApplicationException("New password is invalid", Response.Status.BAD_REQUEST);
        }

        // Get the current user ID
        // int currentUserId = ((Session) context.getProperty(Session.class.getSimpleName())).getUser().getId();
        int currentUserId = user.getId();

        // Update the user password in the database
        User updatedUser = userDao.updateUserPassword(currentUserId, oldCredentials, newCredentials);

        return Response.ok(updatedUser).build();
    }

}
