
import static org.junit.jupiter.api.Assertions.*;


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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;




public class SimpleTreeTest {

    public static String loadTestJSONTree() {
        String jsonFilePath = "org/fortranas/test/simple_tree.json";
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
    @DisplayName("Tree augment test") 
    void treeAugmentTest() throws IOException {

        try {
            String jsonString = loadTestJSONTree();
            Map<String, Object> treeMap = Tree.fromJSON(jsonString);

            String treeJSONString = Tree.toJSON(treeMap);
            String treeDOTString = DOTConverter.toDOT(treeMap, "text", List.of("text"), true);
            System.out.println("Loaded JSON:\n" + treeJSONString);
            
            System.out.println("DOT String:\n" + treeDOTString);

            Map<String, Object> config = Map.of("subtree_internal_node_serialization_key", "name",
                                                "subtree_terminal_node_serialization_key", "text",
                                                "subtree_terminal_node_multi_character_delimiter", "⎵", 
                                                "subtree_terminal_node_multi_character_replace_text", "STRING",
                                                "subtree_terminal_node_multi_character_replace", new Boolean("true")
                                          );

            Tree.populateSubtreeStrings(treeMap, config);
            treeJSONString = Tree.toJSON(treeMap);
            System.out.println("Augmented JSON:\n" + treeJSONString);
            
           treeDOTString = DOTConverter.toDOT(treeMap, "text", List.of("uuid","text","string"), true);
           System.out.println("Augmented DOT String:\n" + treeDOTString);

            assertNotNull(treeMap);
        } catch (IOException e) {
            System.err.println("An IOException occurred:");
            e.printStackTrace();
        }
    }




}



