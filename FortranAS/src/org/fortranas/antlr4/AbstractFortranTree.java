

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import com.google.gson.Gson;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.Interval;

import java.io.IOException;
import java.io.File;


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
    ArrayList<Token> tokens = new ArrayList<Token>();

    public AbstractFortranTree(String fortranSourceFilePath) throws IOException, Exception{

        System.out.println("Processing fortran source file: " + fortranSourceFilePath);
        this.data.put("fortranSourceFile", fortranSourceFilePath);
        File fortranSourceFile = new File(fortranSourceFilePath);

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
    
    private Map<String, Object> getPT() throws IOException, Exception{
        if (this.pt == null){ 
            this.pt = ParseTreeConverter.toMap(this.getParseTree(), this.getTokens(), this.getSource(), (String)this.data.get("lexerClassName")); 
        }
        return this.pt;
    }

    private Map<String, Object> getAST() throws IOException, Exception{
        if (this.ast == null){
            this.ast = Tree.clone(this.getPT());
            Map<String, Object> pruneOn = new HashMap<String, Object>();
            pruneOn.put("text", "Program *");
            Tree.prune(this.ast, pruneOn);
            pruneOn.put("text", "implicit");
            Tree.prune(this.ast, pruneOn);
            pruneOn.put("text", "none");
            Tree.prune(this.ast, pruneOn);
            pruneOn.put("text", "implicit none");
            Tree.prune(this.ast, pruneOn);
            pruneOn.put("text", "<EOF>");
            Tree.prune(this.ast, pruneOn);
            pruneOn.put("text", "program");
            Tree.prune(this.ast, pruneOn);
            pruneOn.put("text", "end program *");
            Tree.prune(this.ast, pruneOn);
            pruneOn.put("text", "program *");
            Tree.prune(this.ast, pruneOn);
            //Tree.promote(this.ast, Map.of("name", "Level2Expr"), Map.of("tokenName", "PLUS"));
            //Tree.promote(this.ast, Map.of("name", "AddOperand"), Map.of("tokenName", "STAR"));
            this.ast = Tree.merge(this.ast, "text");

            //Tree.promote(this.ast, Map.of("name", "OutputItemList"), Map.of("tokenName", "PLUS"));
            //Tree.promote(this.ast, Map.of("name", "AddOperand"), Map.of("tokenName", "STAR"));
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

    public void saveOutputFiles(String outputDirectory, String sourceDirectory) throws IOException, Exception{

        String fortranSourceFile = ((String)this.data.get("fortranSourceFile")).replace(sourceDirectory, "");

        String abstractSyntaxTreeJSONFileName = outputDirectory + fortranSourceFile + ".abstract_syntax_tree.json";
        String abstractSyntaxTreeJSONFileContents = JSONConverter.toJSON(this.getAST());
        FileTools.mkdirp(abstractSyntaxTreeJSONFileName);
        FileTools.writeFile(abstractSyntaxTreeJSONFileName, abstractSyntaxTreeJSONFileContents);

        String abstractSyntaxTreeDOTFileName = outputDirectory + fortranSourceFile + ".abstract_syntax_tree.dot";
        String abstractSyntaxTreeDOTFileContents = ParseTreeConverter.toDOT(this.getAST(), (String)this.data.get("lexerClassName"), false);
        FileTools.mkdirp(abstractSyntaxTreeDOTFileName);
        FileTools.writeFile(abstractSyntaxTreeDOTFileName, abstractSyntaxTreeDOTFileContents);


        String parseTreeJSONFileName = outputDirectory + fortranSourceFile + ".parse_tree.json";
        //String parseTreeJSONFileContents = ParseTreeConverter.toJSON(this.getParseTree(), this.getTokens(), this.getSource());
        String parseTreeJSONFileContents = JSONConverter.toJSON(this.getPT());
        FileTools.mkdirp(parseTreeJSONFileName);
        FileTools.writeFile(parseTreeJSONFileName, parseTreeJSONFileContents);
        
        String parseTreeDOTFileName = outputDirectory + fortranSourceFile + ".parse_tree.dot";
        String parseTreeDOTFileContents = ParseTreeConverter.toDOT(this.getPT(), (String)this.data.get("lexerClassName"), true);
        //String parseTreeDOTFileContents = ParseTreeConverter.toDOT(this.getParseTree(), this.getTokens(), this.getSource());
        FileTools.mkdirp(parseTreeDOTFileName);
        FileTools.writeFile(parseTreeDOTFileName, parseTreeDOTFileContents);



        String abstractFortranTreeJSONFileName = outputDirectory + fortranSourceFile + ".abstract_fortran_tree.json";
        String abstractFortranTreeJSONFileContents = this.toJSON();
        FileTools.mkdirp(abstractFortranTreeJSONFileName);
        FileTools.writeFile(abstractFortranTreeJSONFileName, abstractFortranTreeJSONFileContents);

        
        //String abstractSyntaxTreeDOTFileName = outputDirectory + fortranSourceFile + ".abstract_syntax_tree.dot";
        //String abstractSyntaxTreeDOTFileContents = ParseTreeConverter.toDOT(ParseTreeConverter.mergeNodes(this.getAST()));
        //FileTools.mkdirp(abstractSyntaxTreeDOTFileName);
        //FileTools.writeFile(abstractSyntaxTreeDOTFileName, abstractSyntaxTreeDOTFileContents);

/*
         String abstractFortranTreeJSONFileName = outputDirectory + fortranSourceFile + ".abstract_fortran_tree.json";
        String abstractFortranTreeJSONFileContents = this.toJSON();
        FileTools.mkdirp(abstractFortranTreeJSONFileName);
        FileTools.writeFile(abstractFortranTreeJSONFileName, abstractFortranTreeJSONFileContents);


        String parseTreeJSONFileName = outputDirectory + fortranSourceFile + ".parse_tree.json";
        String parseTreeJSONFileContents = this.getParseTreeJSONString();
        FileTools.mkdirp(parseTreeJSONFileName);
        FileTools.writeFile(parseTreeJSONFileName, parseTreeJSONFileContents);

        String abstractSyntaxTreeJSONFileName = outputDirectory + fortranSourceFile + ".abstract_syntax_tree.json";
        String abstractSyntaxTreeJSONFileContents = this.getAbstractSyntaxTreeJSONString();
        FileTools.mkdirp(abstractSyntaxTreeJSONFileName);
        FileTools.writeFile(abstractSyntaxTreeJSONFileName, abstractSyntaxTreeJSONFileContents);

        String parseTreeDOTFileName = outputDirectory + fortranSourceFile + ".parse_tree.dot";
        String parseTreeDOTFileContents = this.getParseTreeDOTString();
        FileTools.mkdirp(parseTreeDOTFileName);
        FileTools.writeFile(parseTreeDOTFileName, parseTreeDOTFileContents);
        FileTools.dotToSVG(parseTreeDOTFileName);
        FileTools.dotToPNG(parseTreeDOTFileName);
        FileTools.svgToPNG(parseTreeDOTFileName + ".svg");

        String abstractSyntaxTreeDOTFileName = outputDirectory + fortranSourceFile + ".abstract_syntax_tree.dot";
        String abstractSyntaxTreeDOTFileContents = this.getAbstractSyntaxTreeDOTString();
        FileTools.mkdirp(abstractSyntaxTreeDOTFileName);
        FileTools.writeFile(abstractSyntaxTreeDOTFileName, abstractSyntaxTreeDOTFileContents);
        FileTools.dotToSVG(abstractSyntaxTreeDOTFileName);
        FileTools.dotToPNG(abstractSyntaxTreeDOTFileName);
        FileTools.svgToPNG(abstractSyntaxTreeDOTFileName + ".svg");

        */

        String logFileName = outputDirectory + fortranSourceFile + ".log";
        String logFileContents = this.toString();
        FileTools.mkdirp(logFileName);
        FileTools.writeFile(logFileName, logFileContents);

        String antlr4ParseTreeFileName = outputDirectory + fortranSourceFile + ".antlr4_parse_tree.txt";
        String antlr4ParseTreeFileContents = (String)this.data.get("antlr4ParseTreeString");
        FileTools.mkdirp(antlr4ParseTreeFileName);
        FileTools.writeFile(antlr4ParseTreeFileName, antlr4ParseTreeFileContents);
    }

}

