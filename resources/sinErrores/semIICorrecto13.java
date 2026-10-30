// Contratos genericos heredados, inferencia contextual y resolucion de nombres.
interface Proveedor<T> {
    T obtener();
}
class Implementacion<U> implements Proveedor<U> {
    U obtener() { return null; }
}
class Caja {
    <T> static T crear() { return null; }
}
class Prueba {
    int valor;
    void recibir(String texto) { }

    void probar(Proveedor<String> proveedor, Implementacion<String> implementacion,
                int valor) {
        var texto = proveedor.obtener();
        var otro = implementacion.obtener();
        recibir(Caja.crear());
        this.valor = valor;
        { var hermano = 1; }
        { var hermano = true; }
    }
}
class Init { static void main() { } }
