import org.antlr.v4.runtime.*;

public class ContinueOnTokenErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        System.err.println("Syntax error at line " + line + ", position " + charPositionInLine + ": " + msg);
    }
}

