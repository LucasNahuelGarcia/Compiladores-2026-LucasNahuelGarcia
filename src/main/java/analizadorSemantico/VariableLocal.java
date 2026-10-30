package analizadorSemantico;

import analizadorLexico.Token;
import java.util.Objects;

public final class VariableLocal implements EntidadDeclarativa {
    private final Token token;
    private final String nombre;
    private final Tipo tipo;

    public VariableLocal(Token token, String nombre, Tipo tipo) {
        this.token = Objects.requireNonNull(token);
        this.nombre = Objects.requireNonNull(nombre);
        this.tipo = Objects.requireNonNull(tipo);
    }

    public Token getToken() { return token; }
    public String getNombre() { return nombre; }
    public Tipo getTipo() { return tipo; }
}
