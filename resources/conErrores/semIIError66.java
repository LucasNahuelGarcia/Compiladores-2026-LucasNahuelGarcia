///[Error:==|6]
// Dos clases no relacionadas no son comparables por igualdad.
class Alfa { }
class Beta { }
class Prueba {
    boolean comparar(Alfa a, Beta b) { return a == b; }
}
