package analizadorSemantico.ast;

import analizadorLexico.Token;
import analizadorSemantico.Tipo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A syntax expression node; semantic attributes live in ExpresionAst. */
public final class NodoExpresionAst extends ExpresionAst {
    public enum Clase {
        LITERAL_INT, LITERAL_CHAR, LITERAL_BOOLEAN, LITERAL_STRING, LITERAL_NULL,
        THIS, VARIABLE, LLAMADA_IMPLICITA, LLAMADA_MIEMBRO, LLAMADA_ESTATICA,
        ACCESO_ATRIBUTO, ACCESO_INDICE, NUEVO_OBJETO, NUEVO_ARREGLO,
        PARENTESIS, UNARIO, BINARIO, ASIGNACION, INCREMENTO
    }

    private final Clase clase;
    private final Token nombre;
    private final Tipo tipoEscrito;
    private final String valor;
    private final boolean postfix;
    private final List<ExpresionAst> operandos;

    public NodoExpresionAst(Token token, Clase clase, Token nombre, Tipo tipoEscrito,
                            String valor, boolean postfix, List<ExpresionAst> operandos) {
        super(token);
        this.clase = clase;
        this.nombre = nombre;
        this.tipoEscrito = tipoEscrito;
        this.valor = valor;
        this.postfix = postfix;
        this.operandos = Collections.unmodifiableList(new ArrayList<>(operandos));
    }

    public Clase getClase() { return clase; }
    public Token getNombre() { return nombre; }
    public Tipo getTipoEscrito() { return tipoEscrito; }
    public String getValor() { return valor; }
    public boolean esPostfix() { return postfix; }
    public List<ExpresionAst> getOperandos() { return operandos; }
}
