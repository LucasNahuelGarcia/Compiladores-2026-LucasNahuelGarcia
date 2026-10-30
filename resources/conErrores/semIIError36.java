///[Error:+|6]
// void no puede ser operando aritmetico.
class Prueba {
    void sinValor() { }
    int obtener() {
        return 1 + sinValor();
    }
}
