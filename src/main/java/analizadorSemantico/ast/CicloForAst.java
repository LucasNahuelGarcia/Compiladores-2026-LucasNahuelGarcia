package analizadorSemantico.ast;

import analizadorLexico.Token;
import analizadorSemantico.Tipo;

/** Represents both classic and enhanced for statements. */
public final class CicloForAst extends SentenciaAst {
    public enum Forma { CLASICO, MEJORADO }

    private final Forma forma;
    private final Tipo tipoVariable;
    private final Token nombreVariable;
    private final ExpresionAst inicializacion;
    private final ExpresionAst condicion;
    private final ExpresionAst actualizacion;
    private final ExpresionAst iterable;
    private final SentenciaAst cuerpo;

    public CicloForAst(Token token, Forma forma, Tipo tipoVariable, Token nombreVariable,
                       ExpresionAst inicializacion, ExpresionAst condicion,
                       ExpresionAst actualizacion, ExpresionAst iterable, SentenciaAst cuerpo) {
        super(token);
        this.forma = forma;
        this.tipoVariable = tipoVariable;
        this.nombreVariable = nombreVariable;
        this.inicializacion = inicializacion;
        this.condicion = condicion;
        this.actualizacion = actualizacion;
        this.iterable = iterable;
        this.cuerpo = cuerpo;
    }

    public Forma getForma() { return forma; }
    public Tipo getTipoVariable() { return tipoVariable; }
    public Token getNombreVariable() { return nombreVariable; }
    public ExpresionAst getInicializacion() { return inicializacion; }
    public ExpresionAst getCondicion() { return condicion; }
    public ExpresionAst getActualizacion() { return actualizacion; }
    public ExpresionAst getIterable() { return iterable; }
    public SentenciaAst getCuerpo() { return cuerpo; }
}
