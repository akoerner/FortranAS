import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TreeTest {

    public static String removeLeadingWhitespace(String input) {
        return input.replaceAll("(?m)^\\s+", "");
    }

    public static String removeNewLines(String input) {
        return input.replaceAll("\\n|\\r", "");
    }

    public static boolean jsonStringsAreEqual(String jsonString1, String jsonString2) {
        JsonElement jsonElement1 = JsonParser.parseString(jsonString1);
        JsonElement jsonElement2 = JsonParser.parseString(jsonString2);

        return jsonElement1.equals(jsonElement2);
    }

    public static String loadTestJSONTree() {
        String jsonFilePath = "org/fortranas/test/tree.json";
        InputStream inputStream =  TreeTest.class.getClassLoader().getResourceAsStream(jsonFilePath);

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            StringBuilder stringBuilder = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }
            String jsonString = stringBuilder.toString();
            return jsonString;
        } catch (IOException e) {
            System.err.println("An IOException occurred:");
            e.printStackTrace();
        }

        return "";
    }

    @Test
    @DisplayName("Test Loading tree json file") 
    void JSONLoadTest() throws IOException {

        try {
            String jsonString = loadTestJSONTree();
            Map<String, Object> treeMap = Tree.fromJSON(jsonString);

            String prettyJson = Tree.toJSON(treeMap);
            System.out.println("Loaded JSON:\n" + prettyJson);

            assertNotNull(treeMap);
        } catch (IOException e) {
            System.err.println("An IOException occurred:");
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test Tree.clone function") 
    void cloneTest() throws IOException {
        try {
            String treeJSON = loadTestJSONTree(); 

            Map<String, Object> tree = Tree.fromJSON(treeJSON);
            Map<String, Object> clone = Tree.clone(tree);
            String cloneJSON = Tree.toJSON(clone);

            assertTrue(jsonStringsAreEqual(treeJSON, cloneJSON));
        } catch (IOException e) {
            System.err.println("An IOException occurred:");
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test tree toString") 
    void toStringTest() throws IOException {
        try {
            String treeJSON = loadTestJSONTree(); 

            Map<String, Object> tree = Tree.fromJSON(treeJSON);

            Map<String, Object> config = Map.of("subtree_internal_node_serialization_key", "name",
                                                "subtree_terminal_node_serialization_key", "text",
                                                "subtree_terminal_node_multi_character_delimiter", "⎵", 
                                                "subtree_terminal_node_multi_character_replace_text", "STRING",
                                                "subtree_terminal_node_multi_character_replace", new Boolean("false")
                                          );

            System.out.print(Tree.toString(tree, config));

            config = Map.of("subtree_internal_node_serialization_key", "name",
                                                "subtree_terminal_node_serialization_key", "text",
                                                "subtree_terminal_node_multi_character_delimiter", "⎵", 
                                                "subtree_terminal_node_multi_character_replace_text", "STRING",
                                                "subtree_terminal_node_multi_character_replace", new Boolean("true")
                                          );

            System.out.println();
            System.out.print(Tree.toString(tree, config));



        } catch (IOException e) {
            System.err.println("An IOException occurred:");
            e.printStackTrace();
        }
    }

}

