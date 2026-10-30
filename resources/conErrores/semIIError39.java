///[Error:new|6]
// No se pueden instanciar interfaces.
interface Interfaz { }
class Prueba {
    void crear() {
        var valor = new Interfaz();
    }
}
