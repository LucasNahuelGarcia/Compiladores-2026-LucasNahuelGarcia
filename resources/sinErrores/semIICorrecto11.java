// Sentencias validas de llamada/creacion, retorno y chequeo de codigo inalcanzable.
class Persona {
    int edad;
    void accion() { }
    static void estatico() { }
}
class Prueba {
    Persona crear() { return new Persona(); }
    int sinRetornoTotal() { }

    void probar(Persona persona, int[] valores) {
        persona.accion();
        persona.estatico();
        Persona.estatico();
        crear().accion();
        new Persona();
        System.println();
        return;
        valores[0] = persona.edad;
    }

    int parcial(boolean condicion) {
        if (condicion) return 1;
    }
}
class Init { static void main() { } }
