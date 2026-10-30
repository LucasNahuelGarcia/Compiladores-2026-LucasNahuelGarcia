// Sustitucion transitiva e inferencia de parametros genericos de metodo.
class Base<T> {
    T obtener() { return null; }
}

class Medio<U> extends Base<U> { }

class Caja<T> {
    <T> static T identidad(T valor) { return valor; }
}

class Final extends Medio<String> {
    void aceptar(String valor) { }

    void probar(Final receptor) {
        aceptar(receptor.obtener());
        var copia = Caja.identidad(receptor.obtener());
    }
}

class Init { static void main() { } }
