import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.regex.*;
import java.util.*;


public class Tree {

    public static List<Map<String, Object>> getNodes(Map<String, Object> node) {
        List<Map<String, Object>> flattenedNodes = new ArrayList<>();
        flattenTree(node, flattenedNodes);
        return flattenedNodes;
    }

    private static void flattenTree(Map<String, Object> node, List<Map<String, Object>> flattenedNodes) {
        flattenedNodes.add(node);
        if (node.containsKey("children")) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            for (Map<String, Object> child : children) {
                flattenTree(child, flattenedNodes);
            }
        }
    }

    public static Map<String, Object> clone(Map<String, Object> tree) {
        if (tree == null) {
            return null;
        }

        Map<String, Object> clonedTree = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Object> entry : tree.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                clonedTree.put(key, clone((Map<String, Object>) value));
            } else if (value instanceof List) {
                clonedTree.put(key, cloneList((List<Object>) value));
            } else {
                clonedTree.put(key, value);
            }
        }

        return clonedTree;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> cloneList(List<Object> list) {
        List<Object> clonedList = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                clonedList.add(clone((Map<String, Object>) item));
            } else if (item instanceof List) {
                clonedList.add(cloneList((List<Object>) item));
            } else {
                clonedList.add(item);
            }
        }
        return clonedList;
    }

    public static final String PRE_ORDER = "pre-order";
    public static final String IN_ORDER = "in-order";
    public static final String POST_ORDER = "post-order";

    public static void traverse(Map<String, Object> node, String traversalType) {
        if (node == null) {
            return;
        }

        if (traversalType.equals(PRE_ORDER)) {
            System.out.println(node.get("data"));
        }

        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        if (children != null) {
            for (Map<String, Object> child : children) {
                traverse(child, traversalType);
            }
        }

        if (traversalType.equals(IN_ORDER)) {
            System.out.println(node.get("data"));
        }

        if (traversalType.equals(POST_ORDER)) {
            System.out.println(node.get("data"));
        }
    }



    public static void prune(Map<String, Object> node, Map<String, Object> pruneOn) {
        if (node == null || node.isEmpty()) {
            return;
        }

        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");

        if (children != null && !children.isEmpty()) {
            for (int i = children.size() - 2; i >= 0; i--) {
                Map<String, Object> child = children.get(i);
                prune(child, pruneOn);
                if (child.isEmpty()) {
                    children.remove(i);
                }
            }
        }

        boolean shouldPrune = true;//children == null || children.isEmpty();
        if (shouldPrune) {
            for (Map.Entry<String, Object> entry : pruneOn.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                //System.out.println("Key: " + key + ", Value: " + value);
                //System.out.println( (String) node.get(key)+ " matches " + (String)value + ":" + Tree.wildcardCompare((String)value, (String) node.get(key)));
                if (Tree.wildcardCompare((String)value, (String) node.get(key))){
                    node.clear();
                }
            }
        }
    }

    public static boolean hasChildren(Map<String, Object> node) {
        if (node.containsKey("children")) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            return children != null && !children.isEmpty();
        }
        return false;
    }

    private static boolean matchesRule(Map<String, Object> map, Map<String, Object> rule) {
        //map.entrySet().forEach(entry -> System.out.println("  Node Key: " + entry.getKey() + " Node Value: " + map.get(entry.getKey()) + "  Rule Value: " + rule.get(entry.getKey())));
        return rule.entrySet().stream()
            .allMatch(entry -> map.containsKey(entry.getKey()) &&
                    Tree.wildcardCompare((String)entry.getValue(), (String)map.get(entry.getKey())));
    }

    public static void clearNode(Map<String, Object> node) {
        Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (!"children".equals(entry.getKey())) {
                iterator.remove();
            }
        }
    }


    public static void populateSubtreeStrings(Map<String, Object> node, Map<String, Object> config) throws IOException {
        if (node == null || !node.containsKey("children")) {
            return;
        }


        @SuppressWarnings("unchecked")
        ArrayList<Map<String, Object>> children = (ArrayList<Map<String, Object>>) node.get("children");
        for (Map<String, Object> child : children) {
            populateSubtreeStrings(child, config);
            child.put("parent_uuid", node.get("uuid"));
        }

        String subtreeString = Tree.toString(node, config);
        if (subtreeString == null || subtreeString.isEmpty() || subtreeString.equals("null")) {
            throw new IOException("Invalid string: " + subtreeString);
        }
        node.put("subtree_string", subtreeString);
        node.put("subtree_size", Tree.calculateSize(subtreeString));
        node.put("subtree_depth", Tree.calculateDepth(subtreeString));
        String subtree_uuid = HashingTools.toUUID(String.valueOf(node.get("subtree_string"))); 
        node.put("subtree_uuid", subtree_uuid);
    }

    public static int calculateSize(String subtreeString) {
        return subtreeString.replaceAll("[()]", "").split("\\s+").length;
    }

    public static int calculateDepth(String subtreeString) {
        int depth = -1;
        int maxDepth = 0;

        for (char c : subtreeString.toCharArray()) {
            if (c == '(') {
                depth++;
                maxDepth = Math.max(maxDepth, depth);
            } else if (c == ')') {
                depth--;
            }
        }

        return maxDepth;
    }


    public static void copyData(Map<String, Object> nodea, Map<String, Object> nodeb) {
        for (Map.Entry<String, Object> entry : nodeb.entrySet()) {
            if (!entry.getKey().equals("children")) {
                //System.out.println("putting: key:" + entry.getKey() + " value: " + entry.getValue());
                nodea.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static boolean wildcardCompare(String wildcard, String str) {
        String[] wildcardTokens = wildcard.split("\\s+");
        String[] strTokens = str.split("\\s+");

        if (wildcardTokens.length != strTokens.length) {
            //System.out.println( str + " matches " + wildcard + ":" + false);
            return false;
        }

        for (int i = 0; i < wildcardTokens.length; i++) {
            String wildcardToken = wildcardTokens[i];
            String strToken = strTokens[i];

            if (!wildcardToken.equals("*") && !wildcardToken.equals(strToken)) {

                //System.out.println( str + " matches " + wildcard + ":" + false);
                return false;
            }
        }

        //System.out.println( str + " matches " + wildcard + ":" + true);
        return true;
    }

    public static Map<String, Object> merge(Map<String, Object> tree, String mergeOnKey) {
        Map<String, Object> clonedTree = Tree.clone(tree);
        Tree.mergeInternal(clonedTree, mergeOnKey);
        return clonedTree; 
    }

    private static void mergeInternal(Map<String, Object> tree, String mergeOnKey) {
        if (tree == null || !tree.containsKey(mergeOnKey) || !tree.containsKey("children")) {
            return;
        }



        List<Map<String, Object>> children = (List<Map<String, Object>>) tree.get("children");
        if (children == null) {
            return; 
        }

        String parentKeyValue = Objects.toString(tree.get(mergeOnKey), null);
        boolean hasChanges = true;

        while (hasChanges) {
            hasChanges = false;

            for (int i = 0; i < children.size(); i++) {
                Map<String, Object> child = children.get(i);
                if (child.containsKey(mergeOnKey)) {
                    String childKeyValue = Objects.toString(child.get(mergeOnKey), null);
                    tree.put("token", Objects.toString(child.get("token"), null));
                    if (parentKeyValue != null && parentKeyValue.equals(childKeyValue)) {
                        List<Map<String, Object>> childChildren = (List<Map<String, Object>>) child.get("children");
                        if (childChildren != null) {
                            children.addAll(childChildren);
                        }

                        children.remove(i);
                        i--;
                        hasChanges = true;
                    }
                }

                mergeInternal(child, mergeOnKey);
            }
        }
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String toJSON(Map<String, Object> tree) {
        String json = gson.toJson(tree);
        return json;
    }

    public static Map<String, Object> fromJSON(String json) throws IOException {
        return gson.fromJson(json, Map.class);
    }

    //generates a subtree string
    public static String toString(Map<String, Object> node, Map<String, Object> config) throws IOException {
        StringBuilder result = new StringBuilder();

        String internalNodeSerializationKey = String.valueOf(config.get("subtree_internal_node_serialization_key"));
        String terminalNodeSerializationKey = String.valueOf(config.get("subtree_terminal_node_serialization_key"));
        String serializationText = null;

        if(Tree.hasChildren(node)){ 
            serializationText = String.valueOf(node.get(internalNodeSerializationKey));
        }
        if (serializationText == null || serializationText.isEmpty() || serializationText.equals("null")){
           serializationText = String.valueOf(node.get(terminalNodeSerializationKey));
        }
 

        if (serializationText.contains(" ")) {
            if (((Boolean)config.get("subtree_terminal_node_multi_character_replace")).booleanValue()){
                serializationText = String.valueOf(config.get("subtree_terminal_node_multi_character_replace_text"));
            } else {
                String delimiter = toUTF8(String.valueOf(config.get("subtree_terminal_node_multi_character_delimiter")));
                System.out.println(delimiter);
                System.out.println("⎵");
                serializationText = serializationText.replace(" ", delimiter);
            }
        }

        node.put("subtree_text", serializationText);
        result.append("( ").append(serializationText);

        if (node.containsKey("children") && node.get("children") instanceof List) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            for (Map<String, Object> child : children) {
                result.append(" ").append(Tree.toString(child, config));
            }
        }

        result.append(" )");
        node.put("subtree_string", result.toString());
        return toUTF8(result.toString());
    }

    public static String toUTF8(String input) {
        try {
            byte[] utf8Bytes = input.getBytes("UTF-8");
            return new String(utf8Bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toUTF8(byte[] inputBytes) {
        try {
            return new String(inputBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Map<String, Object> level4Child = new HashMap<>();
        level4Child.put("text", "D");

        Map<String, Object> level3Child1 = new HashMap<>();
        level3Child1.put("text", "C");
        Map<String, Object> level3Child2 = new HashMap<>();
        level3Child2.put("text", "C");

        List<Map<String, Object>> level3Children = new ArrayList<>();
        level3Children.add(level3Child1);
        level3Children.add(level3Child2);

        Map<String, Object> level3Parent = new HashMap<>();
        level3Parent.put("text", "C");
        level3Parent.put("children", level3Children);

        List<Map<String, Object>> level2Children = new ArrayList<>();
        level2Children.add(level3Parent);
        level2Children.add(level4Child);

        Map<String, Object> level2Parent = new HashMap<>();
        level2Parent.put("text", "B");
        level2Parent.put("children", level2Children);

        Map<String, Object> root = new HashMap<>();
        root.put("text", "A");
        root.put("children", List.of(level2Parent));

        System.out.println("Original Tree Structure:");
        printTree(root, 0);

        Map<String, Object> mergedTree = Tree.merge(root, "text");

        System.out.println("\nMerged Tree Structure:");
        printTree(mergedTree, 0);
    }

    private static void printTree(Map<String, Object> node, int level) {
        String indent = " ".repeat(level * 2);
        System.out.println(indent + node.get("text"));

        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        if (children != null) {
            for (Map<String, Object> child : children) {
                printTree(child, level + 1);
            }
        }
    }
}
