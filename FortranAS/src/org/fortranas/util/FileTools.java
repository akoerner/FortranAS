
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

import java.io.*;
import java.nio.file.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileTools {

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

}
