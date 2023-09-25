package org.fortranas;

import org.fortranas.antlr4.generated.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.*;

public class AbstractFortran90Lexer extends Fortran90Lexer {
    private int currentLine = 1;

    public AbstractFortran90Lexer(CharStream input) {
        super(input);
    }

    @Override
    public Token emit() {
        CommonToken token = (CommonToken) super.emit();
        token.setLine(currentLine);
        return token;
    }

    @Override
    public Token nextToken() {
        Token next = super.nextToken();
        if (next.getChannel() == Token.DEFAULT_CHANNEL) {
            currentLine = getLine();
        }
        return next;
    }

}

