import java.io.File;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.*;
import java.nio.file.*;

import org.antlr.v4.runtime.*;

import java.lang.reflect.Constructor;
import java.net.URLClassLoader;
import java.net.URL;


import java.net.URI;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.lang.reflect.Constructor;




public class Antlr4LexerLoader {


    public static Lexer loadLexer(String lexerClassName, String fortranSourceFilePath) throws Exception {
        Class<?> lexerClass = Class.forName(lexerClassName);

        File fortranSourceFile = new File(fortranSourceFilePath);
        CharStream fortranSourceFileCharStream = CharStreams.fromFileName(fortranSourceFile.getAbsolutePath());
        Constructor<?> constructor = lexerClass.getConstructor(CharStream.class);
        Lexer lexer = (Lexer) constructor.newInstance(fortranSourceFileCharStream);

        return lexer;
    }

    static Lexer loadLexer_(String lexerClassName, String fortranSourceFilePath) {

        try {
            String generatedLexerPath = "/app/src/main/java/";
            String abstractLexerClassName = "Abstract" + lexerClassName; 
            String lexerSourceFilePath = generatedLexerPath + abstractLexerClassName + ".java";

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int compilationResult = compiler.run(null, null, null, lexerSourceFilePath);
            File fortranSourceFile = new File(fortranSourceFilePath);

            if (compilationResult == 0) {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("/app/src/main/java/").toURI().toURL()});

                CharStream fortranSourceFileCharStream = CharStreams.fromFileName(fortranSourceFile.getAbsolutePath());

                Class<?> lexerClass = classLoader.loadClass(abstractLexerClassName);
                Constructor<?> constructor = lexerClass.getConstructor(CharStream.class);
                Lexer lexer = (Lexer) constructor.newInstance(fortranSourceFileCharStream); 
                return lexer;
            } else {
                System.err.println("Compilation failed for " + abstractLexerClassName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
