package se.lth.base.server.trip;

import se.lth.base.server.database.DataAccess;
import se.lth.base.server.database.Mapper;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Date;

public class TripDataAccess extends DataAccess<Trip> {

    private static class TripMapper implements Mapper<Trip> {
        @Override
        public Trip map(ResultSet resultSet) throws SQLException {
            return new Trip(resultSet.getInt("trip_id"), resultSet.getInt("driver_id"),
                    resultSet.getInt("from_location_id"), resultSet.getInt("to_location_id"),
                    resultSet.getObject("start_time", Date.class).getTime(),
                    resultSet.getObject("end_time", Date.class).getTime(), resultSet.getInt("seat_capacity"));
        }
    }

    public TripDataAccess(String driverUrl) {
        super(driverUrl, new TripMapper());
    }

    public Trip addTrip(int driverId, Trip trip) {
        String sql = "INSERT INTO trips (driver_id, from_location_id, to_location_id, start_time, end_time, seat_capacity) VALUES (?, ?, ?, ?, ?, ?)";
        //
        // Right now it is just the starttime + 1 hour (3600000 ms)
        long end_time = trip.getStartTime() + 3600000;
        int trip_id = insert(sql, driverId, trip.getFromLocationId(), trip.getToLocationId(),
                new Timestamp(trip.getStartTime()), new Timestamp(end_time), trip.getSeatCapacity());
        return new Trip(trip_id, driverId, trip.getFromLocationId(), trip.getToLocationId(), trip.getStartTime(),
                end_time, trip.getSeatCapacity());
    }

    /**
     * Retrieves a list of available trips based on the parameters.
     * 
     * @param fromLocationId
     *            ID of starting location.
     * 
     * @param toLocationId
     *            ID of destination.
     * 
     * @return A list of Trip objects (available trips matching the parameters).
     * 
     */
    public List<Trip> availableTrips(int fromLocationId, int toLocationId) {
        // possible to add parameters below, such as date/time/seats. Needs to be added in method searchTrips
        // (TripResource) as well. (requires also modify respective test method)
        String sql = "SELECT * FROM trips WHERE from_location_id = ? AND to_location_id = ?";
        return query(sql, fromLocationId, toLocationId);
    }

    public List<Trip> getTripsFromDriver(int driverId) {
        String sql = "SELECT * FROM trips WHERE driver_id = ?";
        return query(sql, driverId);
    }

}