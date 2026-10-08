package analizadorSemantico;

import analizadorLexico.Token;

public class TipoPrimitivo extends Tipo {
    public enum Primitivo {
        BOOLEAN,
        CHAR,
        INT,
        VOID
    }

    private Primitivo primitivo;

    public TipoPrimitivo(Token token, Primitivo primitivo) {
        super(token);
        this.primitivo = primitivo;
    }

    public Primitivo getPrimitivo() {
        return primitivo;
    }

    public void setPrimitivo(Primitivo primitivo) {
        this.primitivo = primitivo;
    }

    @Override
    public String getNombre() {
        return primitivo.name().toLowerCase();
    }
}