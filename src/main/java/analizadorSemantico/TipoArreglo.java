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
}