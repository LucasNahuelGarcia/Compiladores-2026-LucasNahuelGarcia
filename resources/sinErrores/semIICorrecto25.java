// T crudo heredado conserva su identidad; arreglos y llamadas estáticas.
class Padre<T> {
    T obtener() { return null; }
    void guardar(T valor) { }
}
class HijaSinT extends Padre {
    void test() {
        var fantasma = obtener();
        guardar(fantasma);
        guardar(null);
    }
}
class ArregloGen<T> {
    void recibir(Object valor) { }
    static void estatico() { }
    int instancia() { return 2; }
    static void test(ArregloGen<String> obj) {
        var a = new ArregloGen<String>();
        var b = new Figura[10];
        obj.estatico();
        var c = obj.instancia();
        recibirEstatico(b);
        var longitud = b.length;
    }
    void arreglos() {
        var arr = new T[4];
        recibirEstatico(arr);
        var texto = arr.toString();
        var tam = arr.length;
    }
    static void recibirEstatico(Object valor) { }
}
interface Figura { }
class Caja<U> { }
class IgualdadValida {
    boolean comparar(Caja<String> caja, String[] textos, Object objeto) {
        var b1 = textos == objeto;
        var b2 = caja == objeto;
        return b1 && b2;
    }
}
