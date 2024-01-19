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

public class TreeTraversalTest {

    public static String loadTestJSONTree() {
        String jsonFilePath = "org/fortranas/test/wiki_tree.json";
        InputStream inputStream = TreeTraversalTest.class.getClassLoader().getResourceAsStream(jsonFilePath);

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
    void treeTraversalTest() throws IOException {

        try {
            String jsonString = loadTestJSONTree();
            Map<String, Object> tree = Tree.fromJSON(jsonString);
            System.out.println("Pre-order:");
            TreeTraversal.traverse(tree, TreeTraversal.PRE_ORDER);
            System.out.println();

            System.out.println("In-order:");
            TreeTraversal.traverse(tree, TreeTraversal.IN_ORDER);
            System.out.println();

            System.out.println("Post-order:");
            TreeTraversal.traverse(tree, TreeTraversal.POST_ORDER);
            System.out.println();

        } catch (IOException e) {
            System.err.println("An IOException occurred:");
            e.printStackTrace();
        }
    }
}
