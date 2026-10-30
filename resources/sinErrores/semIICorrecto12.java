// Las ramas con el mismo parametro generico declarado en su origen son identicas.
class Base<T> {
    T obtener() { return null; }
}
class Derivada<U> extends Base<U> {
    void probar(Derivada<U> otra) {
        var primero = obtener();
        var segundo = otra.obtener();
        var iguales = primero == segundo;
    }
}
class Init { static void main() { } }
