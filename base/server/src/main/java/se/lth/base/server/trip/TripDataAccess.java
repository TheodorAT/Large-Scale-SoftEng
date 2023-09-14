package se.lth.base.server.trip;

import se.lth.base.server.database.DataAccess;
import se.lth.base.server.database.Mapper;


import java.sql.ResultSet;
import java.sql.SQLException;

public class TripDataAccess extends DataAccess<Trip> {

    private static class TripMapper implements Mapper<Trip> {
        @Override
        public Trip map(ResultSet resultSet) throws SQLException {
            return new Trip(resultSet.getInt("trip_id"), resultSet.getInt("driver_id"),
                    resultSet.getInt("from_location_id"), resultSet.getInt("to_location_id"), resultSet.getTimestamp("start_time"), resultSet.getTimestamp("end_time"), resultSet.getInt("seat_capacity"));
        }
    }

    public TripDataAccess(String driverUrl) {
        super(driverUrl, new TripMapper());
    }

    public Trip addTrip(int driverId, Trip trip){
        String sql = "INSERT INTO trips (driver_id, from_location_id, to_location_id, start_time, end_time, seat_capacity) VALUES (?, ?, ?, ?, ?, ?)";
        int trip_id = insert(sql, driverId, trip.getFromLocationId(), trip.getToLocationId(), trip.getStartTime(), trip.getEndTime(), trip.getSeatCapacity());
        return new Trip(trip_id, driverId, trip.getFromLocationId(), trip.getToLocationId(), trip.getStartTime(), trip.getEndTime(), trip.getSeatCapacity());
    }

}
