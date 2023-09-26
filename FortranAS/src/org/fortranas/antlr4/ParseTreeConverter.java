
import java.lang.StringBuilder;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.tool.*;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import java.io.*;

import com.google.gson.*;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class ParseTreeConverter {

    private static final Gson PRETTY_PRINT_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson GSON = new Gson();


private static String escapeDOT(String text) {
    String[][] escapeSequences = {
        {"\\", "\\\\"},
        {"&", "&#38;"},
        {"'", "&#39;"},
        {"/", "&#047;"},
        {"\"", "\\\""},
        {"\n", "&#92;n\\l"},
        {"\\n", "&#92;n"},
        {"\\\\n", "\\n\\l"},
        {"\r", "\\r"},
        {"\t", "\\t"},
        {"\\\\l", "\\l"},
        {"<", "\\<"},
        {">", "\\>"},
        {"\\{", "\\\\{"},
        {"\\}", "\\\\}"}
    };

    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("\\l" + text);
    //for (String line : text.split("\n")) {
    //    stringBuilder.append("\\l").append(line).append("\\n");
   // }

    String dotText = stringBuilder.toString();

    for (String[] sequence : escapeSequences) {
        dotText = dotText.replace(sequence[0], sequence[1]);
    }

    return dotText;
}

    private static String escapeDOT_(String text) {

        StringBuilder stringBuilder = new StringBuilder();

        for (String line : text.split("\n")) {
            stringBuilder.append("\\l").append(line).append("\\n");
        }
        String dotText = stringBuilder.toString();
        dotText.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\\n", "&#92;n")
            .replace("\\\\n", "\\l\\\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replaceAll("<", "\\\\<")
            .replaceAll(">", "\\\\>")
            .replaceAll("\\{", "\\\\{")
            .replaceAll("\\}", "\\\\}");
        System.out.println(dotText);
        return dotText;
    }

    public static String readLines(File fileName, int startLine, int endLine) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder lines = new StringBuilder();
        for (int i = 1; i < startLine; i++) {
            reader.readLine();
        }
        for (int i = startLine; i <= endLine; i++) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            lines.append(line).append("\n");
        }

        reader.close();
        return lines.toString();
    }

    public static String toJSON(ParseTree tree, List<Token> tokens, char[] source, String lexerClassName) {
        return ParseTreeConverter.toJSON(tree, tokens, source, true, lexerClassName);
    }

    public static String toJSON(ParseTree tree, List<Token> tokens, char[] source, boolean prettyPrint, String lexerClassName) {
        return prettyPrint ? PRETTY_PRINT_GSON.toJson(toMap(tree, tokens, source, lexerClassName)) : GSON.toJson(toMap(tree, tokens, source, lexerClassName));
    }

    public static Map<String, Object> toMap(ParseTree tree, List<Token> tokens, char[] source, String lexerClassName) {
        Map<String, Object> map = new LinkedHashMap<>();
        traverse(tree, tokens, map, source, lexerClassName);
        return map;

    }

    public static String getShortHash(String input){
        String hash = sha256(input);
        return hash.substring(hash.length() - 5);
    }

    public static void traverse(ParseTree tree, List<Token> tokens, Map<String, Object> map, char[] source, String lexerClassName) {

        Map<String, Object> data = new LinkedHashMap<>();
        if (tree instanceof TerminalNodeImpl) {
            Token token = ((TerminalNodeImpl) tree).getPayload();
            map.put("name", token.getText());
            map.put("hash", getShortHash(token.toString()));
            map.put("nodeType", "token");
            map.put("channel", token.getChannel());
            map.put("line", token.getLine());
            map.put("charPositionInLine", token.getCharPositionInLine());
            map.put("charStartIndex", token.getStartIndex());
            map.put("charStopIndex", token.getStopIndex());
            map.put("text", token.getText());
            map.put("textSHA256", sha256(token.getText()));
            map.put("index", token.getTokenIndex());
            map.put("tokenType", token.getType());
            map.put("tokenName", TokenLoader.getTokenName(token.getType(), lexerClassName));
            System.out.println("tokenType: " + map.get("tokenType") + " tokenName: " + map.get("tokenName"));
            map.put("string", token.toString());
        } else {
            List<Map<String, Object>> children = new ArrayList<>();
            String name = tree.getClass().getSimpleName().replaceAll("Context$", "");
            int startTokenNumber = tree.getSourceInterval().a;
            int endTokenNumber = tree.getSourceInterval().b;
            int charStartIndex = tokens.get(startTokenNumber).getStartIndex(); 
            int charStopIndex = tokens.get(endTokenNumber).getStopIndex(); 
            String text = getSourceText(charStartIndex, charStopIndex, source);
            String hash = getShortHash(name + "_" + startTokenNumber + "_" + endTokenNumber);
            map.put("rule", name);
            map.put("name", name);
            map.put("nodeType", "rule");
            map.put("hash", hash);
            map.put("startTokenNumber", startTokenNumber);
            map.put("stopTokenNumber", endTokenNumber);
            map.put("startCharIndex", charStartIndex);
            map.put("stopCharIndex", charStopIndex);
            map.put("text", text);
            map.put("textSHA256", sha256(text));
            map.put("children", children);

            for (int i = 0; i < tree.getChildCount(); i++) {
                Map<String, Object> nested = new LinkedHashMap<>();
                children.add(nested);
                traverse(tree.getChild(i), tokens, nested, source, lexerClassName);
            }
        }
    }

    public static String getSourceText(int startIndex, int stopIndex, char[] source) {
        return ParseTreeConverter.interval(startIndex, stopIndex, source);
    }

    public static String interval(int startIndex, int endIndex, char[] charArray) {
        if (startIndex < 0 || startIndex >= charArray.length || endIndex < 0 || endIndex >= charArray.length || startIndex > endIndex) {
            return ""; 
        }

        return new String(charArray, startIndex, endIndex - startIndex + 1);
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

    public static String toDOT(ParseTree tree, List<Token> tokens, char[] source, String lexerClassName, Boolean includeMetadata) {
        return toDOT(toMap(tree, tokens, source, lexerClassName), lexerClassName, includeMetadata);
    }

    public static String toDOT(Map<String, Object> tree, String lexerClassName, Boolean includeMetadata) {
        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph Tree {\n");
        traverse(tree, dotBuilder, lexerClassName, includeMetadata);
        dotBuilder.append("}\n");
        return dotBuilder.toString();
    }

    private static void traverse(Map<String, Object> node, StringBuilder dotBuilder, String lexerClassName, Boolean includeMetadata) {

        if(node == null){
           return;
        }
        ArrayList<Map<String, Object>> children = (ArrayList<Map<String, Object>>)node.get("children");

        String hash = "\"" + (String)node.get("hash") + "\"";
        //String hash = "\"" + (String)node.get("textSHA256") + "\"";
        String name = (String)node.get("name");
        String nodeLabel;
        if(includeMetadata){
            nodeLabel = escapeDOT("name: "        + name + "\n" + 
                                   "hash: "        + (String)node.get("hash") + "\n" + 
                                   "textHash: "    + getShortHash((String)node.get("textSHA256")) +"\n" + 
                                   "nodeType: "    + (String)node.get("nodeType") + "\n" + 
                                   "text: \n\n"    + (String)node.get("text")+ "");
        } else {
            nodeLabel = escapeDOT((String)node.get("text")); 
        }
        dotBuilder.append(hash).append(" [shape=record " + "label=\"" + nodeLabel + "\"]\n");

        if (children == null) return;
        for (Map<String, Object> child : children){
            String childHash;
            if (((String)child.get("nodeType")).equals("token")) {
                childHash = "\"" + (String)child.get("hash") + "\"";
                //childHash = "\"" + (String)child.get("textSHA256") + "\"";
                String childNodeLabel;
                if(includeMetadata){
                    childNodeLabel = escapeDOT("tokenType: "   +         child.get("tokenType") + "\n" + 
                                               "tokenName: "   + (String)child.get("tokenName") + "\n" +
                                               "hash: "        + (String)child.get("hash") + "\n" +
                                               "textHash: "    + getShortHash((String)child.get("textSHA256")) + "\n" +
                                               "nodeType: "    + (String)child.get("nodeType") + "\n" +
                                               "tokenIndex: "  + (int)child.get("index") + "\n" +
                                               "text: \n\n"    + (String)child.get("text") + "");
                } else {
                    childNodeLabel = escapeDOT((String)child.get("text")); 
                }
                dotBuilder.append(childHash).append(" [shape=record " + "label=\"" + childNodeLabel + "\"]\n");
                dotBuilder.append(hash).append(" -> ").append(childHash).append("\n");
            } else {
                childHash = "\"" + (String)child.get("hash") + "\"";
                dotBuilder.append(hash).append(" -> ").append(childHash).append("\n");
                traverse(child, dotBuilder, lexerClassName, includeMetadata);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> mergeNodes(Map<String, Object> node, String mergeOn) {
        if (node == null || !node.containsKey("children")) {
            return node;
        }

        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");

        if (children != null && children.size() == 1) {
            Map<String, Object> child = children.get(0);
            String parentData = (String) node.get(mergeOn);
            String childData = (String) child.get(mergeOn);
            System.out.println(parentData + " == " + childData + ":" + (parentData.equals(childData)));
            if (parentData != null && parentData.trim().equalsIgnoreCase(childData.trim()) || childData.isEmpty()) {
                node.put("children", child.get("children"));

                Object countObj = node.get("count");
                if (countObj instanceof Integer) {
                    node.put("count", (int) countObj + 1);
                } else {
                    node.put("count", 1);
                }

                node.put("text", child.get("text"));

                mergeNodes(node, mergeOn);
            }
        } else {
            if (children != null){
            for (int i = 0; i < children.size(); i++) {
                children.set(i, mergeNodes(children.get(i), mergeOn));
            }
            }
        }

        return node;
    }


}



