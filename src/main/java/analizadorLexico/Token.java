package src.main.java.analizadorLexico;

public class Token {
    private final TokenType tokenType;
    private final String lexema;
    private final int nroLinea;

    public Token(TokenType tokenType, String lexema, int nroLinea) {
        this.tokenType = tokenType;
        this.lexema = lexema;
        this.nroLinea = nroLinea;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getLexema() {
        return lexema;
    }

    public int getNroLinea() {
        return nroLinea;
    }

    public boolean isEOF() {
        return tokenType == TokenType.EOF;
    }

    @Override
    public String toString() {
        return "(" + tokenType + "," + lexema + "," + nroLinea + ")";
    }
}
