
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import java.io.*;

import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;



import java.io.*;
import java.nio.file.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileTools {


    public static int fileCount(String directory, String[] extensions) {
        for (int i = 0; i < extensions.length; i++) {
            extensions[i] = extensions[i].toLowerCase();
        }
        return countFilesRecursive(new File(directory), extensions);
    }

    private static int countFilesRecursive(File directory, String[] extensions) {
        int count = 0;

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        count += countFilesRecursive(file, extensions);
                    } else {
                        if (hasMatchingExtension(file.getName(), extensions)) {
                            System.out.println("counting file: " + file.getName());
                            count++;
                            System.out.println("  count: " + count);

                        }
                    }
                }
            }
        }

        return count;
    }

    public static List<String> findFiles(String directory, String[] extensions) {
        List<String> files = new ArrayList<>();
        findFilesRecursive(new File(directory), extensions, files);
        return new ArrayList<>(new TreeSet<>(files));
    }

    private static void findFilesRecursive(File directory, String[] extensions, List<String> resultList) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    findFilesRecursive(file, extensions, resultList);
                }
            }
        } else {
            String fileName = directory.getName();
            for (String extension : extensions) {
                if (fileName.toLowerCase().endsWith(extension.toLowerCase())) {
                    resultList.add(directory.getAbsolutePath());
                    break;
                }
            }
        }
    }

    public static void log(String file, String message) {
        try {
            FileTools.mkdirp(file);
            File logFile = new File(file);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean hasMatchingExtension(String fileName, String[] extensions) {
        String lowercaseFileName = fileName.toLowerCase();

        for (String extension : extensions) {
            if (lowercaseFileName.endsWith(extension.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public static void writeFile(File file, String content) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String fileName, String content) {
        File file = new File(fileName);
        FileTools.writeFile(file, content);
    }

    public static void dotToSVG(String dotFileName) throws IOException{
        String command = "dot -T svg " + dotFileName + " -o " + dotFileName + ".svg";
        Runtime.getRuntime().exec(command);
    }

    public static void dotToPNG(String dotFileName) throws IOException{
        String command = "dot -T png " + dotFileName + " -o " + dotFileName + ".png";
        Runtime.getRuntime().exec(command);
    }

    public static void svgToPNG(String svgFileName) throws IOException{
        String pngFileName = svgFileName.replace(".svg", ".png"); 
        String command = "convert -density 300 " + svgFileName + " " + pngFileName;
        exec(command);
    }

    public static String exec(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (InputStream inputStream = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException("ERROR: Command: '" + command + "' exited with non-zero status code: " + exitCode);
            }
        } catch (Exception e) {
            throw new IOException("ERROR: Command: '" + command + "' execution failed. \n" + output.toString());
        }

        return output.toString();
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

    public static List<File> getFilesMatchingRegex(String directoryPath, String regex) {
        List<File> matchingFiles = new ArrayList<>();
        Set<String> uniqueFilenames = new HashSet<>();

        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Invalid directory path: " + directoryPath);
            return matchingFiles;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            Pattern pattern = Pattern.compile(regex);

            for (File file : files) {
                if (file.isFile()) {
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.matches() && uniqueFilenames.add(file.getName())) {
                        matchingFiles.add(file);
                    }
                }
            }
        }

        return matchingFiles;
    }

    public static void mkdirp(String path) throws IOException {
        File file = new File(path);
        String parentPath = file.getParent();

        if (parentPath != null) {
            File parentDir = new File(parentPath);
            if (!parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    //System.out.println("Directories created successfully: " + parentPath);
                } else {
                    //System.err.println("Failed to create directories: " + parentPath);
                    throw new IOException("ERROR: Failed to create directory tree: " + parentPath);
                }
            } else {
                //System.out.println("Directories already exist: " + parentPath);
            }
        }
    }

    public static String find(String searchPath, String fileName) {
        File directory = new File(searchPath);

        if (!directory.isDirectory() || !directory.exists()) {
            return null;
        }

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String foundFilePath = find(file.getAbsolutePath(), fileName);
                    if (foundFilePath != null) {
                        return foundFilePath; 
                    }
                } else if (file.getName().equals(fileName)) {
                    return file.getAbsolutePath();
                }
            }
        }

        return null; 
    }


    //returns the line number of a character by character index
    public static int getLineIndex(String fileName, int characterIndex) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            int currentCharIndex = 0;
            int currentLine = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                currentLine++;
                int lineLength = line.length();

                if (currentCharIndex + lineLength >= characterIndex) {
                    return currentLine;
                }

                currentCharIndex += lineLength + 1;
            }

            return -1;
        }
    }

}
