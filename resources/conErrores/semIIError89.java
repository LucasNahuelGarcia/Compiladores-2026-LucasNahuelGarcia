///[Error:=|6]
class Primario {
    int x;
    Primario siguiente;
    void test(Primario obj) {
        (obj).siguiente.x = 3;
    }
}
