// Firmas y llamadas de miembros predefinidos de MiniJava.
class Prueba {
    void probar() {
        Object.debugPrint(1);
        System.printIln(System.read());
        System.printSln("hola");
        System.println();
        var texto = "hola".toString();
    }
}
class Init { static void main() { } }
