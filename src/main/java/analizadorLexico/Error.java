package src.main.java.analizadorLexico;

class Error {
    String lexema;
    int nroLinea;

    public Error(String lexema, int nroLinea) {
        this.lexema = lexema;
        this.nroLinea = nroLinea;
    }
}