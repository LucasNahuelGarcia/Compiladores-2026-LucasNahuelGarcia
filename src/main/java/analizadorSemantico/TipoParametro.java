package analizadorSemantico;

import analizadorLexico.Token;

/** A type variable use bound to its declaring class or method parameter. */
public final class TipoParametro extends TipoReferencia {
    public TipoParametro(Token token, ParametroTipo parametro) {
        super(token, parametro.getNombre());
        setParametroTipo(parametro);
    }

    public ParametroTipo getParametro() {
        return getParametroTipo();
    }
}
