import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import java.util.UUID;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;

import java.util.concurrent.Executors;
import java.util.concurrent.Executor;


import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;


import org.jctools.queues.MpscArrayQueue;

import java.util.stream.Collectors;

import java.util.concurrent.*;


public class FortranASDatabase {

    private SQLiteDatabase sqliteDatabase;
    private ParallelCloneCalculator parallelCloneCalculator;
    private static String classPath = FortranASDatabase.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    private static String classDirectory = new File(classPath).getParentFile().getPath();
    private static String DATABASE_TABLE_SCHEMA_DIRECTORY = classDirectory + "/sql/";
    private static final String DATABASE_SCHEMA = "sql/fortran_as_table_schema.sql";
    private static final String SUBTREE_TABLE_SCHEMA_FILE = "sql/subtree_table_schema.sql";
    private static final String CLONES_TABLE_SCHEMA_FILE = "sql/bleu_score_table_schema.sql";


    MpscArrayQueue<Map<String, Object>> clones = new MpscArrayQueue<>(2000000);

    private static final int CLONES_BULK_DEQUEUE_NUMBER = 100000;


    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final CountDownLatch stopLatch = new CountDownLatch(1);


    private static final String FORTRAN_FILES_SELECT_SQL= "SELECT COUNT(*) FROM fortran_files WHERE uuid = ?";
    private static final String FORTRAN_FILES_INSERT_SQL = "INSERT OR REPLACE INTO fortran_files (uuid, file_name, sha256sum, line_count, token_count) VALUES (?, ?, ?, ?, ?)";
    private static final String NODES_SELECT_SQL= "SELECT COUNT(*) FROM nodes WHERE uuid = ?";
    private static final String NODES_INSERT_SQL = "INSERT OR REPLACE INTO nodes (uuid, subtree_uuid, parent_uuid, channel, char_position_in_line, char_start_index, char_stop_index, start_line_index, stop_line_index, line_count, node_type, rule, start_token_number, stop_token_number, subtree_depth, subtree_node_text, subtree_size, subtree_string, text, text_sha256, token, token_index, token_name, token_type, fortran_file) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SUBTREES_SELECT_SQL= "SELECT COUNT(*) FROM subtrees WHERE uuid = ?";
    private static final String SUBTREES_INSERT_SQL = "INSERT INTO subtrees (uuid, subtree_string, size, depth) VALUES (?, ?, ?, ?)";
    private static final String CLONES_SELECT_SQL = "SELECT COUNT(*) FROM clones WHERE uuid = ?";
    private static final String CLONES_INSERT_SQL = "INSERT OR REPLACE INTO clones (uuid, reference_uuid, candidate_uuid, bleu_score, cosine_similarity, sorensen_dice_coefficient, jaro_winkler_similarity) VALUES (?, ?, ?, ?, ?, ?, ?)";


    //public final EventBus eventBus = new EventBus();
    public final EventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(2));


    private Map<String, Object> config;
 

    public FortranASDatabase(Map<String, Object> config) throws SQLException, IOException{
        this.eventBus.register(this);
        this.config = config;
        this.sqliteDatabase = new SQLiteDatabase(String.valueOf(config.get("database_file")));
        this.parallelCloneCalculator = new ParallelCloneCalculator(config, eventBus);
        this.sqliteDatabase.connect();
        this.createTables();

    }

    public void disconnect() throws SQLException{
        this.sqliteDatabase.disconnect();
    }

    public void connect() throws SQLException{
        this.sqliteDatabase.connect();
    }

    public void createTables() throws SQLException, IOException{
        if (!this.sqliteDatabase.tableExists("subtrees") && 
                !this.sqliteDatabase.tableExists("nodes") && 
                !this.sqliteDatabase.tableExists("clones") && 
                !this.sqliteDatabase.tableExists("fortran_files")){
            System.out.println("Creating FortranAS database tables: ");
            this.sqliteDatabase.executeSchemas(DATABASE_TABLE_SCHEMA_DIRECTORY);
                } 
    }

    @Subscribe
    public void consumeClone(Map<String, Object> clone) {
            clones.offer(clone);
    }

    private volatile boolean clonesThreadRun = false;
    private volatile boolean clonesThreadWorking = false;

    public void clonesPeriodicWriteStart() {
        if (!clonesThreadRun) {
            clonesThreadRun = true;
            Thread clonesDatabaseInsertWorker = new Thread(() -> {
                while (clonesThreadRun) {
                    clonesThreadWorking = true;
                    insertClones();
                    clonesThreadWorking = false;
                }
            });
            clonesDatabaseInsertWorker.start();
        }
    } 
    public void clonesPeriodicWriteStop() {
        this.clonesThreadRun = false;
        while (clonesThreadWorking) {}
        this.insertClones(clones.size());
    }


    public void insertClones() {
        this.insertClones(clones.size());
    }

    public void clonesPeriodicWriteStart_() {
        scheduler.scheduleAtFixedRate(() -> insertClones(), 0, 1, TimeUnit.MILLISECONDS);
    }

    public void clonesPeriodicWriteStop_() {
        this.clonesThreadRun = false;
        this.insertClones(clones.size());
    }

    public void insertNodes(List<Map<String, Object>> nodes, String fortranFile) {

        try (Connection connection = this.sqliteDatabase.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(NODES_INSERT_SQL);

            connection.setAutoCommit(false);

            for (Map<String, Object> node : nodes) {
                short index = 1;
                preparedStatement.setString(index, node.get("uuid").toString());
                index++;
                preparedStatement.setString(index, node.get("subtree_uuid").toString());
                index++;
                preparedStatement.setString(index, node.get("parent_uuid").toString());
                index++;
                preparedStatement.setObject(index, node.get("channel"));
                index++;
                preparedStatement.setObject(index, node.get("charPositionInLine"));
                index++;
                preparedStatement.setObject(index, node.get("charStartIndex"));
                index++;
                preparedStatement.setObject(index, node.get("charStopIndex"));
                index++;
                preparedStatement.setObject(index, node.get("startLineIndex"));
                index++;
                preparedStatement.setObject(index, node.get("stopLineIndex"));
                index++;
                preparedStatement.setObject(index, node.get("line_count"));
                index++;
                preparedStatement.setObject(index, node.get("nodeType"));
                index++;
                preparedStatement.setObject(index, node.get("rule"));
                index++;
                preparedStatement.setObject(index, node.get("startTokenNumber"));
                index++;
                preparedStatement.setObject(index, node.get("stopTokenNumber"));
                index++;
                preparedStatement.setObject(index, node.get("subtree_depth"));
                index++;
                preparedStatement.setObject(index, node.get("subtree_node_text"));
                index++;
                preparedStatement.setObject(index, node.get("subtree_size"));
                index++;
                preparedStatement.setObject(index, node.get("subtree_string"));
                index++;
                preparedStatement.setObject(index, node.get("text"));
                index++;
                preparedStatement.setObject(index, node.get("textSHA256"));
                index++;
                preparedStatement.setObject(index, node.get("token"));
                index++;
                preparedStatement.setObject(index, node.get("tokenIndex"));
                index++;
                preparedStatement.setObject(index, node.get("token_name"));
                index++;
                preparedStatement.setObject(index, node.get("tokenType"));
                index++;
                preparedStatement.setObject(index, node.get("fortranSourceFile"));
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

            connection.commit();
            this.insertSubtrees(nodes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSubtree(String uuid, String subtreeString) throws SQLException {
        if (uuid == null || uuid.isEmpty()) {
            return;
            //throw new IllegalArgumentException("ERROR: UUID cannot be null or empty");
        }

        if (subtreeString == null || subtreeString.isEmpty()) {
            return;
            //throw new IllegalArgumentException("ERROR: Subtree string cannot be null or empty");
        }

        PreparedStatement statement = this.sqliteDatabase.getConnection().prepareStatement(SUBTREES_INSERT_SQL);
        statement.setString(1, uuid);
        statement.setString(2, subtreeString);
        statement.setInt(3, Tree.calculateSize(subtreeString));
        statement.setInt(4, Tree.calculateDepth(subtreeString));
        statement.executeUpdate();
    }

    public boolean cloneExists(String uuid) {
        boolean exists = false;

        try (Connection connection = this.sqliteDatabase.getConnection();
                PreparedStatement preparedStatementSelect = connection.prepareStatement(CLONES_SELECT_SQL)) {

            preparedStatementSelect.setString(1, uuid);
            ResultSet resultSet = preparedStatementSelect.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            resultSet.close();

            exists = (count != 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;
    }

    public void insertSubtrees(List<Map<String, Object>> nodes) {

        try (Connection connection = this.sqliteDatabase.getConnection()){
            PreparedStatement preparedStatementInsert = connection.prepareStatement(SUBTREES_INSERT_SQL);
            PreparedStatement preparedStatementSelect = connection.prepareStatement(SUBTREES_SELECT_SQL);

            connection.setAutoCommit(false);

            List<Map<String, Object>> uniqueSubtreeNodes = nodes.stream()
                .collect(Collectors.toMap(
                            map -> map.get("subtree_uuid"),
                            map -> map,
                            (existing, replacement) -> existing
                            ))
                .values()
                .stream()
                .collect(Collectors.toList());


            for (Map<String, Object> node : uniqueSubtreeNodes) {
                String subtreeUuid = String.valueOf(node.get("subtree_uuid")); 
                //System.out.println(String.valueOf(node.get("subtree_string"))); 
                String subtreeString = String.valueOf(node.get("subtree_string"));
                int subtreeSize = (int)node.get("subtree_size"); 
                int subtreeDepth = (int)node.get("subtree_depth");

                preparedStatementSelect.setString(1, subtreeUuid);
                ResultSet resultSet = preparedStatementSelect.executeQuery();
                resultSet.next();
                int count = resultSet.getInt(1);
                resultSet.close();

                if (count != 0) {
                    continue; 
                }

                if (subtreeSize < (int) config.get("minimum_subtree_size") ||
                    subtreeSize > (int) config.get("maximum_subtree_size")) {
                    continue;
                }

                if (subtreeDepth < (int) config.get("minimum_subtree_depth") ||
                    subtreeDepth > (int) config.get("maximum_subtree_depth")) {
                    continue;
                }

                preparedStatementInsert.setString(1, subtreeUuid);
                preparedStatementInsert.setString(2, subtreeString);
                preparedStatementInsert.setInt(3, subtreeSize);
                preparedStatementInsert.setInt(4, subtreeDepth);
                preparedStatementInsert.addBatch();

                subtreeUuid = null;
                subtreeString = null;
            }

            uniqueSubtreeNodes = null;
            preparedStatementInsert.executeBatch();
            preparedStatementInsert = null;
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertClones(int cloneCount) {
        if (cloneCount == 0) {
            return;
        }

        try (Connection connection = this.sqliteDatabase.getConnection()) {
            //PreparedStatement preparedStatementSelect = connection.prepareStatement(CLONES_SELECT_SQL);
            PreparedStatement preparedStatementInsert = connection.prepareStatement(CLONES_INSERT_SQL);

            connection.setAutoCommit(false);
            //System.out.println("polling: " + cloneCount);
            int writeCount = 0;
            int batchSize = 0;
            while (writeCount < cloneCount) {
                //Map<String, Object> entry = parallelCloneCalculator.getCloneCalculator().getClones().poll();
                Map<String, Object> entry = clones.poll();
                if (entry == null) {
                    break;
                }
                String uuid = String.valueOf(entry.get("uuid"));
                String referenceUuid = String.valueOf(entry.get("reference_uuid"));
                String candidateUuid = String.valueOf(entry.get("candidate_uuid"));
                Double bleuScore = (Double) entry.get("bleu_score");
                Double cosineSimilarity = (Double) entry.get("cosine_similarity");
                Double sorensen_dice_coefficient = (Double) entry.get("sorensen_dice_coefficient");
                Double jaro_winkler_similarity = (Double) entry.get("jaro_winkler_similarity");

                entry = null;

                if (referenceUuid.equals(candidateUuid)) {
                    continue;
                }

                preparedStatementInsert.setString(1, uuid);
                preparedStatementInsert.setString(2, referenceUuid);
                preparedStatementInsert.setString(3, candidateUuid);
                preparedStatementInsert.setDouble(4, bleuScore);
                preparedStatementInsert.setDouble(5, cosineSimilarity);
                preparedStatementInsert.setDouble(6, sorensen_dice_coefficient);
                preparedStatementInsert.setDouble(7, jaro_winkler_similarity);
                preparedStatementInsert.addBatch();
                uuid = null;
                referenceUuid = null;
                candidateUuid = null;
                if(batchSize++ > 100){
                    preparedStatementInsert.executeBatch();
                    batchSize = 0;
                }

                writeCount++;
            }

            //System.out.println("polled: " + writeCount);
            preparedStatementInsert.executeBatch();
            connection.commit();
            //preparedStatementSelect = null;
            preparedStatementInsert = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertClone(String referenceUuid, String candidateUuid, double bleuScore) throws SQLException{
        try (PreparedStatement statement = this.sqliteDatabase.getConnection().prepareStatement(CLONES_INSERT_SQL)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, referenceUuid);
            statement.setString(3, candidateUuid);
            statement.setDouble(4, bleuScore);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertFile(String fileName, int lineCount, int tokenCount) {
        try (Connection connection = this.sqliteDatabase.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(FORTRAN_FILES_INSERT_SQL);

            String sha256sum = HashingTools.fileSha256sum(fileName);
            String uuid = HashingTools.sha256SumToUUID(sha256sum);

            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, fileName);
            preparedStatement.setString(3, sha256sum);
            preparedStatement.setInt(4, lineCount);
            preparedStatement.setInt(5, tokenCount);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void calculateClones() throws SQLException{
        List<Map<String, Object>> subtrees = getSubtrees();
        long estimatedWork = (((long)subtrees.size() * (long)subtrees.size()));

        System.out.println("      Subtree count: " + subtrees.size());
        if (!(subtrees.size() > 0)){
  
            System.out.println("      No subtrees, nothing to calculate.");
            System.out.println();
            return;
        }
        System.out.println("      Estimated clone calculations: " + estimatedWork);
        System.out.println("      Starting clone calculation periodic database write thread");
        this.clonesPeriodicWriteStart();
        System.out.println("      Starting clone calculation threads");
        this.parallelCloneCalculator.calculateClones(subtrees);
        System.out.println("      Clone calculations done, flushing remaining clone results (" + clones.size() + ") to database...");
        this.clonesPeriodicWriteStop();
        System.out.println("      Flushing complete.");
        System.out.println();

    }

    public List<Map<String, Object>> getSubtrees() {
        List<Map<String, Object>> subtreesList = new ArrayList<>();

        String selectSubtreesQuery = "SELECT uuid, subtree_string, size, depth FROM subtrees";

        try (PreparedStatement subtreesStatement = this.sqliteDatabase.getConnection().prepareStatement(selectSubtreesQuery);
                ResultSet subtreesResultSet = subtreesStatement.executeQuery()) {

            while (subtreesResultSet.next()) {
                Map<String, Object> subtreeMap = new HashMap<>();
                subtreeMap.put("uuid", subtreesResultSet.getString("uuid"));
                subtreeMap.put("subtree_string", subtreesResultSet.getString("subtree_string"));
                subtreeMap.put("subtree_size", subtreesResultSet.getInt("size"));
                subtreeMap.put("subtree_depth", subtreesResultSet.getInt("depth"));
                subtreesList.add(subtreeMap);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subtreesList;
    }

    public SQLiteDatabase getDatabase() {
        return this.sqliteDatabase;
    }

    public void printSubtrees() {
        List<Map<String, Object>> subtreesList = getSubtrees();

        for (Map<String, Object> subtreeMap : subtreesList) {
            String uuid = String.valueOf(subtreeMap.get("uuid"));
            String subtree_string = String.valueOf(subtreeMap.get("subtree_string"));

            System.out.println("UUID: " + uuid + ", Subtree string: " + subtree_string);
        }
    }

}
