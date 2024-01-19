import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.UUID;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SQLiteDatabaseTest {

    private static final String DATABASE_FILE = "test_database.sqlite3";
    private static final String DATABASE_TABLE_SCHEMA_DIRECTORY = "sql";
    private static final String DATABASE_TABLE_SCHEMA_FILE = "sql/fortran_as_table_schema.sql";
    private static final String SUBTREE_TABLE_SCHEMA_FILE = "sql/subtree_table_schema.sql";
    private static final String CLONE_TABLE_SCHEMA_FILE = "sql/clone_table_schema.sql";

    private SQLiteDatabase testDatabase;

    @BeforeEach
    void setUp() throws SQLException, IOException{
        this.testDatabase = new SQLiteDatabase(DATABASE_FILE);
        this.testDatabase.connect();
        this.testDatabase.executeSchemas(DATABASE_TABLE_SCHEMA_DIRECTORY);
    }

    @AfterEach
    void tearDown() throws SQLException{
        dropTestDatabase();
    }

    @Test
    @DisplayName("Test Database Creation")
    void testDatabaseCreation() throws SQLException, IOException {

        assertTrue(this.testDatabase.tableExists("subtrees"));
        assertTrue(this.testDatabase.tableExists("clones"));
        assertTrue(this.testDatabase.tableExists("fortran_files"));
    }

    @Test
    @DisplayName("Insert Record into Database")
    void testInsertRecord() throws SQLException{
        String uuid;
        String stringValue;

        uuid="95d9a756-5abc-487e-874f-f94f10db7a88";
        stringValue="( B )";
        this.testDatabase.insertRecord(uuid, stringValue);
        assertTrue(recordExists(uuid, stringValue));
        uuid="f64b24f5-0d39-4005-8522-7806adc4d37a";
        stringValue="( D )";
        this.testDatabase.insertRecord(uuid, stringValue);
        assertTrue(recordExists(uuid, stringValue));
        uuid="6ba0c8c9-6228-47ae-a7f1-14ddc4f3e462";
        stringValue="( E )";
        this.testDatabase.insertRecord(uuid, stringValue);
        assertTrue(recordExists(uuid, stringValue));
        uuid="9facc9ac-18df-4fbe-a45a-ea14c2cfacaf";
        stringValue="( C ( D ) ( E ) )";
        this.testDatabase.insertRecord(uuid, stringValue);
        assertTrue(recordExists(uuid, stringValue));
        uuid="b5040b14-66f0-43ad-9205-fded43c5ceb1";
        stringValue="( F )";
        this.testDatabase.insertRecord(uuid, stringValue);
        uuid="7cea240e-6ef6-443c-92b9-34f556433f19";
        stringValue="( A ( B ) ( C ( D ) ( E ) ) ( F ) )";
        this.testDatabase.insertRecord(uuid, stringValue);
        
        uuid="d5d9a756-5abc-487e-874f-f94f10db7a88";
        stringValue="( B )";
        this.testDatabase.insertRecord(uuid, stringValue);
        uuid="d64b24f5-0d39-4005-8522-7806adc4d37a";
        stringValue="( D )";
        this.testDatabase.insertRecord(uuid, stringValue);
        uuid="dba0c8c9-6228-47ae-a7f1-14ddc4f3e462";
        stringValue="( E )";
        this.testDatabase.insertRecord(uuid, stringValue);
        uuid="dfacc9ac-18df-4fbe-a45a-ea14c2cfacaf";
        stringValue="( C ( W ) ( E ) )";
        this.testDatabase.insertRecord(uuid, stringValue);
        uuid="d5040b14-66f0-43ad-9205-fded43c5ceb1";
        stringValue="( F )";
        this.testDatabase.insertRecord(uuid, stringValue);
        uuid="dcea240e-6ef6-443c-92b9-34f556433f19";
        stringValue="( A ( B ) ( C ( D ) ( E ) ) ( F ) )";
        this.testDatabase.insertRecord(uuid, stringValue);


    }

    private void dropTestDatabase() throws SQLException {
        //assertTrue(false, "Database file: " + new File(DATABASE_FILE).getAbsolutePath());
        try {
            if (new File(DATABASE_FILE).delete()) {
                System.out.println("Test database dropped successfully.");
            } else {
                System.err.println("Error dropping the test database.");
            }
        } catch (Exception e) {
            System.err.println("Error dropping the test database: " + e.getMessage());
        }
    }

    private boolean recordExists(String uuid, String stringValue) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_FILE);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM subtrees WHERE uuid = ? AND subtree_string = ?")) {

            statement.setString(1, uuid);
            statement.setString(2, stringValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if record exists: " + e.getMessage());
            return false;
        }
    }

    private void assertSchemaFileExists(String fileName) {
        File schemaFile = new File(fileName);
        assertTrue(schemaFile.exists(), "Schema file does not exist: " + fileName);
    }
}

