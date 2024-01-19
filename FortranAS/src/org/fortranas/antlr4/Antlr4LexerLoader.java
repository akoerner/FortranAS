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

        lexer.removeErrorListeners();
        lexer.addErrorListener(new ContinueOnTokenErrorListener());

        lexerClass = null;
        fortranSourceFileCharStream = null;
        constructor = null;
        return lexer;
    }

}
