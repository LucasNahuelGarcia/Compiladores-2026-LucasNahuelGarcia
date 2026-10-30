// Sustitucion en arreglos, extension de interfaces y conformidad transitiva.
class Base<T> {
    T[] obtener() { return null; }
}
class Hija extends Base<String> { }
interface Raiz<T> { T dato(); }
interface Sub extends Raiz<String> { }
class Implementacion implements Sub {
    String dato() { return null; }
}
class Prueba {
    void recibirObject(Object valor) { }
    void recibirRaiz(Raiz<String> valor) { }

    void probar(Hija hija, Implementacion objeto) {
        var arreglo = hija.obtener();
        var elemento = arreglo[0];
        recibirObject(objeto);
        recibirRaiz(objeto);
        var texto = objeto.toString();
        var iguales = null == null;
        if (iguales = (elemento == null)) { ; }
        posterior();
    }

    void invocarAntes() { posterior(); }
    void posterior() { }
}
class Init { static void main() { } }
