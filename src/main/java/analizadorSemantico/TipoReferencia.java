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
        if (!nombreClase.equals(parametroGenerico) && tablaSimbolos.getClase(nombreClase) == null)
            throw new ExcepcionSemantica(getToken(),
                    "el tipo '" + nombreClase + "' no fue declarado");
        if (argumentoGenerico != null)
            argumentoGenerico.estaBienDeclarado(tablaSimbolos, parametroGenerico);
    }
}