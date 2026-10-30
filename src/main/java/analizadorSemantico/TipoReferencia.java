package analizadorSemantico;

import analizadorLexico.Token;
import java.util.Set;

public class TipoReferencia extends Tipo {
    private String nombreClase;
    private Tipo argumentoGenerico;
    private ParametroTipo parametroTipo;

    public TipoReferencia(Token token, String nombreClase) {
        super(token);
        this.nombreClase = nombreClase;
    }

    public TipoReferencia(Token token, String nombreClase, Tipo argumentoGenerico) {
        this(token, nombreClase);
        this.argumentoGenerico = argumentoGenerico;
    }

    public String getNombreClase() {
        return nombreClase;
    }

    public void setNombreClase(String nombreClase) {
        this.nombreClase = nombreClase;
    }

    public Tipo getArgumentoGenerico() {
        return argumentoGenerico;
    }

    public void setArgumentoGenerico(Tipo argumentoGenerico) {
        this.argumentoGenerico = argumentoGenerico;
    }

    public ParametroTipo getParametroTipo() { return parametroTipo; }

    public void setParametroTipo(ParametroTipo parametroTipo) {
        this.parametroTipo = parametroTipo;
    }

    public boolean esParametroTipo() { return parametroTipo != null; }

    @Override
    public String getNombre() {
        return nombreClase;
    }

    @Override
    public void estaBienDeclarado(TablaSimbolos tablaSimbolos) {
        estaBienDeclarado(tablaSimbolos, (String) null);
    }

    @Override
    public void estaBienDeclarado(TablaSimbolos tablaSimbolos, String parametroGenerico) {
        estaBienDeclarado(tablaSimbolos, parametroGenerico == null
                ? java.util.Collections.emptySet()
                : java.util.Collections.singleton(parametroGenerico));
    }

    @Override
    public void estaBienDeclarado(TablaSimbolos tablaSimbolos, Set<String> parametrosGenericos) {
        EntradaClase clase = tablaSimbolos.getClase(nombreClase);
        if (parametroTipo == null && !parametrosGenericos.contains(nombreClase) && clase == null)
            throw new ExcepcionSemantica(getToken(),
                    "el tipo '" + nombreClase + "' no fue declarado");
        if (argumentoGenerico != null && clase != null && clase.getParametroGenerico() == null)
            throw new ExcepcionSemantica(getToken(),
                    "la clase '" + nombreClase + "' no es parametrizada");
        if (argumentoGenerico != null)
            argumentoGenerico.estaBienDeclarado(tablaSimbolos, parametrosGenericos);
    }

    @Override
    public boolean usaParametroGenerico(String parametroGenerico) {
        return (parametroTipo == null ? nombreClase.equals(parametroGenerico)
                    : parametroTipo.getNombre().equals(parametroGenerico))
                || argumentoGenerico != null && argumentoGenerico.usaParametroGenerico(parametroGenerico);
    }

    @Override
    public Token tokenDelParametro(String parametroGenerico) {
        if (parametroTipo == null ? nombreClase.equals(parametroGenerico)
                : parametroTipo.getNombre().equals(parametroGenerico))
            return parametroTipo == null ? getToken() : parametroTipo.getToken();
        return argumentoGenerico == null ? getToken() : argumentoGenerico.tokenDelParametro(parametroGenerico);
    }
}
