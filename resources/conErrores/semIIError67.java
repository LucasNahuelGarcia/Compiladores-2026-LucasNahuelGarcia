///[Error:=|7]
// Un supertipo no conforma con un destino subtipo.
class Base { }
class Hija extends Base { }
class Prueba {
    void asignar(Hija destino, Base origen) {
        destino = origen;
    }
}
