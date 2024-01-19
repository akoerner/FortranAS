import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeTraversal {
    // Constants for traversal types
    public static final String PRE_ORDER = "pre-order";
    public static final String IN_ORDER = "in-order";
    public static final String POST_ORDER = "post-order";

    public static void traverse(Map<String, Object> node, String traversalType) {
        List<Map<String, Object>> visited = new ArrayList<>();
        traverseNode(node, traversalType, visited);
    }

    private static void traverseNode(Map<String, Object> node, String traversalType, List<Map<String, Object>> visited) {
        if (node == null || visited.contains(node)) {
            return;
        }

        visited.add(node);

        if (traversalType.equals("pre-order")) {
            System.out.print(node.get("text") + " ");
        }

        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        if (children != null) {
            for (Map<String, Object> child : children) {
                traverseNode(child, traversalType, visited);
            }
        }

        if (traversalType.equals("in-order")) {
            // In-order traversal is not well-defined for arbitrary trees
        }

        if (traversalType.equals("post-order")) {
            // Post-order traversal is not well-defined for arbitrary trees
        }
    }


}

