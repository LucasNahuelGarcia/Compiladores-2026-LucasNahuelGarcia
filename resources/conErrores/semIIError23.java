///[Error:this|5]
// this no existe en un contexto estatico.
class Prueba {
    static Prueba obtener() {
        return this;
    }
}
