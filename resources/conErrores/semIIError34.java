///[Error:aceptar|7]
// Un argumento void invalida la llamada.
class Prueba {
    void aceptar(int numero) { }
    void sinValor() { }
    void probar() {
        aceptar(sinValor());
    }
}
