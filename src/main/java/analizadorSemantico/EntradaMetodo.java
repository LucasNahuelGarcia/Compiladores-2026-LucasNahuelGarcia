package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import analizadorSemantico.ast.BloqueAst;

public class EntradaMetodo {
    private Token token;
    private String nombre;
    private Tipo tipoRetorno;
    private String visibilidad;
    private boolean estatico;
    private boolean finalMethod;
    private String parametroGenerico;
    private Map<String, List<Tipo>> parametrosGenericos;
    private Map<String, ParametroTipo> simbolosParametrosGenericos;
    private Map<String, EntradaParametro> parametros;
    private boolean invalido;
    private BloqueAst cuerpo;
    private EntradaClase claseDeclaracion;
    private EntradaMetodo declaracionOriginal;

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
        this.parametrosGenericos = new LinkedHashMap<>();
        this.simbolosParametrosGenericos = new LinkedHashMap<>();
    }

    public void agregarParametro(EntradaParametro parametro) {
        if (parametros.containsKey(parametro.getNombre())) {
            parametros.remove(parametro.getNombre());
            invalido = true;
            throw new ExcepcionSemantica(parametro.getToken(),
                    "el parametro '" + parametro.getNombre() + "' ya fue declarado");
        }
        parametros.put(parametro.getNombre(), parametro);
    }

    public boolean isInvalido() { return invalido; }

    public BloqueAst getCuerpo() { return cuerpo; }

    public void setCuerpo(BloqueAst cuerpo) { this.cuerpo = cuerpo; }
    public EntradaClase getClaseDeclaracion() { return claseDeclaracion; }
    void setClaseDeclaracion(EntradaClase claseDeclaracion) {
        if (this.claseDeclaracion == null) {
            this.claseDeclaracion = claseDeclaracion;
            this.declaracionOriginal = this;
        }
    }
    public EntradaMetodo getDeclaracionOriginal() {
        return declaracionOriginal == null ? this : declaracionOriginal;
    }

    public String getClaveFirma() {
        return nombre + "/" + parametros.size();
    }

    public String getClaveConstructor() {
        return String.valueOf(parametros.size());
    }

    public String getParametroGenerico() {
        return parametroGenerico;
    }

    public void setParametroGenerico(String parametroGenerico) {
        this.parametroGenerico = parametroGenerico;
        if (parametroGenerico != null) {
            parametrosGenericos.put(parametroGenerico, new ArrayList<>());
            simbolosParametrosGenericos.put(parametroGenerico,
                    new ParametroTipo(token, parametroGenerico, this));
        }
    }

    public void agregarParametroGenerico(String nombre, List<Tipo> bounds) {
        agregarParametroGenerico(new ParametroTipo(token, nombre, this), bounds);
    }

    public void agregarParametroGenerico(ParametroTipo parametro, List<Tipo> bounds) {
        String nombre = parametro.getNombre();
        if (parametrosGenericos.containsKey(nombre))
            throw new ExcepcionSemantica(token, "parametro generico de metodo repetido");
        parametrosGenericos.put(nombre, new ArrayList<>(bounds));
        simbolosParametrosGenericos.put(nombre, parametro);
        if (parametroGenerico == null)
            parametroGenerico = nombre;
    }

    public Map<String, List<Tipo>> getParametrosGenericos() {
        return Collections.unmodifiableMap(parametrosGenericos);
    }

    public Map<String, ParametroTipo> getSimbolosParametrosGenericos() {
        return Collections.unmodifiableMap(simbolosParametrosGenericos);
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
        estaBienDeclarado(tablaSimbolos, parametroGenerico, null);
    }

    void estaBienDeclarado(TablaSimbolos tablaSimbolos, String parametroGenerico,
                           ParametroTipo simboloParametroGenerico) {
        Set<String> alcance = new java.util.LinkedHashSet<>();
        if (parametroGenerico != null) alcance.add(parametroGenerico);
        alcance.addAll(parametrosGenericos.keySet());
        for (List<Tipo> bounds : parametrosGenericos.values())
            for (Tipo bound : bounds) {
                if (estatico && contieneParametro(bound, simboloParametroGenerico))
                    throw new ExcepcionSemantica(bound.getToken(),
                            "el parametro generico de clase no puede usarse en un metodo estatico");
                bound.estaBienDeclarado(tablaSimbolos, alcance);
            }
        if (estatico && contieneParametro(tipoRetorno, simboloParametroGenerico))
            throw new ExcepcionSemantica(tipoRetorno.getToken(),
                    "el parametro generico de clase no puede usarse en un metodo estatico");
        if (tipoRetorno != null)
            tipoRetorno.estaBienDeclarado(tablaSimbolos, alcance);
        for (EntradaParametro parametro : parametros.values()) {
            if (estatico && contieneParametro(parametro.getTipo(), simboloParametroGenerico))
                throw new ExcepcionSemantica(parametro.getTipo().getToken(),
                        "el parametro generico de clase no puede usarse en un metodo estatico");
            parametro.getTipo().estaBienDeclarado(tablaSimbolos, alcance);
        }
    }

    private boolean contieneParametro(Tipo tipo, ParametroTipo buscado) {
        if (tipo == null || buscado == null) return false;
        if (tipo instanceof TipoArreglo)
            return contieneParametro(((TipoArreglo) tipo).getTipoElemento(), buscado);
        if (!(tipo instanceof TipoReferencia)) return false;
        TipoReferencia referencia = (TipoReferencia) tipo;
        return referencia.getParametroTipo() == buscado
                || contieneParametro(referencia.getArgumentoGenerico(), buscado);
    }

    EntradaMetodo copiar(Map<ParametroTipo, Tipo> sustituciones) {
        EntradaMetodo copia = new EntradaMetodo(token, nombre,
                copiarTipo(tipoRetorno, sustituciones), visibilidad, estatico);
        copia.finalMethod = finalMethod;
        copia.parametroGenerico = parametroGenerico;
        copia.claseDeclaracion = claseDeclaracion;
        copia.declaracionOriginal = getDeclaracionOriginal();
        for (Map.Entry<String, List<Tipo>> entrada : parametrosGenericos.entrySet())
            copia.parametrosGenericos.put(entrada.getKey(), new ArrayList<>(entrada.getValue()));
        copia.simbolosParametrosGenericos.putAll(simbolosParametrosGenericos);
        for (EntradaParametro parametro : parametros.values())
            copia.agregarParametro(new EntradaParametro(
                    parametro.getToken(), parametro.getNombre(),
                    copiarTipo(parametro.getTipo(), sustituciones)));
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
