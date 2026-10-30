// Casos adicionales de operadores, null, bloques y receptores de interfaz.
interface Lector { String leer(); }
class SuperDatos {
    int heredado;
    void heredadoMetodo() { }
}
class Datos extends SuperDatos {
    int numero;
    char caracter;
    boolean activo;
    String texto;
    void voidCall() { }
    static void estatico() { }
}
class Prueba {
    void recibir(Object valor) { }
    void probar(Datos datos, Lector lector, int[] arreglo) {
        var entero = +(-3 * 2 / 1 % 4);
        var letra = 'x';
        var verdad = !false && true || false;
        var cadena = "texto";
        var nulo = null == null;
        datos = null;
        datos.numero = arreglo[0];
        var valorHeredado = datos.heredado;
        datos.heredadoMetodo();
        var longitud = arreglo.length;
        arreglo[0] = datos.numero;
        new Datos();
        var resultado = lector.leer();
        recibir(datos);
        if (datos == null) { var dentro = entero; }
        else { var dentro = letra; }
        { var bloque = verdad; { var profundo = cadena; } }
        datos.voidCall();
        recibir(datos);
        Datos.estatico();
        System.println();
        for (int item : arreglo) { var copia = item; }
        ;
    }
}
