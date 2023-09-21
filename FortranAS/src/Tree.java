import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

public class Tree {

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
