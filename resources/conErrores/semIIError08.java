///[Error:new|5]
// Un parametro generico de clase no puede usarse en un cuerpo estatico.
class Caja<T> {
    static void crear() {
        var arreglo = new T[10];
    }
}
class Init { static void main() { } }
