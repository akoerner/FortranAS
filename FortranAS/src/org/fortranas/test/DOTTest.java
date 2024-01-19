import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;

public class DOTTest {

    @Test
    public void testToDOT() {
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

        String dotString = DOTConverter.toDOT(tree, "id", "label", true);
        System.out.println(dotString);
        assertTrue(true);
    }

    private static char currentChar = 'A';
    private static int currentIndex = 0;

    @Test
    public void characterDOTTest() {
        //char startChar = 33; 
        //char endChar = 126;
        //char endChar = 124;
        
        char startChar = '!';
        DOTTest.currentChar = startChar;
        char endChar = '~';

        int startIndex = 0;
        int endIndex = 94;
        DOTTest.currentIndex = startIndex;

        //FileTools.writeFile("int_bintary_tree.dot", DOTConverter.toDOT(generateBinaryTree(startIndex, endIndex), "label", "label", true));
       // Map<String, Object> root = generateBinaryTreeChars(startChar, endChar);

        //String dotString = DOTConverter.toDOT(root, "label", "label");
        //System.out.println(dotString);
        //FileTools.writeFile("character.dot", dotString);
        //assertFalse(dotString.isEmpty());
    }

    public static Map<String, Object> generateBinaryTree(int startIndex, int endIndex) {
        if (DOTTest.currentIndex > endIndex) {
            return null;
        }
        if (startIndex > endIndex) {
            return null;
        }

        Map<String, Object> node = new HashMap<>();
        node.put("id", DOTTest.currentIndex);
        node.put("label", DOTTest.currentIndex);
        DOTTest.currentIndex++;

        List<Map<String, Object>> children = new ArrayList<>();
        node.put("children", children);

        if (startIndex != endIndex) {
            Map<String, Object> leftChild = generateBinaryTree(startIndex,((startIndex + endIndex) / 2 - 1));
            if (leftChild != null) {
                children.add(leftChild);
            }

            Map<String, Object> rightChild = generateBinaryTree(((startIndex + endIndex) / 2 + 1), endIndex);
            if (rightChild != null) {
                children.add(rightChild);
            }
        }

        return node;

    }

    public static Map<String, Object> generateBinaryTreeChars(char startChar, char endChar) {
        if (startChar > endChar) {
            return null;
        }

        Map<String, Object> node = new HashMap<>();
        node.put("id", String.valueOf(currentChar));
        node.put("label", String.valueOf(currentChar));
        currentChar++;

        List<Map<String, Object>> children = new ArrayList<>();
        node.put("children", children);

        if (startChar != endChar) {
            Map<String, Object> leftChild = generateBinaryTree(startChar, (char) ((startChar + endChar) / 2 - 1));
            if (leftChild != null) {
                children.add(leftChild);
            }

            Map<String, Object> rightChild = generateBinaryTree((char) ((startChar + endChar) / 2 + 1), endChar);
            if (rightChild != null) {
                children.add(rightChild);
            }
        }

        return node;
    }


}
