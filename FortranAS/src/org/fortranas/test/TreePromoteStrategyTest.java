import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TreePromoteStrategyTest {

    public static String loadTestJSONTree() {
        String jsonFilePath = "org/fortranas/test/273.json";
        InputStream inputStream =  TreePromoteStrategyTest.class.getClassLoader().getResourceAsStream(jsonFilePath);

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
    @DisplayName("Tree node promotion test") 
    void treePromoteStrategyTest() throws IOException {
        String jsonFilePath = "org/fortranas/test/273.json";
            String jsonString = loadTestJSONTree();
            Map<String, Object> tree = Tree.fromJSON(jsonString);

        String[][] parentRules = {
            {"name", "AddOperand"},
            {"name", "OutputItemList"}
        };

        String[][] childRules = {
            {"tokenName", "PLUS"},
            {"tokenName", "STAR"}
            // Add more child rules with possible duplicate keys if needed
        };

            TreePromoteStrategy.promote(tree, parentRules, childRules);

            //String dotString = DOTConverter.toDOT(tree, "uuid", "sutbree_text", true);
            //FileTools.writeFile("273.dot", dotString);
 
    }
}
