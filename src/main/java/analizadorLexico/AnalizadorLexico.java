package src.main.java.analizadorLexico;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import src.main.java.sourcemanager.*;

public class AnalizadorLexico {
    private static final int _intLiteral_MaxLength = 9;
    String lexema;
    char caracterActual;
    SourceManager SourceManager;
    private List<Error> errors;

    public AnalizadorLexico(SourceManager gestor) {
        SourceManager = gestor;
        errors = new LinkedList<Error>();
        actualizarCaracterActual();
    }

    public Token proximoToken() {
        lexema = "";
        return e0();
    }

    private void actualizarLexema() {
        lexema = lexema + caracterActual;
    }

    private void actualizarCaracterActual() {
        try {
            caracterActual = SourceManager.getNextChar();
        } catch (IOException e) {
            System.err.println("Error de Lectura.");
            throw new RuntimeException("Compilación abortada por falla I/O.", e);
        }
    }

    private Token e0() {
        if (SourceManager.isEOF(caracterActual)) {
            return e_EOF();
        }

        if (Character.isDigit(caracterActual)) {
            consumir();
            return e_literalEntero();
        }

        if (caracterActual == '"') {
            consumir();
            return e_literalString();
        }

        if (caracterActual == '\'') {
            consumir();
            return e_literalChar();
        }

        if (Character.isWhitespace(caracterActual)) {
            actualizarCaracterActual();
            return e0();
        }

        if (Character.isUpperCase(caracterActual)) {
            consumir();
            return e_IdentificadorParametroTipoClase();
        }

        if (Character.isLowerCase(caracterActual)) {
            consumir();
            return e_IdentificadorMetodoVariable();
        }

        if (caracterActual == '/') {
            consumir();
            return e_slash();
        }

        Token punctuation = checkForPunctuation();
        if (punctuation != null)
            return punctuation;

        // No es caracter valido
        // ERROR
        consumir();
        return guardarError();
    }

    private Token guardarError() {
        errors.add(new Error(lexema, SourceManager.getLineNumber()));
        return proximoToken();
    }

    public boolean tieneErrores() {
        return !errors.isEmpty();
    }

    public void imprimirErrores() {
        for (Error error : errors) {
            System.out.println("[Error:" + error.lexema + "|" + error.nroLinea + "]");
        }
    }

    private Token e_literalEntero() {
        return e_literalEntero(0);
    }

    private Token e_literalEntero(int currentLength) {
        if (currentLength > _intLiteral_MaxLength) {
            guardarError();
            return e0();
        }

        if (Character.isDigit(caracterActual)) {
            consumir();
            return e_literalEntero(currentLength + 1);
        }

        return createToken(TokenType.intLiteral);
    }

    private Token e_literalString() {
        if (SourceManager.isEOF(caracterActual) || caracterActual == '\n') {
            return guardarError();
        }

        if (caracterActual == '"') {
            consumir();
            return createToken(TokenType.stringLiteral);
        }

        if (caracterActual == '\\') {
            consumir();
            if (SourceManager.isEOF(caracterActual) || caracterActual == '\n') {
                return guardarError();
            }
            consumir();
            return e_literalString_1();
        }

        consumir();
        return e_literalString();
    }

    private Token e_literalString_1() {
        return e_literalString();
    }

    private Token e_literalChar() {
        if (caracterActual == '\\') {
            consumir();
            return e_literalChar_1();
        }

        if (caracterActual != '\'') {
            consumir();
            return e_literalChar_2();
        }

        consumir();
        return guardarError();
    }

    private Token e_literalChar_1() {
        if (caracterActual != '\'') {
            consumir();
            return e_literalChar_2();
        }

        consumir();
        return guardarError();
    }

    private Token e_literalChar_2() {
        if (caracterActual == '\'') {
            consumir();
            return createToken(TokenType.charLiteral);
        }

        consumir();
        return guardarError();
    }

    private Token e_IdentificadorParametroTipoClase() {
        if (Character.isAlphabetic(caracterActual) || Character.isDigit(caracterActual)) {
            consumir();
            return e_IdentificadorClase();
        }

        return createToken(TokenType.IdentificadorDeParametroDeTipo);
    }

    private Token e_IdentificadorClase() {
        if (Character.isAlphabetic(caracterActual) || Character.isDigit(caracterActual)) {
            consumir();
            return e_IdentificadorClase();
        }

        return createToken(TokenType.identificadorDeClase);
    }

    private Token e_IdentificadorMetodoVariable() {
        if (Character.isAlphabetic(caracterActual) || Character.isDigit(caracterActual) || caracterActual == '_') {
            consumir();
            return e_IdentificadorMetodoVariable();
        }

        return createToken(TokenType.identificador);
    }

    private Token e_slash() {
        if (caracterActual == '/') {
            while (!SourceManager.isEOF(caracterActual) && caracterActual != '\n') {
                actualizarCaracterActual();
            }

            lexema = "";
            if (caracterActual == '\n') {
                actualizarCaracterActual();
            }
            return e0();
        }

        return guardarError();
    }

    private Token e_EOF() {
        lexema = "$";
        return createToken(TokenType.EOF);
    }

    private Token checkForPunctuation() {
        if (caracterActual == '(') {
            consumir();
            return createToken(TokenType.openParenthesis);
        }
        if (caracterActual == ')') {
            consumir();
            return createToken(TokenType.closeParenthesis);
        }
        if (caracterActual == '[') {
            consumir();
            return createToken(TokenType.openSquareBracket);
        }
        if (caracterActual == ']') {
            consumir();
            return createToken(TokenType.closeSquareBracket);
        }
        if (caracterActual == '{') {
            consumir();
            return createToken(TokenType.openBraces);
        }
        if (caracterActual == '}') {
            consumir();
            return createToken(TokenType.closeBraces);
        }
        if (caracterActual == ';') {
            consumir();
            return createToken(TokenType.semicolon);
        }
        if (caracterActual == ',') {
            consumir();
            return createToken(TokenType.comma);
        }
        if (caracterActual == '.') {
            consumir();
            return createToken(TokenType.period);
        }
        if (caracterActual == ':') {
            consumir();
            return createToken(TokenType.twopoints);
        }
        if (caracterActual == '+') {
            consumir();
            return createToken(TokenType.plus);
        }
        if (caracterActual == '=') {
            consumir();
            return createToken(TokenType.assignment);
        }
        if (caracterActual == '>') {
            consumir();
            return createToken(TokenType.greater);
        }
        return null;
    }

    private void consumir() {
        actualizarLexema();
        actualizarCaracterActual();
    }

    private Token createToken(TokenType tokenType) {
        return new Token(tokenType, lexema, SourceManager.getLineNumber());
    }
}
