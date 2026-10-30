package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Objects;
import java.util.Map;

public class EntradaAtributo implements EntidadDeclarativa {
    private Token token;
    private String nombre;
    private Tipo tipo;
    private String visibilidad;
    private boolean estatico;
    private EntradaClase claseDeclaracion;
    private EntradaAtributo declaracionOriginal;

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

    public EntradaClase getClaseDeclaracion() { return claseDeclaracion; }

    void setClaseDeclaracion(EntradaClase claseDeclaracion) {
        if (this.claseDeclaracion == null) {
            this.claseDeclaracion = claseDeclaracion;
            this.declaracionOriginal = this;
        }
    }

    public EntradaAtributo getDeclaracionOriginal() {
        return declaracionOriginal == null ? this : declaracionOriginal;
    }

    public void setEstatico(boolean estatico) {
        this.estatico = estatico;
    }

    public void estaBienDeclarado(TablaSimbolos tablaSimbolos) {
        estaBienDeclarado(tablaSimbolos, null);
        }

        public void estaBienDeclarado(TablaSimbolos tablaSimbolos, String parametroGenerico) {
            if (estatico && tipo != null && tipo.usaParametroGenerico(parametroGenerico))
                throw new ExcepcionSemantica(tipo.getToken(), "el parametro generico no puede usarse en un atributo estatico");
        if (tipo != null)
            tipo.estaBienDeclarado(tablaSimbolos, parametroGenerico);
    }

    EntradaAtributo copiar(Map<ParametroTipo, Tipo> sustituciones) {
        EntradaAtributo copia = new EntradaAtributo(token, nombre, copiarTipo(tipo, sustituciones),
                visibilidad, estatico);
        copia.claseDeclaracion = claseDeclaracion;
        copia.declaracionOriginal = getDeclaracionOriginal();
        return copia;
    }

    private Tipo copiarTipo(Tipo tipo, Map<ParametroTipo, Tipo> sustituciones) {
        if (tipo == null)
            return null;
        if (tipo instanceof TipoPrimitivo)
            return new TipoPrimitivo(tipo.getToken(), ((TipoPrimitivo) tipo).getPrimitivo());
        if (tipo instanceof TipoArreglo)
            return new TipoArreglo(tipo.getToken(), copiarTipo(((TipoArreglo) tipo).getTipoElemento(), sustituciones));
        TipoReferencia referencia = (TipoReferencia) tipo;
        Tipo sustituto = referencia.getParametroTipo() == null
                ? null : sustituciones.get(referencia.getParametroTipo());
        if (sustituto != null && !esSustitucionIdentica(referencia, sustituto))
            return copiarTipo(sustituto, sustituciones);
        if (referencia.getParametroTipo() != null && referencia.getArgumentoGenerico() == null)
            return new TipoParametro(referencia.getToken(), referencia.getParametroTipo());
        TipoReferencia copia = new TipoReferencia(referencia.getToken(), referencia.getNombreClase(),
                copiarTipo(referencia.getArgumentoGenerico(), sustituciones));
        copia.setParametroTipo(referencia.getParametroTipo());
        return copia;
    }

    private boolean esSustitucionIdentica(TipoReferencia referencia, Tipo sustituto) {
        if (!(sustituto instanceof TipoReferencia))
            return false;
        TipoReferencia otra = (TipoReferencia) sustituto;
        return referencia.getParametroTipo() != null && otra.getParametroTipo() != null
                ? referencia.getParametroTipo() == otra.getParametroTipo()
                : referencia.getArgumentoGenerico() == null && otra.getArgumentoGenerico() == null
                    && referencia.getNombreClase().equals(otra.getNombreClase());
    }
}
