///[Error:toString|5]
// No se resuelven miembros sobre un parametro de tipo sin instanciar.
class Generico<T> {
    void probar(T valor) {
        valor.toString();
    }
}
