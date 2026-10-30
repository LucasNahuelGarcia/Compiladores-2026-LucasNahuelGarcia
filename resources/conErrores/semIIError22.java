///[Error:[|5]
// Un Object no se puede indexar aunque su valor dinamico sea un arreglo.
class Prueba {
    int obtener(Object valores) {
        return valores[0];
    }
}
