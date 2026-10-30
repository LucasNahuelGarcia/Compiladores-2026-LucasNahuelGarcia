///[Error:=|8]
// El parametro heredado por una arista raw conserva su identidad de origen.
class Base<T> {
    T obtener() { return null; }
}
class Hija<T> extends Base {
    void probar(T propio) {
        propio = obtener();
    }
}
