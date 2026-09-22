package analizadorSintactico;

import analizadorLexico.AnalizadorLexico;
import analizadorLexico.TokenType;

public final class Inicial {
    public void parse(AnalizadorLexico lexer) {
        ContextoSintactico contexto = new ContextoSintactico(lexer);
        new ListaClases().parse(contexto);
        contexto.match(TokenType.EOF);
    }
}
