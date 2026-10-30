///[Error:==|5]
// Un parametro de tipo sin instanciar no conforma con String.
class Generico<T> {
    boolean comparar(T valor) {
        return valor == "texto";
    }
}
