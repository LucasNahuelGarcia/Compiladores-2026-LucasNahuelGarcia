package analizadorSintactico;

import analizadorLexico.AnalizadorLexico;
import analizadorLexico.TokenType;
import analizadorSemantico.TablaSimbolos;

public final class Inicial {
    public TablaSimbolos parse(AnalizadorLexico lexer) {
        ContextoSintactico contexto = new ContextoSintactico(lexer);
        new ListaClases().parse(contexto);
        contexto.match(TokenType.EOF);
        return contexto.getTablaSimbolos();
    }
}
