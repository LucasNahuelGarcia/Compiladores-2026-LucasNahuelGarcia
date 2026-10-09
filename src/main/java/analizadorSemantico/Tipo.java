package analizadorSemantico;

import analizadorLexico.Token;

import java.util.Objects;
import java.util.Set;

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
        estaBienDeclarado(tablaSimbolos, parametroGenerico == null
            ? java.util.Collections.emptySet()
            : java.util.Collections.singleton(parametroGenerico));
        }

        public void estaBienDeclarado(TablaSimbolos tablaSimbolos, Set<String> parametrosGenericos) {
    }

    public boolean usaParametroGenerico(String parametroGenerico) {
        return false;
    }

    public Token tokenDelParametro(String parametroGenerico) {
        return getToken();
    }
}