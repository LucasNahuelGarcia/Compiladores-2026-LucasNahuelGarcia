///[Error:var|5]
// Una declaracion local directa en while requiere llaves.
class Prueba {
    void probar() {
        while (true) var valor = 1;
    }
}
