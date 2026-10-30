package analizadorSemantico;

import analizadorLexico.Token;

public final class TipoVoid extends Tipo {
    public TipoVoid(Token token) { super(token); }
    @Override public String getNombre() { return "void"; }
}
