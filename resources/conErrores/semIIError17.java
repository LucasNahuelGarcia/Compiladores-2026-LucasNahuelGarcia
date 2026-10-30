///[Error:=|6]
// Los argumentos genericos son invariantes.
class Caja<T> { }
class Prueba {
    void asignar(Caja<String> texto, Caja<Object> objeto) {
        texto = objeto;
    }
}
