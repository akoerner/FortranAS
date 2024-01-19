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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import java.util.List;
import java.util.ArrayList;

import com.google.common.io.Files;

import java.util.Map;
import java.util.HashMap;

import java.sql.SQLException;

import java.util.logging.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.concurrent.*;



public class FortranAS {


    private static final String FORTRAN_AS_CONFIG_FILE="FortranAS.conf";
    public static String[] fortranFileExtensions;
    public static String defaultLexer;
    private static final Logger LOGGER = FortranASLogger.getLogger();
    public static FortranASConfig fortranASConfig;
    public static Map<String, Object> config; 
    private static int sourceFileCount = 0;
    private static int processedFilesCount = 0;



    public static void main(String[] args) {

        try {
            FortranAS.fortranASConfig = new FortranASConfig(FORTRAN_AS_CONFIG_FILE);
            FortranAS.config = FortranAS.fortranASConfig.getConfig();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
        }));

        Options options = new Options();

        Option listLexers = Option.builder("l")
            .longOpt("list-lexers")
            .desc("List available lexers and exit")
            .required(false)
            .type(Boolean.class)
            .build();

        Option inputDir = Option.builder("i")
            .longOpt("input-source-code-directory")
            .desc("Input Fortran source code directory (DEFAULT: ./source)")
            .required(false)
            .hasArg()
            .build();

        Option outputDir = Option.builder("o")
            .longOpt("output-directory")
            .desc("Output directory flag (DEFAULT: ./output)\n")
            .required(false)
            .hasArg()
            .build();

        Option parseFortanFiles = Option.builder("p")
            .longOpt("parse-fortran-files")
            .desc("Parse FORTRAN files")
            .required(false)
            .type(Boolean.class)
            .build();

        Option printSourceFiles = Option.builder("P")
            .longOpt("print-fortran-files")
            .desc("Print discovered FORTRAN files and exit")
            .required(false)
            .type(Boolean.class)
            .build();

        Option summarizeDatabase = Option.builder("s")
            .longOpt("summarize-database")
            .desc("Prints a summary of the provided database")
            .required(false)
            .type(Boolean.class)
            .build();


        Option calculateCodeClones = Option.builder("c")
            .longOpt("calculate-code-clones")
            .desc("Calculate code clones for all subtrees in the provided database")
            .required(false)
            .type(Boolean.class)
            .build();

        Option lexerName = Option.builder("L")
            .longOpt("lexer")
            .required(false)
            .hasArg()
            .build();

        Option help = Option.builder("h")
            .longOpt("help")
            .desc("Print help")
            .required(false)
            .build();

        options.addOption(listLexers);
        options.addOption(parseFortanFiles);
        options.addOption(calculateCodeClones);
        options.addOption(summarizeDatabase);
        options.addOption(printSourceFiles);
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

            String outputDirectory = cmd.getOptionValue("o", getDefaultOutputDirectory());

            FortranAS.defaultLexer = String.valueOf(FortranAS.config.get("default_lexer"));
            String lexer = cmd.getOptionValue("lexer", FortranAS.defaultLexer);
            if (!isLexer(lexer)) {
                throw new IllegalArgumentException("Lexer '" + lexer + "' does not exist.");
            }

            //LOGGER.info("  FortranAS Using:");
            //LOGGER.info("    input source code directory: " + inputDirectory);
            //LOGGER.info("    output directory: " + outputDirectory);
            //LOGGER.info("    lexer Name: " + lexer);

            FortranAS.fortranFileExtensions = String.valueOf(FortranAS.config.get("fortran_file_extensions")).split(",");
            for (int i = 0; i < FortranAS.fortranFileExtensions.length; i++) {
                FortranAS.fortranFileExtensions[i] = FortranAS.fortranFileExtensions[i].trim();
            }
            List<String> fortranFiles = new ArrayList<String>();
            FortranASDatabase fortranASDatabase = null;

            try {
                fortranASDatabase = new FortranASDatabase(FortranAS.config);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            String inputDirectory = "";
            if (cmd.hasOption("input-source-code-directory")) {
                inputDirectory = cmd.getOptionValue("input-source-code-directory");
                File inputDirectoryFile = new File(inputDirectory);
                if (!inputDirectoryFile.exists() || !inputDirectoryFile.isDirectory()) {
                    throw new IllegalArgumentException("ERROR: Input source code directory does not exist or is not a directory.");
                }
                fortranFiles = FileTools.findFiles(inputDirectory, fortranFileExtensions);
            }


            System.out.println("");
            System.out.println("  FortranAS:");
            System.out.println("    input source code directory: " + inputDirectory);
            System.out.println("    input source file count: " + fortranFiles.size());
            System.out.println("    output directory: " + outputDirectory);
            System.out.println("    lexer Name: " + lexer);
            FortranAS.fortranASConfig.printConfig();

            if (cmd.hasOption("print-fortran-files")) {
                if (inputDirectory == null) {
                    throw new IllegalArgumentException("ERROR: Input source code directory is required.");
                }
                System.out.println();
                System.out.println("    Printing FORTRAN files...");
                System.out.println();
                printFortranFiles(fortranFiles);
            }


            if (cmd.hasOption("parse-fortran-files")) {
                if (inputDirectory == null) {
                    throw new IllegalArgumentException("ERROR: Input source code directory is required.");
                }
                System.out.println();
                System.out.println("    Parsing FORTRAN files...");
                System.out.println();
                parseFortranFiles(fortranFiles, inputDirectory, outputDirectory, fortranASDatabase);
            }

            if (cmd.hasOption("calculate-code-clones")) {
                System.out.println();
                System.out.println("    Calculating code clones...");
                System.out.println();
                System.out.println("Progress | Elapsed Time (hh:mm:ss) | Estimated Time (hh:mm:ss)| Calculations/Second (estimated) | Completed calculations/required calculations (estimated)");
                try {
                    fortranASDatabase.calculateClones();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            if (cmd.hasOption("summarize-database")) {
                System.out.println();
                System.out.println("    Printing FortranAS database summary...");
                fortranASDatabase.getDatabase().printDatabaseSummary();
                System.out.println();
            }

        } catch (ParseException e) {
            System.err.println("ERROR: Error parsing command line arguments: " + e.getMessage());
            printHelp(options);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            printHelp(options);
            System.exit(1);
        }

        System.exit(0);
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        int helpWidth = 100;
        formatter.setWidth(helpWidth);
        String header = "\nFortranAS is an Antlr4 powered FORTRAN parsing and code clone detection tool.\n\n";
        header += "  Example basic usage: \n";
        header += "      1. Place FORTRAN source code in in the ./source directory\n";
        header += "      2. Run FortranAS: fortranas -p -c\n";
        header += "      3. Optionally, generate the code clone pairs: \n";
        header += "        python3 code_clone_analysis/generate_clone_pairs.py\n\n";
        String footer = "\nPlease report issues at https://https://github.com/akoerner/FortranAS/issues";

        formatter.printHelp("fortranas", header, options, footer, true);
    }

    private static void parseFortranFiles(List<String> fortranFiles, String sourceDirectory, String outputDirectory, FortranASDatabase fortranASDatabase) {

        createDirectory(outputDirectory);
        int progress = 0;
        int parseTimeout_ms = ((int)FortranAS.config.get("parse_timeout_ms"));
        for (String fortranFile : fortranFiles) {

            FortranAS.parseFortranFile(fortranFile, sourceDirectory, outputDirectory, fortranASDatabase);

            progress++;
            System.out.println("Progress | Elapsed Time (hh:mm:ss) | Estimated Time (hh:mm:ss)| Processed Files/Second (estimated) | Completed Files)");
            ProgressBar.updateProgressBar((long)progress, (long)fortranFiles.size());
            System.out.println();
        }

        ProgressBar.stop();
    }

    private static void printFortranFiles(List<String> fortranFiles) {
        int sourceFileIndex=1;
        for (String fortranFile : fortranFiles) {
            System.out.println("      source file #" + sourceFileIndex + ": " + fortranFile);
            sourceFileIndex++;
        }
        System.out.println("    source file count: " + (fortranFiles.size()));

    }


    private static void parseFortranFile(String fortranFile, String sourceDirectory, String outputDirectory, FortranASDatabase fortranASDatabase) {


        ExecutorService executor = Executors.newSingleThreadExecutor();
        int parseTimeout_ms = ((int)FortranAS.config.get("parse_timeout_ms"));
        File file = new File(fortranFile);
        String fileAbsolutePath = file.getAbsolutePath();
        String fileRelativePath = Paths.get(System.getProperty("user.dir")).relativize(Paths.get(file.getAbsolutePath())).toString();
        Boolean useParseTree = ((Boolean)FortranAS.config.get("use_parse_tree")).booleanValue();
        String antlr4ParsingLog = null;
        AbstractFortranTree abstractFortranTree = null;
        final Map<String, AbstractFortranTree> map = new HashMap<>();
        try {
            System.out.println("");
            String outputFileBaseName = AbstractFortranTree.getOutputFileBaseName(outputDirectory, sourceDirectory, fileAbsolutePath);
            antlr4ParsingLog = outputFileBaseName + ".antlr4_parsing.log";
            Future<?> future = executor.submit(() -> {
                try {
                    map.put("abstractFortranTree", new AbstractFortranTree(fileRelativePath, sourceDirectory, outputDirectory, FortranAS.config));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            future.get(parseTimeout_ms, TimeUnit.MILLISECONDS);

            abstractFortranTree = (AbstractFortranTree)map.get("abstractFortranTree");
            System.out.println("    Finished parsing FORTRAN source file: " + fileRelativePath);
            System.out.println();
            abstractFortranTree.saveOutputFiles(outputDirectory, sourceDirectory);
            if (useParseTree) {
                fortranASDatabase.insertNodes(abstractFortranTree.getParseTreeNodes(), fileRelativePath);
            } else {
                fortranASDatabase.insertNodes(abstractFortranTree.getASTNodes(), fileRelativePath);
            }
            int lineCount =((Number) abstractFortranTree.getData().get("endLineNumber")).intValue();
            int tokenCount =((Number) abstractFortranTree.getData().get("tokenCount")).intValue();
            fortranASDatabase.insertFile(fileRelativePath, lineCount, tokenCount);
            fortranASDatabase = null;
            abstractFortranTree = null;
        } catch (SQLException e) {
            System.err.println("    An SQLException occurred while processing file: " + fileRelativePath);
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println("    ERROR: Parse time exceeded timeout defined by parse_timeout_ms: " + parseTimeout_ms + " ms" );
            System.err.println("      adjust 'parse_timeout_ms' to allow more parse time.");
            System.err.println("      Review the Antlr4 parse log for more information: " + antlr4ParsingLog); 
            System.err.println(" "); 
            FileTools.log(antlr4ParsingLog, "ERROR: Parse time exceeded timeout defined by parse_timeout_ms: " + parseTimeout_ms + " ms");
            FileTools.log(antlr4ParsingLog, "  adjust parse_timeout_ms to allow more parse time.");
            FileTools.log(antlr4ParsingLog, e.toString());
        } catch (Throwable t) {
            System.err.println("    ERROR: Complete parse failure occurred with processing file: " + fileRelativePath); 
            System.err.println("      Reported exception: " + t.toString());
            System.err.println("      Review the Antlr4 parse log for a complete stack trace and more information: " + antlr4ParsingLog); 
            System.err.println(" "); 
            FileTools.log(antlr4ParsingLog, t.toString());
            FileTools.log(antlr4ParsingLog, t.getMessage());
            t.printStackTrace();
        }
        abstractFortranTree = null;
        executor.shutdown();
    }


    private static String getDefaultOutputDirectory() {
        String currentDir = System.getProperty("user.dir");
        return currentDir + File.separator + "output";
    }

    private static void createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            try {
                if (!directory.mkdirs()) {
                    throw new IOException("ERROR: Failed to create output directory: " + directoryPath);
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

}

