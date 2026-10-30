///[Error:==|5]
// Una variable de tipo T no puede compararse con String.
class Generico<T> {
    void probar(T valor) {
        if (valor == "texto") { ; }
    }
}
