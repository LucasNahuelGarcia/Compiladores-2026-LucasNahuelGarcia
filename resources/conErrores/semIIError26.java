///[Error:accion|6]
// var no puede inferir el retorno void de una llamada.
class Prueba {
    void accion() { }
    void probar() {
        var resultado = accion();
    }
}
