import org.antlr.v4.runtime.*;

public class ContinueOnTokenErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        return null;
    }
}

