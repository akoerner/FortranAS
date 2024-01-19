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

import java.util.UUID;

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

        String dotText = stringBuilder.toString();

        for (String[] sequence : escapeSequences) {
            dotText = dotText.replace(sequence[0], sequence[1]);
        }

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

    public static String toJSON(ParseTree tree, List<Token> tokens, char[] source, String lexerClassName, String fortranSourceFile) throws IOException{
        return ParseTreeConverter.toJSON(tree, tokens, source, true, lexerClassName, fortranSourceFile);
    }

    public static String toJSON(ParseTree tree, List<Token> tokens, char[] source, boolean prettyPrint, String lexerClassName, String fortranSourceFile) throws IOException {
        return prettyPrint ? PRETTY_PRINT_GSON.toJson(toMap(tree, tokens, source, lexerClassName, fortranSourceFile)) : GSON.toJson(toMap(tree, tokens, source, lexerClassName, fortranSourceFile));
    }

    public static Map<String, Object> toMap(ParseTree tree, List<Token> tokens, char[] source, String lexerClassName, String fortranSourceFile) throws IOException {
        Map<String, Object> map = new LinkedHashMap<>();
        traverse(tree, tokens, map, source, lexerClassName, fortranSourceFile);
        return map;

    }

    public static String getShortHash(String input){
        String hash = HashingTools.sha256Sum(input);
        return hash.substring(hash.length() - 5);
    }

    public static void traverse(ParseTree tree, List<Token> tokens, Map<String, Object> map, char[] source, String lexerClassName, String fortranSourceFile) throws IOException {

        Map<String, Object> data = new LinkedHashMap<>();
        if (tree instanceof TerminalNodeImpl) {
            Token token = ((TerminalNodeImpl) tree).getPayload();
            String token_name = TokenLoader.getTokenName(token.getType(), lexerClassName);
            //map.put("name", token.getText());
            //map.put("", getShortHash(token.toString()));
            map.put("nodeType", "token");
            map.put("channel", token.getChannel());
            map.put("startLineIndex", token.getLine());
            map.put("stopLineIndex", token.getLine());
            map.put("line_count", 1);
            map.put("charPositionInLine", token.getCharPositionInLine());
            map.put("charStartIndex", token.getStartIndex());
            map.put("charStopIndex", token.getStopIndex());
            map.put("text", token.getText());
            map.put("subtree_uuid", HashingTools.toUUID(String.valueOf(token.getText())));
            map.put("subtree_size", 1);
            map.put("subtree_depth", 0);
            map.put("textSHA256", HashingTools.sha256Sum(token.getText()));
            map.put("index", token.getTokenIndex());
            map.put("tokenType", token.getType());
            map.put("token_name", token_name);
            //System.out.println("tokenType: " + map.get("tokenType") + " tokenName: " + map.get("tokenName"));
            map.put("token", token.toString());
            map.put("fortranSourceFile", fortranSourceFile);
            map.put("uuid", UUID.randomUUID().toString());
            map.put("parent_uuid", "");
        } else {
            List<Map<String, Object>> children = new ArrayList<>();
            String name = tree.getClass().getSimpleName().replaceAll("Context$", "");
            int startTokenNumber = tree.getSourceInterval().a;
            int endTokenNumber = tree.getSourceInterval().b;
            int charStartIndex = -1;
            int charStopIndex = -1; 
            int startLineIndex = -1;
            int stopLineIndex = -1; 
            int lineCount = -1; 
            if(startTokenNumber >= 0){
                charStartIndex = tokens.get(startTokenNumber).getStartIndex();
            }
            if(endTokenNumber >= 0){
                charStopIndex = tokens.get(endTokenNumber).getStopIndex(); 
            }
            String text = getSourceText(charStartIndex, charStopIndex, source);
            startLineIndex = FileTools.getLineIndex(fortranSourceFile, charStartIndex);
            stopLineIndex = FileTools.getLineIndex(fortranSourceFile, charStopIndex);
            lineCount = stopLineIndex - startLineIndex + 1;
            map.put("rule", name);
            map.put("nodeType", "rule");
            map.put("startTokenNumber", startTokenNumber);
            map.put("stopTokenNumber", endTokenNumber);
            map.put("charStartIndex", charStartIndex);
            map.put("charStopIndex", charStopIndex);
            map.put("startLineIndex", startLineIndex);
            map.put("stopLineIndex", stopLineIndex);
            map.put("line_count", lineCount);
            map.put("text", text);
            map.put("textSHA256", HashingTools.sha256Sum(text));
            map.put("fortranSourceFile", fortranSourceFile);
            map.put("children", children);
            map.put("uuid", UUID.randomUUID().toString());
            map.put("parent_uuid", "");

            for (int i = 0; i < tree.getChildCount(); i++) {
                Map<String, Object> nested = new LinkedHashMap<>();
                children.add(nested);
                traverse(tree.getChild(i), tokens, nested, source, lexerClassName, fortranSourceFile);
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

    public static String toDOT(Map<String, Object> tree, Boolean showKeyLabels) {
        return DOTConverter.toDOT(tree, "uuid", "subtree_text", showKeyLabels);
    }

    public static String toDOT(Map<String, Object> tree, String idKey, List<String> keys, Boolean showKeyLabels) {
        return DOTConverter.toDOT(tree, idKey, keys, showKeyLabels);
    }

    private static void traverse(Map<String, Object> node, StringBuilder dotBuilder, String lexerClassName, Boolean includeMetadata, String fortranSourceFile) {

        if(node == null){
            return;
        }
        ArrayList<Map<String, Object>> children = (ArrayList<Map<String, Object>>)node.get("children");

        //String hash = "\"" + (String)node.get("uuid") + "\"";
        //String hash = "\"" + (String)node.get("textSHA256") + "\"";
        String uuid = "\"" + (String)node.get("uuid") + "\"";
        String name = (String)node.get("name");
        String nodeLabel;
        if(includeMetadata){
            nodeLabel = escapeDOT("name: "        + name + "\n" + 
                    "uuid: "        + (String)node.get("uuid") + "\n" + 
                    "textHash: "    + getShortHash((String)node.get("textSHA256")) +"\n" + 
                    "nodeType: "    + (String)node.get("nodeType") + "\n" + 
                    "text: \n\n"    + (String)node.get("text")+ "");
        } else {
            nodeLabel = escapeDOT((String)node.get("subtree_node_text")); 
        }
        dotBuilder.append(uuid).append(" [shape=record " + "label=\"" + nodeLabel + "\"]\n");



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
            //System.out.println(parentData + " == " + childData + ":" + (parentData.equals(childData)));
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



