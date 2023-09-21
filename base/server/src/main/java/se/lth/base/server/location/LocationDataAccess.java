package se.lth.base.server.location;

import se.lth.base.server.database.DataAccess;
import se.lth.base.server.database.Mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    public List<Location> getAll() {
        String sql = "SELECT * FROM locations";
        return query(sql);
    }
}
