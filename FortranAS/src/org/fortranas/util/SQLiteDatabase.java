import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class SQLiteDatabase {

    private Connection connection;
    private String sqliteFile;
    private String databaseURL;

    public SQLiteDatabase(String sqliteFile) throws SQLException {
        this.sqliteFile = sqliteFile;
        this.databaseURL = "jdbc:sqlite:" + sqliteFile;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading SQLite JDBC driver", e);
        }
    }

    public Connection getConnection() throws SQLException{
        if(this.connection == null || this.connection.isClosed()){
            this.connection = DriverManager.getConnection(databaseURL);
        }
        return this.connection;
    }

    public String getSqliteFile() {
        return this.sqliteFile;
    }

    public String getDatabaseURL() {
        return this.databaseURL;
    }

    public void executeSchemas(String schemaDirectory) throws SQLException, IOException {
        File directory = new File(schemaDirectory);

        if (directory.isDirectory()) {
            File[] schemaFiles = directory.listFiles();

            if (schemaFiles != null) {
                for (File schemaFile : schemaFiles) {
                    if (schemaFile.isFile()) {
                        this.executeSchema(schemaFile.getAbsolutePath());
                    }
                }
            } else {
                System.out.println("No files found in the directory: " + schemaDirectory);
            }
        } else {
            System.out.println("Invalid directory: " + schemaDirectory);
        }
    }

    public void executeSchema(String schemaFile) throws SQLException, IOException{
        List<String> schemaQueries = Files.readAllLines(Paths.get(schemaFile));

        System.out.println("Executing schema file: " + new File(schemaFile).getAbsolutePath());
        java.sql.Statement statement = this.connection.createStatement();
        String sql = readSchemaFromFile(schemaFile);
        statement.execute(sql);
    }

    public void connect() throws SQLException {
        this.connection = DriverManager.getConnection(databaseURL);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void insertRecord(String uuid, String stringValue) throws SQLException{
        String sql = "INSERT INTO subtrees (uuid, subtree_string) VALUES (?, ?)";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)){
            statement.setString(1, uuid);
            statement.setString(2, stringValue);
            statement.executeUpdate();
            System.out.println("Record inserted successfully.");
        } catch (SQLException e) {
            System.err.println("Error inserting record: " + e.getMessage());
        }
    }

    public boolean tableExists(String tableName) {
        try (ResultSet resultSet = this.connection.getMetaData().getTables(null, null, tableName, null)){
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println("Error checking if table exists: " + e.getMessage());
            return false;
        }
    }

    private String readSchemaFromFile(String schemaFile) throws IOException {
        return Files.readString(Paths.get(schemaFile));
    }

    public void printDatabaseSummary() {
        try (Connection connection = this.getConnection()) {

            Statement statement = connection.createStatement();
            File file = new File(sqliteFile);
            String fileName = file.getName();
            long fileSize = file.length();
            double fileSizeInMB = fileSize / (1024.0 * 1024.0);

            System.out.println("  Database Summary for: " + fileName);
            System.out.printf("    File Size: %.2f MB%n", fileSizeInMB);

            ResultSet tables = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                if (!(tableName.startsWith("sqlite_") || tableName.startsWith("sqlite_autoindex_"))) {
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName);
                    int recordCount = resultSet.getInt(1);
                    System.out.println("    Table: " + tableName + ", Records: " + recordCount);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void printDatabaseSummary_() {
        try (Connection connection = this.getConnection()){;

            Statement statement = connection.createStatement();
            File file = new File(sqliteFile);
            String fileName = file.getName();
            long fileSize = file.length();

            double fileSizeInMB = fileSize / (1024.0 * 1024.0);

            System.out.println("  Database Summary for: " + fileName);
            System.out.printf("    File Size: %.2f MB%n", fileSizeInMB);


            ResultSet tables = connection.getMetaData().getTables(null, null, null, null);
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                if (!(tableName.startsWith("sqlite_") || tableName.startsWith("sqlite_autoindex_"))) {

                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName);
                    int recordCount = resultSet.getInt(1);
                    System.out.println("    Table: " + tableName + ", Records: " + recordCount);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

