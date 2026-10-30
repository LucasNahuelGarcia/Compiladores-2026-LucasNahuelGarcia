///[Error:new|8]
// El constructor se busca solo en la clase instanciada y por aridad.
class Persona {
    Persona(int edad) { }
}
class Prueba {
    void crear() {
        var valor = new Persona();
    }
}
