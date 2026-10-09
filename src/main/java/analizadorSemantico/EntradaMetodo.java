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
    private boolean finalMethod;
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
        if (parametros.containsKey(parametro.getNombre()))
            throw new ExcepcionSemantica(parametro.getToken(),
                    "el parametro '" + parametro.getNombre() + "' ya fue declarado");
        parametros.put(parametro.getNombre(), parametro);
    }

    public String getClaveFirma() {
        return nombre + "/" + parametros.size();
    }

    public String getClaveConstructor() {
        return String.valueOf(parametros.size());
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

    public boolean isFinalMethod() {
        return finalMethod;
    }

    public void setFinalMethod(boolean finalMethod) {
        this.finalMethod = finalMethod;
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

    public void estaBienDeclarado(TablaSimbolos tablaSimbolos) {
        estaBienDeclarado(tablaSimbolos, null);
    }

    public void estaBienDeclarado(TablaSimbolos tablaSimbolos, String parametroGenerico) {
        if (estatico && tipoRetorno != null && tipoRetorno.usaParametroGenerico(parametroGenerico))
            throw new ExcepcionSemantica(tipoRetorno.tokenDelParametro(parametroGenerico),
                    "el parametro generico no puede usarse en un metodo estatico");
        if (tipoRetorno != null)
            tipoRetorno.estaBienDeclarado(tablaSimbolos, parametroGenerico);
        for (EntradaParametro parametro : parametros.values()) {
            if (estatico && parametro.getTipo().usaParametroGenerico(parametroGenerico))
                throw new ExcepcionSemantica(parametro.getTipo().tokenDelParametro(parametroGenerico),
                        "el parametro generico no puede usarse en un metodo estatico");
            parametro.estaBienDeclarado(tablaSimbolos, parametroGenerico);
        }
    }

    EntradaMetodo copiar(Map<String, Tipo> sustituciones) {
        EntradaMetodo copia = new EntradaMetodo(token, nombre,
                copiarTipo(tipoRetorno, sustituciones), visibilidad, estatico);
        copia.finalMethod = finalMethod;
        for (EntradaParametro parametro : parametros.values())
            copia.agregarParametro(new EntradaParametro(
                    parametro.getToken(), parametro.getNombre(),
                    copiarTipo(parametro.getTipo(), sustituciones)));
        return copia;
    }

    private Tipo copiarTipo(Tipo tipo, Map<String, Tipo> sustituciones) {
        if (tipo == null)
            return null;
        if (tipo instanceof TipoPrimitivo)
            return new TipoPrimitivo(tipo.getToken(), ((TipoPrimitivo) tipo).getPrimitivo());
        if (tipo instanceof TipoArreglo)
            return new TipoArreglo(tipo.getToken(), copiarTipo(((TipoArreglo) tipo).getTipoElemento(), sustituciones));
        TipoReferencia referencia = (TipoReferencia) tipo;
        Tipo sustituto = sustituciones.get(referencia.getNombreClase());
        if (sustituto != null)
            return copiarTipo(sustituto, sustituciones);
        return new TipoReferencia(referencia.getToken(), referencia.getNombreClase(),
                copiarTipo(referencia.getArgumentoGenerico(), sustituciones));
    }
}