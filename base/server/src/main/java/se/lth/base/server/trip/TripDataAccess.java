package se.lth.base.server.trip;

import se.lth.base.server.database.DataAccess;
import se.lth.base.server.database.Mapper;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.eclipse.jetty.server.session.Session;

import java.util.Date;

/**
 * The TripDataAccess class provides data access methods for trip-related data in the database.
 *
 * This class extends the DataAccess class.
 *
 * @author Isak Wahlqvist
 * 
 * @see DataAccess
 */
public class TripDataAccess extends DataAccess<Trip> {

    private static class TripMapper implements Mapper<Trip> {
        @Override
        public Trip map(ResultSet resultSet) throws SQLException {
            return new Trip(resultSet.getInt("trip_id"), resultSet.getInt("driver_id"),
                    resultSet.getInt("from_location_id"), resultSet.getInt("to_location_id"),
                    resultSet.getObject("start_time", Date.class).getTime(),
                    resultSet.getObject("end_time", Date.class).getTime(), resultSet.getInt("seat_capacity"),
                    resultSet.getInt("status_id"));
        }
    }

    public TripDataAccess(String driverUrl) {
        super(driverUrl, new TripMapper());
    }

    /**
     * Adds a new trip to the database for a specific driver. If the driverId is 0, the trip is added as a request with
     * the driverID set to NULL.
     *
     * @param driverId
     *            The ID of the driver adding the trip.
     * @param trip
     *            The Trip object to be added.
     * 
     * @return The newly added Trip object with updated details.
     */
    public Trip addTrip(int driverId, Trip trip) {
        String sql = "INSERT INTO trips (driver_id, from_location_id, to_location_id, start_time, end_time, seat_capacity, status_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Right now it is just the starttime + 1 hour (3600000 ms)
        long end_time = trip.getStartTime() + 3600000;

        Object[] params = { driverId, trip.getFromLocationId(), trip.getToLocationId(),
                new Timestamp(trip.getStartTime()), new Timestamp(end_time), trip.getSeatCapacity(), trip.getStatus() };

        // Check if the driverId is 0, if so, set it to null and set the status to
        // REQUESTED
        if (driverId == 0) {
            params[0] = null;
            params[5] = 0;
            params[6] = TripStatus.REQUESTED.getTripStatus();
        }

        int trip_id = insert(sql, params);

        return new Trip(trip_id, driverId, trip.getFromLocationId(), trip.getToLocationId(), trip.getStartTime(),
                end_time, (int) params[5], (int) params[6]);
    }

    /**
     * Cancels a driver's trip by updating it's status to CANCELLED
     *
     * @param driverId
     *            The unique identifier of the driver.
     * @param tripId
     *            The unique identifier of the trip to be canceled.
     * 
     * @return true if the trip was successfully canceled, false otherwise.
     */
    public boolean cancelDriverTrip(int driverId, int tripId) {
        String sql = "UPDATE trips SET status_id = (SELECT status_id FROM trip_status WHERE trip_status.status = ?) WHERE driver_id = ? AND trip_id = ?";
        return execute(sql, "CANCELLED", driverId, tripId) > 0;
    }

    /**
     * Retrieves a list of available trips based on the parameters. Retrieves a list of available trips based on the
     * parameters. Within a 24 hour period.
     * 
     * @param fromLocationId
     *            ID of starting location.
     * 
     * @param toLocationId
     *            ID of destination.
     * 
     * @param startTime
     *            Start time of trips to search for
     * 
     * @return A list of Trip objects (available trips matching the parameters).
     * 
     */
    public List<Trip> availableTrips(int fromLocationId, int toLocationId, long startTime) {

        // End time is 24 hours after start time
        long endTime = startTime + 86400000;
        String sql = "SELECT * FROM trips WHERE from_location_id = ? AND to_location_id = ? AND start_time >= ? AND start_time <= ? AND status_id = 1";
        return query(sql, fromLocationId, toLocationId, new Timestamp(startTime), new Timestamp(endTime));
    }

    /**
     * Retrieves a list of created trips belonging to the driverId.
     * 
     * @param driverId
     *            ID of driver
     * 
     * @return A list of all trips created by the driver.
     */
    public List<Trip> getTripsFromDriver(int driverId) {
        String sql = "SELECT * FROM trips WHERE driver_id = ?";
        return query(sql, driverId);
    }

    /**
     * Retrieves a list of booked trips belonging to the passengerId.
     * 
     * @param passengerId
     *            ID of passenger
     * 
     * @return A list of all trips booked by the passenger.
     */
    public List<Trip> getTripsAsPassenger(int passengerId) {
        String sql = "SELECT trips.* FROM trips JOIN trip_passengers ON trips.trip_id = trip_passengers.trip_id WHERE trip_passengers.user_id = ?";
        return query(sql, passengerId);
    }

    /**
     * Retrieves a Trip object from the database with trip ID.
     *
     * @param tripId
     *            ID of the trip to retrieve
     * 
     * @return Trip object with the ID
     */
    public Trip getTrip(int tripId) {
        String sql = "SELECT * FROM trips WHERE trip_id = ?";
        return queryFirst(sql, tripId);
    }

    /**
     * Returns a list of all trips in the database.
     *
     * @return a list of all trips
     */
    public List<Trip> getAllTrips() {
        String sql = "SELECT * FROM trips";
        return query(sql);
    }

    /**
     * Updates the driver of a trip with the given driver ID and trip ID. The driver ID must not be null and the trip
     * must not already have a driver assigned.
     * 
     * @param driverId
     *            The ID of the driver to assign to the trip
     * @param tripId
     *            The ID of the trip to update
     * @param seatCapacity
     *            The seat capacity of the trip
     * 
     * @return Updated Trip object
     */
    public Trip updateDriver(int driverId, int tripId, int seatCapacity) {
        String sql = "UPDATE trips SET driver_id = ?, status_id = ?, seat_capacity = ? WHERE trip_id = ? AND driver_id IS NULL";
        execute(sql, driverId, TripStatus.ACTIVE.getTripStatus(), seatCapacity, tripId);
        return getTrip(tripId);
    }

    /**
     * Retrieves a list of requested trips without a driver.
     * 
     * @return A list of trips without driver.
     */
    public List<Trip> getTripsWithoutDriver() {
        return query("SELECT * FROM trips WHERE driver_id IS NULL");
    }

}