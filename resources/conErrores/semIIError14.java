///[Error:metodo|6]
// Parentizar el receptor invalida la llamada como sentencia, aunque conserva su tipo.
class A1 {
    void metodo() { }
    void probar(A1 objeto) {
        (objeto).metodo();
    }
}
class Init { static void main() { } }
