package analizadorSemantico;

import analizadorLexico.Token;

public class TipoArreglo extends Tipo {
    private Tipo tipoElemento;

    public TipoArreglo(Token token, Tipo tipoElemento) {
        super(token);
        this.tipoElemento = tipoElemento;
    }

    public Tipo getTipoElemento() {
        return tipoElemento;
    }

    public void setTipoElemento(Tipo tipoElemento) {
        this.tipoElemento = tipoElemento;
    }

    @Override
    public String getNombre() {
        return tipoElemento.getNombre() + "[]";
    }

    @Override
    public void estaBienDeclarado(TablaSimbolos tablaSimbolos) {
        estaBienDeclarado(tablaSimbolos, null);
    }

    @Override
    public void estaBienDeclarado(TablaSimbolos tablaSimbolos, String parametroGenerico) {
        tipoElemento.estaBienDeclarado(tablaSimbolos, parametroGenerico);
    }

    @Override
    public boolean usaParametroGenerico(String parametroGenerico) {
        return tipoElemento.usaParametroGenerico(parametroGenerico);
    }

    @Override
    public Token tokenDelParametro(String parametroGenerico) {
        return tipoElemento.tokenDelParametro(parametroGenerico);
    }
}