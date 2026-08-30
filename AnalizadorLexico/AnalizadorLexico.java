package AnalizadorLexico;

import sourcemanager.*;

class AnalizadorLexico {
    String lexema;
    char caracterActual;
    SourceManager SourceManager;

    public AnalizadorLexico(SourceManager gestor) {
        SourceManager = gestor;
        actualizarCaracterActual();
    }

    public Token proximoToken() {
        lexema = "";
        return e0();
    }

    private void actualizarLexema() {
        lexema = lexema + caracterActual;
    }

    private Cacho actualizarCaracterActual() {
        caracterActual = SourceManager.getNextChar();
    }

    private Token checkForPunctuation() {
        if (caracterActual == '(')
            return new Token(TokenType.openParenthesis, lexema, SourceManager.getLineNumber());
        if (caracterActual == ')')
            return new Token(TokenType.closeParenthesis, lexema, SourceManager.getLineNumber());
        if (caracterActual == '[')
            return new Token(TokenType.openSquareBracket, lexema, SourceManager.getLineNumber());
        if (caracterActual == ']')
            return new Token(TokenType.closeSquareBracket, lexema, SourceManager.getLineNumber());
        if (caracterActual == '{')
            return new Token(TokenType.openBraces, lexema, SourceManager.getLineNumber());
        if (caracterActual == '}')
            return new Token(TokenType.closeBraces, lexema, SourceManager.getLineNumber());
        if (caracterActual == ';')
            return new Token(TokenType.semicolon, lexema, SourceManager.getLineNumber());
        if (caracterActual == ',')
            return new Token(TokenType.comma, lexema, SourceManager.getLineNumber());
        if (caracterActual == '.')
            return new Token(TokenType.period, lexema, SourceManager.getLineNumber());
        if (caracterActual == ':')
            return new Token(TokenType.twopoints, lexema, SourceManager.getLineNumber());
        else
            return null;
    }

    private Token e0() {
        if (Character.isDigit(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return e1();
        } else if (Character.isLetter(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return e2();
        } else if (caracterActual == '>') {
            actualizarLexema();
            actualizarCaracterActual();
            return e3();
        } else if (SourceManager.esEOF(caracterActual)) {
            return e5();
        } else if (Character.isWhitespace(caracterActual)) {
            actualizarCaracterActual();
            return e0();
        } else {
            actualizarLexema();
            throw new ExcepcionLexica(lexema, SourceManager.nroLinea());
        }

        Token punctuation = checkForPunctuation();
        if (punctuation != null)
            return punctuation;

        // No es caracter valido

        // ERROR
    }

    private Token e1() {
        if (Character.isDigit(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return e1();
        } else {
            return new Token("entero", lexema, SourceManager.nroLinea());
        }
    }

    private Token e2() {
        if (Character.isLetter(caracterActual) || Character.isDigit(caracterActual)) {
            actualizarLexema();
            actualizarCaracterActual();
            return e2();
        } else {
            return new Token("identificador", lexema, SourceManager.nroLinea());
        }
    }

    private Token e3() {
        if (caracterActual == '=') {
            actualizarLexema();
            actualizarCaracterActual();
            return e4();
        } else {
            return new Token(TokenType.greater, lexema, SourceManager.getLineNumber());
        }
    }

    private Token e4() {

    }

    private Token e5() {
        return new Token("EOF", lexema, SourceManager.nroLinea());
    }
}