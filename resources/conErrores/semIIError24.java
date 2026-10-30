///[Error:valor|6]
// Un atributo de instancia no puede usarse implicitamente desde un metodo estatico.
class Prueba {
    int valor;
    static int obtener() {
        return valor;
    }
}
