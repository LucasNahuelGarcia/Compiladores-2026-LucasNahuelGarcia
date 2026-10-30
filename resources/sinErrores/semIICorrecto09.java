// Cobertura combinada de vistas genericas, arreglos, ambitos y sentencias.
class Base<T> {
    T obtener() { return null; }
}
class Medio<U> extends Base<U> { }
class Caja<T> {
    T valor;
    Caja(T inicial) { valor = inicial; }
    T obtener() { return valor; }
    void guardar(T nuevo) { valor = nuevo; }
    <U> static U identidad(U dato) { return dato; }
    static void estatico() { }
}
class Final extends Medio<String> { }
class Util {
    static void recibir(Object valor) { }
    static void estatico() { }
}
class Prueba {
    void probar(Caja<String> caja, Final finalizado, int[][] matriz,
                String[] textos, Caja<String> otra) {
        var asignado = (matriz[0][0] = 3);
        var texto = finalizado.obtener();
        caja.guardar("dato");
        var obtenido = caja.obtener();
        var inferido = Caja.identidad(finalizado.obtener());
        var arreglo = new int[3][4];
        arreglo[0][0] = asignado;
        var fila = arreglo[0];
        Util.recibir(new int[10]);
        var elemento = textos[0];
        textos.toString();
        Util.estatico();
        caja.estatico();
        if (obtenido == null) { var local = 1; }
        else { var local = 2; }
        while (false) { ; }
        for (int elementoFor : arreglo[0]) { var copia = elementoFor; }
        for (; true; ) { ; }
        return;
    }

    static void estatico(Caja<String> caja) {
        var referencias = new Caja<String>[2];
        caja.estatico();
    }

    int sinRetornoTotal() { }
}
class Init { static void main() { } }
