package analizadorSemantico;

import analizadorLexico.Token;

public class TipoReferencia extends Tipo {
    private String nombreClase;
    private Tipo argumentoGenerico;

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

    @Override
    public String getNombre() {
        return nombreClase;
    }

    @Override
    public void estaBienDeclarado(TablaSimbolos tablaSimbolos) {
        estaBienDeclarado(tablaSimbolos, null);
    }

    @Override
    public void estaBienDeclarado(TablaSimbolos tablaSimbolos, String parametroGenerico) {
        EntradaClase clase = tablaSimbolos.getClase(nombreClase);
        if (!nombreClase.equals(parametroGenerico) && clase == null)
            throw new ExcepcionSemantica(getToken(),
                    "el tipo '" + nombreClase + "' no fue declarado");
        if (argumentoGenerico != null && clase != null && clase.getParametroGenerico() == null)
            throw new ExcepcionSemantica(getToken(),
                    "la clase '" + nombreClase + "' no es parametrizada");
        if (argumentoGenerico != null)
            argumentoGenerico.estaBienDeclarado(tablaSimbolos, parametroGenerico);
    }

    @Override
    public boolean usaParametroGenerico(String parametroGenerico) {
        return nombreClase.equals(parametroGenerico)
                || argumentoGenerico != null && argumentoGenerico.usaParametroGenerico(parametroGenerico);
    }

    @Override
    public Token tokenDelParametro(String parametroGenerico) {
        if (nombreClase.equals(parametroGenerico))
            return getToken();
        return argumentoGenerico == null ? getToken() : argumentoGenerico.tokenDelParametro(parametroGenerico);
    }
}