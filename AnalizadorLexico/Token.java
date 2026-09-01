package AnalizadorLexico;

class Token {
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

    @Override
    public String toString() {
        return "Token{" +
                "tokenType=" + tokenType +
                ", lexema='" + lexema + '\'' +
                ", nroLinea=" + nroLinea +
                '}';
    }
}
