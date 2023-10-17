package se.lth.base.server.database;

import org.h2.tools.RunScript;
import se.lth.base.server.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

/**
 * Contains helpers for creating the database schema. Each time the server starts
 * the @{@link #createSchemaIfNotExists()} method is called.
 *
 * @author Rasmus Ros, rasmus.ros@cs.lth.se
 */
public class CreateSchema {

    private final String driverUrl;

    public CreateSchema(String driverUrl) {
        this.driverUrl = driverUrl;
    }

    public static void main(String[] args) throws Exception {
        CreateSchema cs = new CreateSchema(Config.instance().getDatabaseDriver());
        cs.dropAll();
        cs.createSchema();
    }

    public void dropAll() {
        new DataAccess<>(driverUrl, null).execute("DROP ALL OBJECTS");
    }

    public void createSchema() {
        try (Connection conn = new DataAccess<>(driverUrl, null).getConnection()) {
            runScript(conn);
            // Insert location data into database 
            insertLocations("dataleverans/areas.csv");
        } catch (SQLException e) {
            throw new DataAccessException(e, ErrorType.UNKNOWN);
        }
    }

    /**
     * Inserts location data into the database from a specified CSV file.
     *
     * @param path
     *            The path to the CSV file containing location data.
     */
    public void insertLocations(String path) {
        String insertDataQuery = "INSERT INTO locations (municipality, name, longitude, latitude) VALUES (?, ?, ?, ?);";

        try (Connection conn = new DataAccess<>(driverUrl, null).getConnection()) {
            try (PreparedStatement insertDataStmt = conn.prepareStatement(insertDataQuery);
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(DataAccess.class.getResourceAsStream(path)))) {

                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    int id = Integer.parseInt(st.nextToken());
                    insertDataStmt.setString(1, st.nextToken());
                    insertDataStmt.setString(2, st.nextToken());
                    insertDataStmt.setDouble(3, Double.parseDouble(st.nextToken()));
                    insertDataStmt.setDouble(4, Double.parseDouble(st.nextToken()));

                    insertDataStmt.addBatch();
                }

                insertDataStmt.executeBatch();
            }

        } catch (SQLException | IOException e) {
            throw new DataAccessException(e, ErrorType.UNKNOWN);
        }
    }

    public boolean createSchemaIfNotExists() {
        DataAccess<Long> counter = new DataAccess<>(driverUrl, (rs) -> rs.getLong(1));
        boolean tableExists = counter
                .queryFirst("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'USER_ROLE'") > 0L;
        if (!tableExists) {
            System.out.println("Creating schema");
            createSchema();
            return true;
        }
        return false;
    }

    private static void runScript(Connection conn) throws SQLException {
        RunScript.execute(conn, new InputStreamReader(DataAccess.class.getResourceAsStream("schema.sql")));
    }
}
