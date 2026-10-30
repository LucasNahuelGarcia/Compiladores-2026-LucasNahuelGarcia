package analizadorSemantico.ast;

import analizadorLexico.Token;
import analizadorSemantico.EntidadDeclarativa;
import java.util.Objects;

/** Base node for the syntax tree retained for semantic body analysis. */
public abstract class NodoAst {
    private final Token token;
    private EntidadDeclarativa entidadVinculada;

    protected NodoAst(Token token) {
        this.token = Objects.requireNonNull(token, "El token del nodo AST no puede ser null");
    }

    public final Token getToken() {
        return token;
    }

    public final EntidadDeclarativa getEntidadVinculada() {
        return entidadVinculada;
    }

    /** Binds this syntax node to the declaration selected during semantic analysis. */
    public final void vincularEntidad(EntidadDeclarativa entidad) {
        Objects.requireNonNull(entidad, "La entidad vinculada no puede ser null");
        if (entidadVinculada != null && entidadVinculada != entidad)
            throw new IllegalStateException("El nodo AST ya esta vinculado a otra entidad");
        entidadVinculada = entidad;
    }
}
