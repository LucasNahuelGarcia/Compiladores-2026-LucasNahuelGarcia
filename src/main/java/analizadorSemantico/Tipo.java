package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Objects;

public abstract class Tipo {
    private Token token;

    protected Tipo(Token token) {
        this.token = Objects.requireNonNull(token, "El token del tipo no puede ser null");
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = Objects.requireNonNull(token, "El token del tipo no puede ser null");
    }

    public abstract String getNombre();

    public void estaBienDeclarado(TablaSimbolos tablaSimbolos) {
        estaBienDeclarado(tablaSimbolos, null);
    }

    public void estaBienDeclarado(TablaSimbolos tablaSimbolos, String parametroGenerico) {
    }

    public boolean usaParametroGenerico(String parametroGenerico) {
        return false;
    }

    public Token tokenDelParametro(String parametroGenerico) {
        return getToken();
    }
}