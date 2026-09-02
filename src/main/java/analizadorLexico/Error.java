package analizadorLexico;

class Error {
    String lexema;
    int nroLinea;
    int nroCol;
    String explicacion;
    String linea;

    public Error(String lexema, int nroLinea, int columnaActual, String linea, String explicacion) {
        this.lexema = lexema;
        this.nroLinea = nroLinea;
        this.nroCol = columnaActual;
        this.explicacion = explicacion;
    }
}