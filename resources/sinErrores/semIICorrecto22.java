// El inicializador se resuelve antes de declarar la local homónima.
class SombraInicializador {
    String x;
    void test() {
        var x = x.toString();
        var y = x;
        var z = 10;
        var campo = this.x;
    }
}
class SombraConCambioDeTipo {
    String x;
    void test() {
        var x = 10;
        this.x.toString();
    }
}
