package org.fortranas;

import org.apache.commons.cli.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;

import org.antlr.v4.runtime.*;

import java.io.File;
import java.util.function.Consumer;

import java.util.List;

public class FortranAS {

    public static void main(String[] args) {

        Options options = new Options();

        Option listLexers = Option.builder("l")
                .longOpt("list-lexers")
                .desc("List available lexers and exit")
                .required(false)
                .type(Boolean.class)
                .build();

        Option inputDir = Option.builder("i")
                .longOpt("input-source-code-directory")
                .desc("Input Fortran source code directory (required)")
                .required(false)
                .hasArg()
                .build();

        Option outputDir = Option.builder("o")
                .longOpt("output-directory")
                .desc("Output directory flag (DEFAULT: ./output)\n")
                .required(false)
                .hasArg()
                .build();

        Option lexerName = Option.builder("L")
                .longOpt("lexer")
                .desc("Lexer name flag (DEFAULT: Fortran90Lexer)\n")
                .required(false)
                .hasArg()
                .build();

        Option help = Option.builder("h")
                .longOpt("help")
                .desc("Print help")
                .required(false)
                .build();

        options.addOption(listLexers);
        options.addOption(inputDir);
        options.addOption(outputDir);
        options.addOption(lexerName);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("list-lexers")) {
                listLexers();
                return;
            }

            if (cmd.hasOption("help")) {
                printHelp(options);
                return;
            }

            String inputDirectory = cmd.getOptionValue("input-source-code-directory");
            if (inputDirectory == null) {
                throw new IllegalArgumentException("ERROR: Input source code directory is required.");
            }

            File inputDirFile = new File(inputDirectory);
            if (!inputDirFile.exists() || !inputDirFile.isDirectory()) {
                throw new IllegalArgumentException("ERROR: Input source code directory does not exist or is not a directory.");
            }

            String outputDirectory = cmd.getOptionValue("o", getDefaultOutputDirectory());
            createDirectory(outputDirectory);

            String lexer = cmd.getOptionValue("lexer", "Fortran90Lexer");
            if (!isLexer(lexer)) {
                throw new IllegalArgumentException("Lexer '" + lexer + "' does not exist.");
            }

            System.out.println("  Using:");
            System.out.println("    input source code directory: " + inputDirectory);
            System.out.println("    output directory: " + outputDirectory);
            System.out.println("    lexer Name: " + lexer);


            FortranAS.processFortranFiles(outputDirectory, inputDirectory, ".f90");

        } catch (ParseException e) {
            System.err.println("ERROR: Error parsing command line arguments: " + e.getMessage());
            printHelp(options);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            printHelp(options);
            System.exit(1);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        int helpWidth = 100;
        formatter.setWidth(helpWidth);
        String header = "FortranAS is a Fortran parsing tool based off of antlr4\n\n";
        String footer = "\nPlease report issues at https://https://github.com/akoerner/FortranAS/issues";

        formatter.printHelp("fortranas", header, options, footer, true);
    }

    private static String getDefaultOutputDirectory() {
        String currentDir = System.getProperty("user.dir");
        return currentDir + File.separator + "output";
    }

    private static void createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            try {
                if (directory.mkdirs()) {
                    System.out.println("Created output directory: " + directoryPath);
                } else {
                    throw new IOException("Failed to create output directory: " + directoryPath);
                }
            } catch (IOException e) {
                System.err.println("Error creating output directory: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    public static void listLexers() {
        System.out.println("Available Fortran Lexers: ");
        List<String> classes = JarClassLoader.getClasses();
        for (String file : classes) {
            if (isLexer(file)) {
                System.out.println("  " + file);
            }
        }
        System.out.println("  use with: fortranas --lexer <lexer name> ...");
    }

    public static boolean isLexer(String needle) {
        needle = needle.trim();
        String regexPattern = "^Fortran\\d+Lexer$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(needle);

        return matcher.matches();
    }

    private static void processFiles(File directory, String fileExtension, Consumer<File> processFunction) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processFiles(file, fileExtension, processFunction);
                } else if (file.getName().endsWith(fileExtension)) {
                    processFunction.accept(file);
                }
            }
        }
    }


    private static void processFortranFiles(String outputDirectory, String sourceDirectory, String fileExtension){

        Consumer<File> processFunction = (file) -> {
            System.out.println("Processing file: " + file.getAbsolutePath());

            try {
                AbstractFortranTree abstractFortranTree = new AbstractFortranTree(file.getAbsolutePath());
                abstractFortranTree.saveOutputFiles(outputDirectory, sourceDirectory);
            } catch (IOException e) {
                System.err.println("An IOException occurred while processing files: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Exception occurred while processing files: " + e.getMessage());
            }
        };
         processFiles(new File(sourceDirectory), fileExtension, processFunction);

    }




}

