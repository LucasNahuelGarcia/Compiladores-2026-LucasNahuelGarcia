// Los parentesis conservan el tipo, pero no la asignabilidad ni la forma de sentencia.
class A1 {
    int x;
    int y;
    int obtener() { return x; }

    void probar(A1 objeto) {
        var copia = (x = 3);
        var calculado = (x + y) * 2;
        var campo = (objeto).x;
        var llamada = objeto.obtener();
        var anidada = (x = (y = 3));
    }
}

class Init { static void main() { } }
