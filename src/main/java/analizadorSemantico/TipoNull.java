package analizadorSemantico;

import analizadorLexico.Token;

public final class TipoNull extends Tipo {
    public TipoNull(Token token) { super(token); }
    @Override public String getNombre() { return "null"; }
}
