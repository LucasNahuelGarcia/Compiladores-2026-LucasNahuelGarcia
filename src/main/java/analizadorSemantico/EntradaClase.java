package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class EntradaClase {
    private Token token;
    private String nombre;
    private TipoReferencia claseBase;
    private Map<String, EntradaAtributo> atributos;
    private Map<String, EntradaMetodo> metodos;

    public EntradaClase(Token token, String nombre) {
        this.token = Objects.requireNonNull(token, "El token de la clase no puede ser null");
        this.nombre = nombre;
        this.atributos = new LinkedHashMap<>();
        this.metodos = new LinkedHashMap<>();
    }

    public EntradaClase(Token token) {
        this(token, token.getLexema());
    }

    public void agregarAtributo(EntradaAtributo atributo) {
        atributos.put(atributo.getNombre(), atributo);
    }

    public void agregarMetodo(EntradaMetodo metodo) {
        metodos.put(metodo.getNombre(), metodo);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = Objects.requireNonNull(token, "El token de la clase no puede ser null");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoReferencia getClaseBase() {
        return claseBase;
    }

    public void setClaseBase(TipoReferencia claseBase) {
        this.claseBase = claseBase;
    }

    public Map<String, EntradaAtributo> getAtributos() {
        return Collections.unmodifiableMap(atributos);
    }

    public void setAtributos(Map<String, EntradaAtributo> atributos) {
        this.atributos = new LinkedHashMap<>(atributos);
    }

    public Map<String, EntradaMetodo> getMetodos() {
        return Collections.unmodifiableMap(metodos);
    }

    public void setMetodos(Map<String, EntradaMetodo> metodos) {
        this.metodos = new LinkedHashMap<>(metodos);
    }
}