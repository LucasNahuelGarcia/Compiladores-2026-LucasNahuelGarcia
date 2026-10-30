///[Error:x|6]
// Una local deja de ser visible al cerrar su bloque.
class Prueba {
    void probar() {
        { var x = 1; }
        x = 2;
    }
}
