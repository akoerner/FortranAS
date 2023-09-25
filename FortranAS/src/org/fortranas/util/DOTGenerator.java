package org.fortranas;

import java.io.FileWriter;
import java.io.IOException;

public class DOTGenerator {
    public static void main(String[] args) {
        String filename = "test.dot";
        generateDOTFile(filename);
    }

    public static void generateDOTFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("digraph ASCII_Characters {\n");

            for (int i = 33; i <= 126; i++) {
                char character = (char) i;
                String label = "Escape Code: \n" + String.valueOf(character);

                label = escapeDOT(label);

                writer.write("  " + i + " [label=\"" + label + "\"];\n");
            }

            writer.write("}\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String escapeDOT(String text) {
        String[][] escapeSequences = {
            {"\\", "__BACKSLASH__"},
            {"<", "&lt;"},
            {">", "&gt;"},
            {"-", "\\-"},
            {"{", "\\{"},
            {"}", "\\}"},
            {"[", "\\["},
            {"]", "\\]"},
            {"|", "\\|"},
            {"//", "\\/"},
            {"\"", "\\\""},
            {"\n", "\\n"},
            {"\r", "\\r"},
            {"\t", "\\t"},
            {"\\\\", "\\"},
            {"__BACKSLASH__", "\\"}
        };

        StringBuilder stringBuilder = new StringBuilder();//.append("\\l").append(text);

        //stringBuilder.append(text);
        
        for (String line : text.split("\n")) {
            stringBuilder.append("\\l").append(line).append("\n");
        }

        String dotText = stringBuilder.toString();

        for (String[] sequence : escapeSequences) {
            dotText = dotText.replace(sequence[0], sequence[1]);
        }
        
        //for (String line : text.split("\\n")) {
        //    stringBuilder.append("\\l").append(line).append("\\n");
        //}

        return dotText;
    }


    public static String escapeDOT_(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '{':
                    result.append("\\{");
                    break;
                case '}':
                    result.append("\\}");
                    break;
                case '[':
                    result.append("\\[");
                    break;
                case ']':
                    result.append("\\]");
                    break;
                case '|':
                    result.append("\\|");
                    break;
                case '/':
                    result.append("\\/");
                    break;
                case '"':
                    result.append("\\\"");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\\':
                    result.append("\\");
                default:
                    result.append(c);
                    break;
            }
        }
        return result.toString();
    }
}

