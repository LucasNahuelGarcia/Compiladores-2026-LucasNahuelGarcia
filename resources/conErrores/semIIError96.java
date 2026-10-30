///[Error:recibir|4]
class Caja<T> {
    void recibir(Object[] valores) { }
    void test(T[] valores) { recibir(valores); }
}
