///[Error:==|5]
// Un parametro generico no conforma con Object.
class Generico<T> {
    boolean comparar(T valor, Object otro) {
        return valor == otro;
    }
}
