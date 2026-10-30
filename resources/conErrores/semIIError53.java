///[Error:x|5]
// Una local no es visible antes de su declaracion.
class Prueba {
    void probar() {
        x = 1;
        var x = 2;
    }
}
