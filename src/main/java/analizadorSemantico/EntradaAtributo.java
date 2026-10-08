package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Objects;

public class EntradaAtributo {
    private Token token;
    private String nombre;
    private Tipo tipo;
    private String visibilidad;
    private boolean estatico;

    public EntradaAtributo(Token token, String nombre, Tipo tipo) {
        this(token, nombre, tipo, "public", false);
    }

    public EntradaAtributo(Token token, String nombre, Tipo tipo, String visibilidad, boolean estatico) {
        this.token = Objects.requireNonNull(token, "El token del atributo no puede ser null");
        this.nombre = nombre;
        this.tipo = tipo;
        this.visibilidad = visibilidad;
        this.estatico = estatico;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = Objects.requireNonNull(token, "El token del atributo no puede ser null");
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

    public String getVisibilidad() {
        return visibilidad;
    }

    public void setVisibilidad(String visibilidad) {
        this.visibilidad = visibilidad;
    }

    public boolean isEstatico() {
        return estatico;
    }

    public void setEstatico(boolean estatico) {
        this.estatico = estatico;
    }
}