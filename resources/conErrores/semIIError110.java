///[Error:++|6]
// Una llamada que retorna int no es un destino asignable para ++.
class PostfijoLlamada {
    int obtener() { return 1; }
    void probar() {
        obtener()++;
    }
}
