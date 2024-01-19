import java.util.List;
import java.util.Map;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class DOTConverter {


    public static String buildString(Map<String, Object> node, List<String> keys, Boolean showKeyLabels) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : keys) {
            Object value = node.get(key);
            if (value != null) {
                if(showKeyLabels){
                    stringBuilder.append("\\l" + key + ":\\n\\l" + value.toString()+ " ");
                } else {
                    stringBuilder.append("\\l" + value.toString() + " ");
                }
                stringBuilder.append("\\n");
            }
        }

        return stringBuilder.toString();
    }

    public static String toDOT(Map<String, Object> root, String idKey, String key, Boolean showKeyLabels) {
        return DOTConverter.toDOT(root, idKey, List.of("uuid", key, "text"), showKeyLabels);  
    }

    public static String toDOT(Map<String, Object> root, String idKey, List<String> keys, Boolean showKeyLabels) {

        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        dot.append("  charset=\"UTF-8\"\n");
        dot.append("  node [shape=square, fontname=\"Arial Unicode MS\"];\n");
        buildDOT(dot, root, idKey, keys, showKeyLabels);

        dot.append("}\n");
        return dot.toString();
    }

    private static void buildDOT(StringBuilder dot, Map<String, Object> node, String idKey, List<String> keys, Boolean showKeyLabels) {
        String nodeId = node.get(idKey).toString();
        String nodeLabel = DOTConverter.escapeDOT(DOTConverter.buildString(node, keys, showKeyLabels));
        dot.append("  \"").append(nodeId).append("\" [label=\"").append(nodeLabel).append("\", fontsize=10];\n");
        if (node.containsKey("children") && node.get("children") instanceof List) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            for (Map<String, Object> child : children) {
                if (child == null)break;
                String childId = child.get(idKey).toString();
                dot.append("  \"").append(nodeId).append("\" -> \"").append(childId).append("\";\n");
                buildDOT(dot, child, idKey, keys, showKeyLabels);
            }
        }
    }
    
    private static String escapeDOT(String text) {
        //System.out.println(text);
        try {
            text = new String(text.getBytes("UTF-8"), "UTF-8"); 
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace(); 
        }
        String[][] escapeSequences = {
            {"\\", "__BACKSLASH__"},
            {"\"", "\\\""},
            {"<", "&lt;"},
            {">", "&gt;"},
            {"-", "\\-"},
            {"{", "\\{"},
            {"}", "\\}"},
            {"[", "\\["},
            {"]", "\\]"},
            {"|", "\\|"},
            {"//", "\\/"},
            {"\n", "\\n"},
            {"\r", "\\r"},
            {"\t", "\\t"},
            {"\\\\", "\\"},
            {"__BACKSLASH__", "\\"}
        };

        StringBuilder stringBuilder = new StringBuilder();

        for (String line : text.split("\n")) {
            stringBuilder.append(line);
        }

        String dotText = stringBuilder.toString();

        for (String[] sequence : escapeSequences) {
            dotText = dotText.replace(sequence[0], sequence[1]);
        }

        //System.out.println(text);
        return dotText;
    }

    public static String getShortHash(String input, int lenght){
        String hash = sha256(input);
        return hash.substring(hash.length() - lenght);
    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);

            md.update(inputBytes);

            byte[] hashBytes = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; 
        }
    }

    public static void main(String[] args) {
        Map<String, Object> tree = Map.of(
            "id", "A",
            "label", "Root Node",
            "children", List.of(
                Map.of("id", "B", "label", "Child 1"),
                Map.of("id", "C", "label", "Child 2"),
                Map.of("id", "D", "label", "Child 3", "children", List.of(
                    Map.of("id", "E", "label", "Child 3.1"),
                    Map.of("id", "F", "label", "Child 3.2")
                ))
            )
        );

        String dotString = toDOT(tree, "id", "label", true);
        System.out.println(dotString);
    }
}

