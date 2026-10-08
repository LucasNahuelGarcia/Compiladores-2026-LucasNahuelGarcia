package analizadorSemantico;

import analizadorLexico.Token;

public class ExcepcionSemantica extends RuntimeException {
    private final Token token;

    public ExcepcionSemantica(Token token, String mensaje) {
        super(mensaje);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}