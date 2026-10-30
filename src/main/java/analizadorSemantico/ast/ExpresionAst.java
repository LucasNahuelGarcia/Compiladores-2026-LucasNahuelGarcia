package analizadorSemantico.ast;

import analizadorLexico.Token;
import analizadorSemantico.Tipo;

/** Semantic attributes calculated after parsing the expression tree. */
public abstract class ExpresionAst extends NodoAst {
    private Tipo tipo;
    private boolean asignable;
    private boolean llamadaValida;

    protected ExpresionAst(Token token) {
        super(token);
    }

    public final Tipo getTipo() {
        return tipo;
    }

    public final void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public final boolean esAsignable() {
        return asignable;
    }

    public final void setAsignable(boolean asignable) {
        this.asignable = asignable;
    }

    public final boolean esLlamadaValida() {
        return llamadaValida;
    }

    public final void setLlamadaValida(boolean llamadaValida) {
        this.llamadaValida = llamadaValida;
    }
}
