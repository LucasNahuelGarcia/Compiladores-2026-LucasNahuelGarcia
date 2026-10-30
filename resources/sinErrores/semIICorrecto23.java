// Al salir de un bloque, el nombre vuelve a resolver al atributo.
class FugaAmbito {
    int x;
    void test() {
        { var x = "hola"; var s = x.toString(); }
        x = 42;
    }
}
