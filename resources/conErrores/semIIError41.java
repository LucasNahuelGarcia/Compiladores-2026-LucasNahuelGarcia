///[Error:instancia|6]
// Una llamada por tipo solo admite metodos estaticos.
class Prueba {
    void instancia() { }
    void probar() {
        Prueba.instancia();
    }
}
