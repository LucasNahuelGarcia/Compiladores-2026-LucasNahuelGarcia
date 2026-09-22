package analizadorSintactico;

import analizadorLexico.AnalizadorLexico;
import analizadorLexico.ExcepcionLexica;
import analizadorLexico.Token;
import analizadorLexico.TokenType;

public final class ContextoSintactico {
    private final AnalizadorLexico lexer;
    private Token actual;

    public ContextoSintactico(AnalizadorLexico lexer) {
        this.lexer = lexer;
        avanzar();
    }

    public Token actual() { return actual; }
    public boolean es(TokenType tipo) { return actual.getTokenType() == tipo; }
    public boolean fin() { return es(TokenType.EOF); }

    public void match(TokenType esperado) throws ExcepcionSintactica {
        if (!es(esperado)) throw new ExcepcionSintactica(actual, esperado.toString());
        avanzar();
    }

    private void avanzar() {
        actual = lexer.proximoToken();
        if (lexer.tieneErrores()) throw new ExcepcionLexica(actual.getLexema(), actual.getNroLinea());
    }

    public static boolean cualquiera(ContextoSintactico c, TokenType... tipos) {
        for (TokenType tipo : tipos) if (c.es(tipo)) return true;
        return false;
    }
}
