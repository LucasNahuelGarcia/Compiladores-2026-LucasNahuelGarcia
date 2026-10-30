///[Error:=|5]
// T[] tampoco conforma con Object[].
class Generico<T> {
    void copiar(Object[] destino, T[] origen) {
        destino = origen;
    }
}
