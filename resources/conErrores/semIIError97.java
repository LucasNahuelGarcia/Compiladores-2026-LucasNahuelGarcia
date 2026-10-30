///[Error:guardar|4]
class Padre<T> { void guardar(T valor) { } }
class HijaConT<T> extends Padre {
    void test(T propio) { guardar(propio); }
}
