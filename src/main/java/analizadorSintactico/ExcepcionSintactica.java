package analizadorSintactico;

import analizadorLexico.Token;

public class ExcepcionSintactica extends RuntimeException {
    private final Token token;

    ExcepcionSintactica(Token token, String esperado) {
        super("Error Sintactico en linea " + token.getNroLinea()
                + ": se esperaba " + esperado + " se encontro \u201c"
                + token.getLexema() + "\u201d\n[Error:" + token.getLexema()
                + "|" + token.getNroLinea() + "]");
        this.token = token;
    }

    public Token getToken() { return token; }
}
