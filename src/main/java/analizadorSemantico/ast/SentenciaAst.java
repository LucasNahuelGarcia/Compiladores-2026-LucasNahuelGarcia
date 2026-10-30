package analizadorSemantico.ast;

import analizadorLexico.Token;

public abstract class SentenciaAst extends NodoAst {
    protected SentenciaAst(Token token) {
        super(token);
    }
}
