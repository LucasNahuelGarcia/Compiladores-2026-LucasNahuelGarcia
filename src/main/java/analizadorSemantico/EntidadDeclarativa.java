package analizadorSemantico;

import analizadorLexico.Token;

/** A symbol-table declaration that an AST node can bind to directly. */
public interface EntidadDeclarativa {
    Token getToken();
    String getNombre();
}
