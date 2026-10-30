package analizadorSemantico;

import analizadorLexico.Token;
import java.util.Objects;

/** A generic parameter identified by its declaration, not by its spelling. */
public final class ParametroTipo {
    private final Token token;
    private final String nombre;
    private final Object declaracionOrigen;

    public ParametroTipo(Token token, String nombre, Object declaracionOrigen) {
        this.token = Objects.requireNonNull(token, "El token del parametro de tipo no puede ser null");
        this.nombre = Objects.requireNonNull(nombre, "El nombre del parametro de tipo no puede ser null");
        this.declaracionOrigen = Objects.requireNonNull(declaracionOrigen,
                "La declaracion de origen del parametro de tipo no puede ser null");
    }

    public Token getToken() { return token; }
    public String getNombre() { return nombre; }
    public Object getDeclaracionOrigen() { return declaracionOrigen; }
}
