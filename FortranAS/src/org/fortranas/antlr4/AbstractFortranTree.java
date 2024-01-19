import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.Interval;

import java.io.IOException;
import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

class AbstractFortranTree {

    public static final String DEFAULT_LEXER_CLASS = "Fortran90Lexer";


    public int nodeCounter = 0;

    public org.antlr.v4.runtime.tree.ParseTree parseTree;
    public org.antlr.v4.runtime.CommonTokenStream tokenStream; 
    public Lexer lexer;
    public org.antlr.v4.runtime.Parser parser;


    public String lexerClassName = DEFAULT_LEXER_CLASS;
    public String parserClassName = this.lexerClassName.replace("Lexer", "Parser");

    Map<String, Object> pt;
    Map<String, Object> ast;

    public Map<String, Object> data = new HashMap<String, Object>();
    public char[] source; 
    public String antlr4ParsingLog;
    public String sourceDirectory;
    public String outputDirectory;
    ArrayList<Token> tokens = new ArrayList<Token>();
    public Map<String, Object> config;

    public AbstractFortranTree(String fortranSourceFilePath, String sourceDirectory, String outputDirectory, Map<String, Object> config) throws IOException, Exception{

        this.config = config;
        this.data.put("fortranSourceFile", fortranSourceFilePath);
        File fortranSourceFile = new File(fortranSourceFilePath);

        this.sourceDirectory = sourceDirectory;
        this.outputDirectory = outputDirectory;
        String outputFileBaseName = AbstractFortranTree.getOutputFileBaseName(outputDirectory, sourceDirectory, fortranSourceFilePath);
        this.antlr4ParsingLog = outputFileBaseName + ".antlr4_parsing.log";
        this.parse();


        int tokenStartNumber = this.getParseTree().getSourceInterval().a;
        this.data.put("tokenStartNumber", tokenStartNumber);

        String lexerClassName =this.getLexer().getClass().getName(); 
        this.data.put("lexerClassName", lexerClassName);

        String parserClassName =this.getParser().getClass().getName();
        this.data.put("parserClassName", parserClassName);

        int tokenEndNumber = this.getParseTree().getSourceInterval().b;
        this.data.put("tokenEndNumber", tokenEndNumber);

        String tokensString = this.getTokensString();
        this.data.put("tokensString", tokensString);

        ArrayList<Token> tokens = this.getTokens(); 
        this.data.put("tokens", tokens);

        int tokenCount = this.getTokenCount();
        this.data.put("tokenCount", tokenCount);

        int startLineNumber = this.getTokens().get(tokenStartNumber).getLine();
        this.data.put("startLineNumber", startLineNumber);

        int endLineNumber = this.getTokens().get(tokenEndNumber).getLine();
        this.data.put("endLineNumber", endLineNumber);

        String antlr4ParseTreeString = this.toParseTreeString();  
        this.data.put("antlr4ParseTreeString", antlr4ParseTreeString);

        //this.data.put("parseTree", this.getPT());

        //this.data.put("abstractSyntaxTree", this.getAST());
    }

    public String getAntlr4ParsingLog(){
        return this.antlr4ParsingLog;
    }

    public void logToAntlr4ParsingLog(String message){
        FileTools.log(this.antlr4ParsingLog, message);
    }

    public void parse() throws IOException, Exception{
        System.err.println("  Parsing FORTRAN source file: " + (String)this.data.get("fortranSourceFile"));
        PrintStream systemErr = System.err;
        try {

            FileTools.mkdirp(this.antlr4ParsingLog);
            File errorLog = new File(this.antlr4ParsingLog);
            if (!errorLog.exists()) {
                errorLog.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(errorLog);
            PrintStream antlr4ParseErr = new PrintStream(fos);
            System.setErr(antlr4ParseErr);
            this.getParseTree();
            this.getTokens();
            //System.out.println(this.antlr4ParsingLog);
            if(errorLog.length() != 0){
                System.out.println("    Partial parsing error, Antlr4 reported parsing error, for more information view the antlr4 parsing log file: " + this.antlr4ParsingLog );
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.setErr(systemErr);
        }

    }

    public Map<String, Object> getData(){
        return this.data;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Fortran source file: " + ((String)this.data.get("fortranSourceFile")).replace("/tmp/src", ""));
        builder.append("\n");

        builder.append(" USING: Lexer: " + this.data.get("lexerClassName"));
        builder.append("\n");

        builder.append(" USING: Parser: " + this.data.get("parserClassName"));
        builder.append("\n");

        builder.append("  Token start number: " + this.data.get("tokenStartNumber"));
        builder.append("\n");

        builder.append("  Token end number: " + this.data.get("tokenEndNumber"));
        builder.append("\n");

        builder.append("  Tokens:");
        builder.append("\n");
        builder.append(this.data.get("tokensString"));
        builder.append("\n");

        builder.append("  Token Count: " + this.data.get("tokenCount"));
        builder.append("\n");

        builder.append("  Start line number: " + this.data.get("startLineNumber"));
        builder.append("\n");

        builder.append("  End line number: " + this.data.get("endLineNumber"));
        builder.append("\n");

        builder.append("\n");
        builder.append("  Antlr4 Parse Tree String: ");
        builder.append("\n");
        builder.append("    " +  this.data.get("antlr4ParseTreeString"));
        builder.append("\n");

        return builder.toString();
    } 

    public Lexer getLexer() throws Exception {
        //if (this.lexer == null) {
        this.lexer = Antlr4LexerLoader.loadLexer(this.lexerClassName, (String)this.data.get("fortranSourceFile"));
        //}
        return this.lexer;
    }

    public Parser getParser() throws Exception{
        if (this.parser == null) {
            this.parser = Antlr4ParserLoader.loadParser(this.parserClassName, this.getLexer());
        }

        return this.parser;
    }

    public List<Map<String, Object>> getPTNodes() throws IOException, Exception{
        return Tree.getNodes(this.getPT());
    }

    public List<Map<String, Object>> getParseTreeNodes() throws IOException, Exception{
        return Tree.getNodes(this.getPT());
    }

    public List<Map<String, Object>> getASTNodes() throws IOException, Exception{
        return Tree.getNodes(this.getAST());
    }

    private Map<String, Object> getPT() throws IOException, Exception{
        if (this.pt == null){ 
            this.pt = ParseTreeConverter.toMap(this.getParseTree(), this.getTokens(), this.getSource(), (String)this.data.get("lexerClassName"), (String)this.data.get("fortranSourceFile"));
            Tree.populateSubtreeStrings(this.pt, this.config);
        }
        return this.pt;
    }

    private Map<String, Object> getAST() throws IOException, Exception{
        if (this.ast == null){
            this.ast = Tree.clone(this.getPT());

            Boolean enableASTPruning = ((Boolean)this.config.get("enable_ast_pruning")).booleanValue();
            Boolean enableASTMerging = ((Boolean)this.config.get("enable_ast_merging")).booleanValue();
            Boolean enableASTPromoting = ((Boolean)this.config.get("enable_ast_promoting")).booleanValue();
            Boolean enableASTReplacing = ((Boolean)this.config.get("enable_ast_replacing")).booleanValue();

            String[][] parentRules = {
                {"rule", "AddOperand"},
                {"rule", "OutputItemList"},
                {"rule", "Program"},
                {"rule", "ExecutableProgram"},
                {"rule", "MainRange"},
                {"rule", "Body"},
                {"rule", "BodyConstruct"}
            };
            String[][] childRules = {
                {"token_name", "DOP"},
                {"token_name", "PLUS"},
                {"token_name", "MINUS"},
                {"token_name", "STAR"},
                {"token_name", "DIV"},
                {"token_name", "POWER"},
                {"token_name", "LT"},
                {"token_name", "LE"},
                {"token_name", "EQ"},
                {"token_name", "NE"},
                {"token_name", "GT"},
                {"token_name", "GE"},
                {"token_name", "LNOT"},
                {"token_name", "LAND"},
                {"token_name", "LOR"},
                {"token_name", "NEQV"},
                {"token_name", "EQV"},
                {"rule", "Program"},
                {"rule", "ExecutableProgram"},
                {"rule", "MainRange"},
                {"rule", "Body"},
                {"rule", "BodyConstruct"}
            };

            if(enableASTMerging){
                this.ast = Tree.merge(this.ast, "text");
            }
            if(enableASTPromoting){
                TreePromoteStrategy.promote(this.ast, parentRules, childRules);
            }

            Tree.populateSubtreeStrings(this.ast, this.config);
            if(enableASTPruning){
                Map<String, Object> pruneOn = new HashMap<String, Object>();
                pruneOn.put("subtree_text", "Program *");
                Tree.prune(this.ast, pruneOn);
                pruneOn.put("subtree_text", "implicit");
                Tree.prune(this.ast, pruneOn);
                pruneOn.put("subtree_text", "none");
                Tree.prune(this.ast, pruneOn);
                pruneOn.put("subtree_text", "implicit none");
                Tree.prune(this.ast, pruneOn);
                pruneOn.put("subtree_text", "<EOF>");
                Tree.prune(this.ast, pruneOn);
                pruneOn.put("subtree_text", "program");
                Tree.prune(this.ast, pruneOn);
                pruneOn.put("subtree_text", "end program *");
                Tree.prune(this.ast, pruneOn);
                pruneOn.put("subtree_text", "program *");
                Tree.prune(this.ast, pruneOn);
            }



        }
        return this.ast;
    }


    private ParseTree getParseTree() throws IOException, Exception{
        if (this.parseTree == null) {
            this.parseTree = Antlr4ParserLoader.getParseTree(this.getParser());
        }
        return this.parseTree;
    }

    private ArrayList<Token> getTokens() throws IOException, Exception{
        if (this.tokens.isEmpty()) {
            CommonTokenStream tokenStream = this.getTokenStream();
            for (Token token : tokenStream.getTokens()) {
                this.tokens.add(token);
            }
        }
        return this.tokens;
    }

    private CommonTokenStream getTokenStream() throws IOException, Exception{
        this.tokenStream = new CommonTokenStream(this.getLexer());
        this.tokenStream.fill();
        return this.tokenStream;
    }

    public int getTokenCount() throws IOException, Exception{
        return this.getTokens().size();
    }

    public char[] getSource() throws Exception{
        if (this.source == null) {
            CharStream charStream = this.getLexer().getInputStream();
            int length = charStream.size();
            this.source = new char[length];

            for (int i = 0; i < length; i++) {
                this.source[i] = charStream.getText(Interval.of(i, i)).charAt(0);
            }

        }
        return this.source;
    }

    public char[] toCharArray(CharStream charStream) {
        int length = charStream.size();
        char[] charArray = new char[length];

        for (int i = 0; i < length; i++) {
            charArray[i] = charStream.getText(Interval.of(i, i)).charAt(0);
        }

        return charArray;
    } 

    public String getTokensString() throws IOException, Exception{
        StringBuilder builder = new StringBuilder();
        for (Token token : this.getTokens()) {
            builder.append(token.toString() + "\n");
        }
        return builder.toString();
    }

    public String toJSON() {
        return JSONConverter.toJSON(this.data);
    }

    public static String toParseTreeString(ParseTree parseTree, Parser parser) {
        return parseTree.toStringTree(parser);
    }

    public String toParseTreeString() throws IOException, Exception{
        return AbstractFortranTree.toParseTreeString(this.getParseTree(), this.getParser());
    }

    public static String getOutputFileBaseName(String outputDirectory, String sourceDirectory, String fortranSourceFile) throws IOException, Exception{
        String outputFileBaseName=fortranSourceFile;
        Path path = Paths.get(sourceDirectory);
        String sourceDirectoryName = path.getFileName().toString();
        System.out.println("sourceDirectoryName: " + sourceDirectoryName);
        if (outputFileBaseName.startsWith(sourceDirectoryName)) {
             outputFileBaseName = outputDirectory + outputFileBaseName.substring(sourceDirectoryName.length());
        }

        return outputFileBaseName;

    }

    public void saveOutputFiles(String outputDirectory, String sourceDirectory) throws IOException, Exception{


        String fortranSourceFile = ((String)this.data.get("fortranSourceFile"));

        String outputFileBaseName = AbstractFortranTree.getOutputFileBaseName(outputDirectory, sourceDirectory, fortranSourceFile);

        List<String> dotKeys = Arrays.stream(String.valueOf(this.config.get("dot_file_tree_node_keys")).split(",")).map(String::trim)
            .collect(Collectors.toList());

        Boolean showKeyLabels = ((Boolean)this.config.get("dot_file_tree_node_show_key_labels")).booleanValue();

        String abstractSyntaxTreeJSONFileName = outputFileBaseName + ".abstract_syntax_tree.json";
        String abstractSyntaxTreeJSONFileContents = JSONConverter.toJSON(this.getAST());
        FileTools.mkdirp(abstractSyntaxTreeJSONFileName);
        FileTools.writeFile(abstractSyntaxTreeJSONFileName, abstractSyntaxTreeJSONFileContents);

        String abstractSyntaxTreeDOTFileName = outputFileBaseName + ".abstract_syntax_tree.dot";
        String abstractSyntaxTreeDOTFileContents = ParseTreeConverter.toDOT(this.getAST(), "uuid" , dotKeys, showKeyLabels);
        FileTools.mkdirp(abstractSyntaxTreeDOTFileName);
        FileTools.writeFile(abstractSyntaxTreeDOTFileName, abstractSyntaxTreeDOTFileContents);


        String parseTreeJSONFileName = outputFileBaseName + ".parse_tree.json";
        //String parseTreeJSONFileContents = ParseTreeConverter.toJSON(this.getParseTree(), this.getTokens(), this.getSource());
        String parseTreeJSONFileContents = JSONConverter.toJSON(this.getPT());
        FileTools.mkdirp(parseTreeJSONFileName);
        FileTools.writeFile(parseTreeJSONFileName, parseTreeJSONFileContents);

        String parseTreeDOTFileName = outputFileBaseName + ".parse_tree.dot";
        String parseTreeDOTFileContents = ParseTreeConverter.toDOT(this.getPT(), "uuid" , dotKeys, showKeyLabels);
        //String parseTreeDOTFileContents = ParseTreeConverter.toDOT(this.getParseTree(), this.getTokens(), this.getSource());
        FileTools.mkdirp(parseTreeDOTFileName);
        FileTools.writeFile(parseTreeDOTFileName, parseTreeDOTFileContents);

        String abstractFortranTreeJSONFileName = outputFileBaseName + ".json";
        String abstractFortranTreeJSONFileContents = this.toJSON();
        FileTools.mkdirp(abstractFortranTreeJSONFileName);
        FileTools.writeFile(abstractFortranTreeJSONFileName, abstractFortranTreeJSONFileContents);

        String logFileName = outputFileBaseName + ".log";
        String logFileContents = this.toString();
        FileTools.mkdirp(logFileName);
        FileTools.writeFile(logFileName, logFileContents);

        String antlr4ParseTreeFileName = outputFileBaseName + ".antlr4_parse_tree.txt";
        String antlr4ParseTreeFileContents = (String)this.data.get("antlr4ParseTreeString");
        FileTools.mkdirp(antlr4ParseTreeFileName);
        FileTools.writeFile(antlr4ParseTreeFileName, antlr4ParseTreeFileContents);
    }

}

