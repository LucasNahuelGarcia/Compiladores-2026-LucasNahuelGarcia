///[Error:var|5]
// Una declaracion var directa en la rama else requiere llaves.
class Prueba {
    void probar() {
        if (true) { ; } else var valor = 1;
    }
}
