///[Error:var|5]
// var no puede ser cuerpo directo de if sin llaves.
class A1 {
    void crear() {
        if (true) var valor = 1;
    }
}
class Init { static void main() { } }
