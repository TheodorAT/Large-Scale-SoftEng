package se.lth.base.server.tripPassenger;

import se.lth.base.server.trip.Trip;
import se.lth.base.server.trip.TripDataAccess;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import se.lth.base.server.database.DataAccess;
import se.lth.base.server.database.Mapper;

/**
 * This class is used to interact with the database containing a TripPassenger object in each row, i.e the columns
 * tripId and passengerId
 * 
 * @author Anton Tingelholm
 */

public class TripPassengerDataAccess extends DataAccess<TripPassenger> {

    TripDataAccess tripDao;

    private static class TripPassengerMapper implements Mapper<TripPassenger> {
        @Override
        public TripPassenger map(ResultSet resultSet) throws SQLException {
            return new TripPassenger(resultSet.getInt("trip_id"), resultSet.getInt("user_id"));
        }
    }

    public TripPassengerDataAccess(String driverUrl) {
        super(driverUrl, new TripPassengerMapper());
        tripDao = new TripDataAccess(driverUrl);
    }

    /**
     * Inserts a TripPassenger object in to the database.
     * 
     * @param tripId
     * @param passengerId
     * 
     * @return TripPassenger This returns the TripPassenger object.
     * 
     * @throws IllegalArgumentException
     *             If the driver tries to book his own trip.
     */
    public TripPassenger bookTrip(int tripId, int passengerId) {
        Trip trip = tripDao.getTrip(tripId);
        if (trip.getDriverId() == passengerId) {
            throw new IllegalArgumentException("Driver cannot book his own trip");
        }
        insert("INSERT INTO trip_passengers (trip_id, user_id) VALUES (?, ?)", tripId, passengerId);
        return new TripPassenger(tripId, passengerId);
    }

    /**
     * Cancels a passenger's trip by removing them from the trip's passenger list.
     * 
     * @param passengerId
     *            The unique identifier of the passenger.
     * @param tripId
     *            The unique identifier of the trip to be canceled for the passenger
     * 
     * @return true if the passenger's trip was successfully canceled, false otherwise.
     */
    public boolean cancelPassengerTrip(int passengerId, int tripId) {
        String sql = "DELETE FROM trip_passengers WHERE user_id = ? AND trip_id = ?";
        return execute(sql, passengerId, tripId) > 0;
    }

    /**
     * This method returns a list of passengers for a given trip.
     * 
     * @param tripId
     *            The trip to get passengers for.
     * 
     * @return A list of passengers for the given trip.
     */
    public List<TripPassenger> getPassengers(int tripId) {
        String sql = "SELECT * FROM trip_passengers WHERE trip_id = ?";
        return query(sql, tripId);
    }

    public int getAvailableSeats(Trip trip) {
        String sql = "SELECT COUNT(*) FROM trip_passengers WHERE trip_id = ?";
        int bookedSeats = count(sql, trip.getId());
        int availableSeats = trip.getSeatCapacity() - bookedSeats;
        return availableSeats;
    }
}
