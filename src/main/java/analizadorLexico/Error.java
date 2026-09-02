package analizadorLexico;

class Error {
    String lexema;
    int nroLinea;
    int nroCol;
    String explicacion;

    public Error(String lexema, int nroLinea, int columnaActual, String explicacion) {
        this.lexema = lexema;
        this.nroLinea = nroLinea;
        this.nroCol = columnaActual;
        this.explicacion = explicacion;
    }
}