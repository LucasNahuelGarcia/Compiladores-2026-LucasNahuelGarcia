package analizadorSemantico.ast;

import analizadorLexico.Token;
import analizadorSemantico.Tipo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A statement node. Expressions and nested statements retain source order. */
public final class NodoSentenciaAst extends SentenciaAst {
    public enum Clase {
        VACIA, EXPRESION, VARIABLE_LOCAL, RETURN, IF, WHILE, FOR, FOR_EACH
    }

    private final Clase clase;
    private final Token nombre;
    private final Tipo tipoDeclarado;
    private final List<ExpresionAst> expresiones;
    private final List<SentenciaAst> sentencias;

    public NodoSentenciaAst(Token token, Clase clase, Token nombre, Tipo tipoDeclarado,
                            List<ExpresionAst> expresiones, List<SentenciaAst> sentencias) {
        super(token);
        this.clase = clase;
        this.nombre = nombre;
        this.tipoDeclarado = tipoDeclarado;
        this.expresiones = Collections.unmodifiableList(new ArrayList<>(expresiones));
        this.sentencias = Collections.unmodifiableList(new ArrayList<>(sentencias));
    }

    public Clase getClase() { return clase; }
    public Token getNombre() { return nombre; }
    public Tipo getTipoDeclarado() { return tipoDeclarado; }
    public List<ExpresionAst> getExpresiones() { return expresiones; }
    public List<SentenciaAst> getSentencias() { return sentencias; }
}
