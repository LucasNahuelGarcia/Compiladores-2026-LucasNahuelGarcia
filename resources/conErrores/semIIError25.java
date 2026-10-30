///[Error:instancia|6]
// La invocacion implicita de un metodo de instancia falla en contexto estatico.
class Prueba {
    int instancia() { return 1; }
    static int obtener() {
        return instancia();
    }
}
