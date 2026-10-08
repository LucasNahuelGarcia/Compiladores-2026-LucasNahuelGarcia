package analizadorSemantico;

import analizadorLexico.Token;

public class TipoReferencia extends Tipo {
    private String nombreClase;

    public TipoReferencia(Token token, String nombreClase) {
        super(token);
        this.nombreClase = nombreClase;
    }

    public String getNombreClase() {
        return nombreClase;
    }

    public void setNombreClase(String nombreClase) {
        this.nombreClase = nombreClase;
    }

    @Override
    public String getNombre() {
        return nombreClase;
    }
}