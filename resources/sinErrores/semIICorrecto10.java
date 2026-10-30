// Parametros de tipo, null, shadowing de atributos y creacion de arreglos genericos.
class Caja<T> {
    T valor;
    int mismoNombre;
    Caja() { return; }

    T obtener() { return valor; }

    void probar(T entrada, Caja<T> caja) {
        var propio = entrada;
        var arreglo = new T[10];
        var matrices = new Caja<T>[2][3];
        var atributo = valor;
        var ocultamiento = (valor = entrada);
        var mismoNombre = mismoNombre;
        if (entrada == null) { var local = entrada; }
        else { var local = null == caja; }
    }
}
class Init { static void main() { } }
