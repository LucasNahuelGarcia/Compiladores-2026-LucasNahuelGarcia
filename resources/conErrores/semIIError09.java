///[Error:new|5]
// No se permite crear una instancia de un tipo generico raw.
class Caja<T> {
    void crear() {
        var caja = new Caja();
    }
}
class Init { static void main() { } }
