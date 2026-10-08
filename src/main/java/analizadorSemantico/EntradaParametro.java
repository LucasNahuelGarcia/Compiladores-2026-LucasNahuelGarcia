package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Objects;

public class EntradaParametro {
    private Token token;
    private String nombre;
    private Tipo tipo;

    public EntradaParametro(Token token, String nombre, Tipo tipo) {
        this.token = Objects.requireNonNull(token, "El token del parametro no puede ser null");
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = Objects.requireNonNull(token, "El token del parametro no puede ser null");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
}