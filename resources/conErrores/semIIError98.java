///[Error:obtener|4]
class Padre<T> { T obtener() { return null; } }
class HijaConT<T> extends Padre {
    T devolver() { return obtener(); }
}
