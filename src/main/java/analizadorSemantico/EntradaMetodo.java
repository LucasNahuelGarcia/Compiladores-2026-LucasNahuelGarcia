package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class EntradaMetodo {
    private Token token;
    private String nombre;
    private Tipo tipoRetorno;
    private String visibilidad;
    private boolean estatico;
    private Map<String, EntradaParametro> parametros;

    public EntradaMetodo(Token token, String nombre, Tipo tipoRetorno) {
        this(token, nombre, tipoRetorno, "public", false);
    }

    public EntradaMetodo(Token token, String nombre, Tipo tipoRetorno, String visibilidad, boolean estatico) {
        this.token = Objects.requireNonNull(token, "El token del metodo no puede ser null");
        this.nombre = nombre;
        this.tipoRetorno = tipoRetorno;
        this.visibilidad = visibilidad;
        this.estatico = estatico;
        this.parametros = new LinkedHashMap<>();
    }

    public void agregarParametro(EntradaParametro parametro) {
        parametros.put(parametro.getNombre(), parametro);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = Objects.requireNonNull(token, "El token del metodo no puede ser null");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Tipo getTipoRetorno() {
        return tipoRetorno;
    }

    public void setTipoRetorno(Tipo tipoRetorno) {
        this.tipoRetorno = tipoRetorno;
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

    public Map<String, EntradaParametro> getParametros() {
        return Collections.unmodifiableMap(parametros);
    }

    public void setParametros(Map<String, EntradaParametro> parametros) {
        this.parametros = new LinkedHashMap<>(parametros);
    }
}