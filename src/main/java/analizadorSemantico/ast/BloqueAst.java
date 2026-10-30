package analizadorSemantico.ast;

import analizadorLexico.Token;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BloqueAst extends SentenciaAst {
    private final List<SentenciaAst> sentencias;

    public BloqueAst(Token token, List<SentenciaAst> sentencias) {
        super(token);
        this.sentencias = Collections.unmodifiableList(new ArrayList<>(sentencias));
    }

    public List<SentenciaAst> getSentencias() {
        return sentencias;
    }
}
