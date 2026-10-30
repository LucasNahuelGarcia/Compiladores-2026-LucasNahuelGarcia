package analizadorSemantico.ast;

import analizadorLexico.Token;
import java.util.Objects;

/** Base node for the syntax tree retained for semantic body analysis. */
public abstract class NodoAst {
    private final Token token;

    protected NodoAst(Token token) {
        this.token = Objects.requireNonNull(token, "El token del nodo AST no puede ser null");
    }

    public final Token getToken() {
        return token;
    }
}
