package analizadorLexico;

public class ExcepcionLexica extends RuntimeException {

    public ExcepcionLexica(String lexema, int nroLinea) {
        super("Error Lexico en linea " + nroLinea + ": lexema \u201c"
                + lexema + "\u201d\n[Error:" + lexema + "|" + nroLinea + "]");
    }
}
