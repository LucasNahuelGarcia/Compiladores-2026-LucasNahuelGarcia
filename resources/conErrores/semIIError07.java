///[Error:(|6]
// Los parentesis limpian la validez como sentencia aunque el interior sea una llamada.
class A1 {
    void metodo() { }
    void probar() {
        (metodo());
    }
}

class Init { static void main() { } }
