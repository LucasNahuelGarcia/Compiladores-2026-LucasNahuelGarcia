///[Error:=|5]
// Los arreglos no son covariantes, tampoco al cambiar la cantidad de dimensiones.
class Prueba {
    void asignar(Object[] destino, int[][] origen) {
        destino = origen;
    }
}
