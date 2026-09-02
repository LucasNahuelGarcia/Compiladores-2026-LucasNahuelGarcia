package analizadorLexico;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import sourcemanager.*;

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
            return proximoToken();
        }

        if (Character.isUpperCase(caracterActual)) {
            consumir();
            return e_IdentificadorParametroTipoClase();
        }

        if (Character.isLowerCase(caracterActual)) {
            consumir();
            return e_IdentificadorMetodoVariable();
        }

        if (caracterActual == '>') {
            consumir();
            return e_greaterThan();
        }

        if (caracterActual == '<') {
            consumir();
            return e_lessThan();
        }

        if (caracterActual == '!') {
            consumir();
            return e_not();
        }

        if (caracterActual == '=') {
            consumir();
            return e_assign();
        }
        if (caracterActual == '&') {
            consumir();
            return e_and();
        }
        if (caracterActual == '|') {
            consumir();
            return e_or();
        }

        if (caracterActual == '%') {
            consumir();
            return createToken(TokenType.mod);
        }

        if (caracterActual == '+') {
            consumir();
            return e_plus();
        }

        if (caracterActual == '-') {
            consumir();
            return e_minus();
        }

        if (caracterActual == '*') {
            consumir();
            return createToken(TokenType.multiply);
        }

        if (caracterActual == '/') {
            consumir();
            return e_slash();
        }

        Token punctuation = checkForPunctuation();
        if (punctuation != null)
            return punctuation;

        consumir();
        return guardarError("No es un caracter reconocido en el lenguaje.");
    }

    private Token e_greaterThan() {
        if (caracterActual == '=') {
            consumir();
            return createToken(TokenType.greaterThanOrEqual);
        }

        return createToken(TokenType.greaterThan);
    }

    private Token e_lessThan() {
        if (caracterActual == '=') {
            consumir();
            return createToken(TokenType.lessThanOrEqual);
        }

        return createToken(TokenType.lessThan);
    }

    private Token e_not() {
        if (caracterActual == '=') {
            consumir();
            return createToken(TokenType.notEquals);
        }

        return createToken(TokenType.not);
    }

    private Token e_assign() {
        if (caracterActual == '=') {
            consumir();
            return createToken(TokenType.equals);
        }

        return createToken(TokenType.assignment);
    }

    private Token e_and() {
        if (caracterActual == '&') {
            consumir();
            return createToken(TokenType.and);
        }

        return guardarError("Se esperaba '&' a continuación.");
    }

    private Token e_or() {
        if (caracterActual == '|') {
            consumir();
            return createToken(TokenType.or);
        }

        return guardarError("Se esperaba '|' a continuación.");
    }

    private Token e_plus() {
        if (caracterActual == '+') {
            consumir();
            return createToken(TokenType.addOne);
        }

        return createToken(TokenType.plus);
    }

    private Token e_minus() {
        if (caracterActual == '-') {
            consumir();
            return createToken(TokenType.subtractOne);
        }

        return createToken(TokenType.minus);
    }

    private Token guardarError(String explicacion) {
        errors.add(new Error(lexema, SourceManager.getLineNumber(), SourceManager.getColNumber(), explicacion));
        return proximoToken();
    }

    public boolean tieneErrores() {
        return !errors.isEmpty();
    }

    public void imprimirErrores() {
        for (Error error : errors) {
            String explicacion = "\nError Léxico en linea " + error.nroLinea + ": " + error.explicacion;
            explicacion = explicacion + '\n';
            explicacion = explicacion + SourceManager.getLine(error.nroLinea) + '\n';
            for (int i = 0; i < error.nroCol - 1; i++)
                explicacion = explicacion + ' ';
            explicacion = explicacion + '^';
            System.out.println(explicacion + "\n[Error:" + error.lexema + "|" + error.nroLinea + "]");
        }
    }

    private Token e_literalEntero() {
        return e_literalEntero(1);
    }

    private Token e_literalEntero(int currentLength) {
        if (Character.isDigit(caracterActual)) {
            consumir();
            if (currentLength >= _intLiteral_MaxLength)
                return guardarError("Un literal entero no puede exceder los 9 dígitos.");

            return e_literalEntero(currentLength + 1);
        }

        return createToken(TokenType.intLiteral);
    }

    private Token e_literalString() {
        if (SourceManager.isEOF(caracterActual) || caracterActual == '\n' || caracterActual == '\r') {
            return guardarError("String mal formado.");
        }

        if (caracterActual == '"') {
            consumir();
            return createToken(TokenType.stringLiteral);
        }

        if (caracterActual == '\\') {
            consumir();
            if (SourceManager.isEOF(caracterActual) || caracterActual == '\n') {
                return guardarError("Se esperaba un caracter luego de '\\'");
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
            return e_literalChar_1_scape();
        }

        if (caracterActual != '\'') {
            consumir();
            return e_literalChar_2_end();
        }

        consumir();
        return guardarError("Literal Char no puede estar vacio.");
    }

    private Token e_literalChar_1_scape() {
        if (SourceManager.isEOF(caracterActual)) {
            return guardarError("Fin del archivo inesperado.");
        }

        consumir();
        return e_literalChar_2_end();
    }

    private Token e_literalChar_2_end() {
        if (caracterActual == '\'') {
            consumir();
            return createToken(TokenType.charLiteral);
        }

        consumir();
        return guardarError("Se esperaba '''");
    }

    private Token e_IdentificadorParametroTipoClase() {
        if (Character.isAlphabetic(caracterActual) || Character.isDigit(caracterActual) || caracterActual == '_') {
            consumir();
            return e_IdentificadorClase();
        }

        return createToken(TokenType.IdentificadorDeParametroDeTipo);
    }

    private Token e_IdentificadorClase() {
        if (Character.isAlphabetic(caracterActual) || Character.isDigit(caracterActual) || caracterActual == '_') {
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

        Token palabraReservada = getPalabraReservadaFromLexema();
        if (palabraReservada != null)
            return palabraReservada;

        return createToken(TokenType.identificador);
    }

    private Token getPalabraReservadaFromLexema() {
        switch (lexema) {
            case "class":
                return createToken(TokenType.kw_class);
            case "extends":
                return createToken(TokenType.kw_extends);
            case "interface":
                return createToken(TokenType.kw_interface);
            case "implements":
                return createToken(TokenType.kw_implements);
            case "static":
                return createToken(TokenType.kw_static);
            case "boolean":
                return createToken(TokenType.kw_boolean);
            case "char":
                return createToken(TokenType.kw_char);
            case "int":
                return createToken(TokenType.kw_int);
            case "void":
                return createToken(TokenType.kw_void);
            case "public":
                return createToken(TokenType.kw_public);
            case "if":
                return createToken(TokenType.kw_if);
            case "else":
                return createToken(TokenType.kw_else);
            case "while":
                return createToken(TokenType.kw_while);
            case "return":
                return createToken(TokenType.kw_return);
            case "var":
                return createToken(TokenType.kw_var);
            case "this":
                return createToken(TokenType.kw_this);
            case "new":
                return createToken(TokenType.kw_new);
            case "null":
                return createToken(TokenType.kw_null);
            case "true":
                return createToken(TokenType.kw_true);
            case "false":
                return createToken(TokenType.kw_false);
            default:
                return null;
        }
    }

    private Token e_slash() {
        if (caracterActual == '/') {
            consumir();
            return e_inlineComment();
        }

        if (caracterActual == '*') {
            actualizarCaracterActual();
            return e_multiLineComment();
        }

        return createToken(TokenType.operatorSlash);
    }

    private Token e_inlineComment() {
        while (!SourceManager.isEOF(caracterActual) && caracterActual != '\n')
            consumir();

        if (caracterActual == '\n') {
            consumir();
        }
        return proximoToken();
    }

    private Token e_multiLineComment() {
        if (caracterActual == '*') {
            actualizarCaracterActual();
            return e_multiLineComment_1();
        }

        actualizarCaracterActual();
        System.out.println(caracterActual);
        return e_multiLineComment();
    }

    private Token e_multiLineComment_1() {
        if (caracterActual == '/') {
            actualizarCaracterActual();
            return proximoToken();
        }
        actualizarCaracterActual();
        return e_multiLineComment();
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
