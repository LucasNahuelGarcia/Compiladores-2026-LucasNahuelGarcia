package analizadorLexico;

class Error {
    String lexema;
    int nroLinea;
    String explicacion;

    public Error(String lexema, int nroLinea, String explicacion) {
        this.lexema = lexema;
        this.nroLinea = nroLinea;
        this.explicacion = explicacion;
    }
}