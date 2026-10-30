///[Error:sinValor|6]
// No se puede retornar el resultado void de una llamada.
class Prueba {
    void sinValor() { }
    int obtener() {
        return sinValor();
    }
}
