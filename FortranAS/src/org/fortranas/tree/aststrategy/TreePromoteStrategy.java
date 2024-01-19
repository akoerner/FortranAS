import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Iterator;


public class TreePromoteStrategy {

    public static void promote(Map<String, Object> node, String[][] parentRules, String[][] childRules) {
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");

        if (children != null) {
            Iterator<Map<String, Object>> iterator = children.iterator();

            while (iterator.hasNext()) {
                Map<String, Object> child = iterator.next();

                if (meetsPromotionCriteria(node, child, parentRules, childRules)) {
                    // Erase all keys in the parent except "children"
                    node.keySet().retainAll(Collections.singleton("children"));

                    // Copy key value pairs from the child matching promotion criteria to the parent
                    for (Map.Entry<String, Object> entry : child.entrySet()) {
                        node.put(entry.getKey(), entry.getValue());
                    }

                    // Remove the matching child
                    iterator.remove();
                }

                // Recursively check and promote in the child's subtree
                promote(child, parentRules, childRules);
            }
        }
    }

    private static boolean meetsPromotionCriteria(
        Map<String, Object> parentNode,
        Map<String, Object> childNode,
        String[][] parentRules,
        String[][] childRules
    ) {
        boolean parentRuleMatch = false;
        String value;
        for (String[] parentRule : parentRules) {
            String parentKey = parentRule[0];
            String parentValue = parentRule[1];
            value = (String)parentNode.get(parentKey); 
            if (value != null && value.equals(parentValue)) {
                parentRuleMatch = true;
            }
        }

        for (String[] childRule : childRules) {
                String childKey = childRule[0];
                String childValue = childRule[1];
                value = (String)childNode.get(childKey); 
            if (value != null && value.equals(childValue)) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) childNode.get("children");
                return children == null || children.isEmpty();
            }
        }
        return false;
    }





}

