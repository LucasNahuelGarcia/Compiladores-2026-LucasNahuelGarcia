// ++ y -- sobre índices de arreglos son destinos asignables; -- produce int en expresiones.
class PostfijosArreglo {
    void probar() {
        var arreglo = new int[2];
        var indice = 0;
        arreglo[indice]++;
        arreglo[indice]--;
        var resultado = arreglo[indice]-- + 1;
    }
}
