package se.lth.base.server.tripPassenger;

import java.sql.ResultSet;
import java.sql.SQLException;

import se.lth.base.server.database.DataAccess;
import se.lth.base.server.database.Mapper;

/**
 * This class is used to interact with the database containing a TripPassenger object in each row, i.e the columns
 * tripId and passengerId
 * 
 * @author Anton Tingelholm
 */

public class TripPassengerDataAccess extends DataAccess<TripPassenger> {

    private static class TripPassengerMapper implements Mapper<TripPassenger> {
        @Override
        public TripPassenger map(ResultSet resultSet) throws SQLException {
            return new TripPassenger(resultSet.getInt("trip_id"), resultSet.getInt("user_id"));
        }
    }

    public TripPassengerDataAccess(String driverUrl) {
        super(driverUrl, new TripPassengerMapper());
    }

    /**
     * Inserts a TripPassenger object in to the database.
     * 
     * @param tripId
     * @param passengerId
     * 
     * @return TripPassenger This returns the TripPassenger object.
     */
    public TripPassenger bookTrip(int tripId, int passengerId) {
        insert("INSERT INTO trip_passengers (trip_id, user_id) VALUES (?, ?)", tripId, passengerId);
        return new TripPassenger(tripId, passengerId);
    }
}
