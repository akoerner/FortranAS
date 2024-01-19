import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.UUID;
import java.util.Map;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FortranASDatabaseTest {

    private static final String DATABASE_FILE = "clone_test_database.sqlite3";
    private static final String DATABASE_TABLE_SCHEMA_DIRECTORY = "sql";

    private FortranASDatabase testFortranASDatabase;

    @BeforeEach
    void setUp() throws SQLException, IOException{

        this.testFortranASDatabase = new FortranASDatabase(Map.of("database_file", DATABASE_FILE, "bleu_score_enabled", new Boolean("true"), "jaro_winkler_similarity_enabled", new Boolean("true"), "sorensen_dice_coefficient_enabled", new Boolean("true"), "cosine_similarity_enabled", new Boolean("true")));
        System.out.println("subtrees");
        assertTrue(this.testFortranASDatabase.getDatabase().tableExists("subtrees"));
        System.out.println("nodes");
        assertTrue(this.testFortranASDatabase.getDatabase().tableExists("nodes"));
        System.out.println("blue_scores");
        assertTrue(this.testFortranASDatabase.getDatabase().tableExists("clones"));
        System.out.println("fortran_files");
        assertTrue(this.testFortranASDatabase.getDatabase().tableExists("fortran_files"));
    }

    @AfterEach
    void tearDown() throws SQLException{
        dropTestDatabase();
    }

    private void dropTestDatabase() throws SQLException {
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



    @Test
    @DisplayName("Insert Records into Database")
    void testInsertRecord() throws SQLException{
        String uuid;
        String stringValue;

        uuid="95d9a756-5abc-487e-874f-f94f10db7a88";
        stringValue="( B )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="f64b24f5-0d39-4005-8522-7806adc4d37a";
        stringValue="( D )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="6ba0c8c9-6228-47ae-a7f1-14ddc4f3e462";
        stringValue="( E )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="9facc9ac-18df-4fbe-a45a-ea14c2cfacaf";
        stringValue="( C ( D ) ( E ) )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="b5040b14-66f0-43ad-9205-fded43c5ceb1";
        stringValue="( F )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="7cea240e-6ef6-443c-92b9-34f556433f19";
        stringValue="( A ( B ) ( C ( D ) ( E ) ) ( F ) )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        
        uuid="d5d9a756-5abc-487e-874f-f94f10db7a88";
        stringValue="( B )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="d64b24f5-0d39-4005-8522-7806adc4d37a";
        stringValue="( D )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="dba0c8c9-6228-47ae-a7f1-14ddc4f3e462";
        stringValue="( E )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="dfacc9ac-18df-4fbe-a45a-ea14c2cfacaf";
        stringValue="( C ( W ) ( E ) )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="d5040b14-66f0-43ad-9205-fded43c5ceb1";
        stringValue="( F )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);
        uuid="dcea240e-6ef6-443c-92b9-34f556433f19";
        stringValue="( A ( B ) ( C ( D ) ( E ) ) ( F ) )";
        this.testFortranASDatabase.insertSubtree(uuid, stringValue);

        //System.out.println("Printing subtrees");
        //this.testFortranASDatabase.printSubtrees();
        this.testFortranASDatabase.getDatabase().printDatabaseSummary();
        //this.testFortranASDatabase.calculateBleuScores();
        //ParallelCloneCalculator parallelCloneCalculator = new ParallelCloneCalculator();
        //parallelCloneCalculator.calculateClones(this.testFortranASDatabase.getSubtrees());

    }
           // }
}

