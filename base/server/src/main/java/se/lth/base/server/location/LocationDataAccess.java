package se.lth.base.server.location;

import se.lth.base.server.database.DataAccess;
import se.lth.base.server.database.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The LocationDataAccess class provides data access methods for
 * location-related data in the database.
 *
 * This class extends the DataAccess class.
 *
 * @author Isak Wahlqvist
 * @see DataAccess
 */
public class LocationDataAccess extends DataAccess<Location> {

    private static class LocationMapper implements Mapper<Location> {
        @Override
        public Location map(ResultSet resultSet) throws SQLException {
            return new Location(resultSet.getInt("location_id"), resultSet.getString("municipality"),
                    resultSet.getString("name"), resultSet.getDouble("latitude"), resultSet.getDouble("longitude"));
        }
    }

    public LocationDataAccess(String driverUrl) {
        super(driverUrl, new LocationMapper());
    }

    /**
     * Retrieves a list of all locations from the database.
     *
     * @return A List of Location objects representing all locations.
     */
    public List<Location> getAll() {
        String sql = "SELECT * FROM locations";
        return query(sql);
    }
}
