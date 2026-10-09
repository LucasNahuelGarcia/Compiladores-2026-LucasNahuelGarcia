package analizadorSintactico;

import analizadorLexico.AnalizadorLexico;
import analizadorLexico.ExcepcionLexica;
import analizadorLexico.Token;
import analizadorLexico.TokenType;
import analizadorSemantico.EntradaClase;
import analizadorSemantico.EntradaMetodo;
import analizadorSemantico.TablaSimbolos;

public final class ContextoSintactico {
    private final AnalizadorLexico lexer;
    private final TablaSimbolos tablaSimbolos;
    private Token actual;
    private EntradaClase claseActual;
    private EntradaMetodo metodoActual;
    private String visibilidadActual = "public";

    public ContextoSintactico(AnalizadorLexico lexer) {
        this(lexer, new TablaSimbolos());
    }

    public ContextoSintactico(AnalizadorLexico lexer, TablaSimbolos tablaSimbolos) {
        this.lexer = lexer;
        this.tablaSimbolos = tablaSimbolos;
        avanzar();
    }

    public Token actual() { return actual; }
    public boolean es(TokenType tipo) { return actual.getTokenType() == tipo; }
    public boolean fin() { return es(TokenType.EOF); }

    public Token match(TokenType esperado) throws ExcepcionSintactica {
        if (!es(esperado)) throw new ExcepcionSintactica(actual, esperado.toString());
        Token consumido = actual;
        avanzar();
        return consumido;
    }

    public TablaSimbolos getTablaSimbolos() { return tablaSimbolos; }
    public EntradaClase getClaseActual() { return claseActual; }
    public void setClaseActual(EntradaClase claseActual) { this.claseActual = claseActual; }
    public EntradaMetodo getMetodoActual() { return metodoActual; }
    public void setMetodoActual(EntradaMetodo metodoActual) { this.metodoActual = metodoActual; }
    public String getVisibilidadActual() { return visibilidadActual; }
    public void setVisibilidadActual(String visibilidadActual) { this.visibilidadActual = visibilidadActual; }

    private void avanzar() {
        actual = lexer.proximoToken();
        if (lexer.tieneErrores()) throw new ExcepcionLexica(actual.getLexema(), actual.getNroLinea());
    }

    public static boolean cualquiera(ContextoSintactico c, TokenType... tipos) {
        for (TokenType tipo : tipos) if (c.es(tipo)) return true;
        return false;
    }
}
