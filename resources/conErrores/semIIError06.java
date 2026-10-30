///[Error:=|6]
// Un parametro generico no conforma con Object si no esta instanciado.
class Caja<T> {
    Object valor;
    void guardar(T entrada) {
        valor = entrada;
    }
}

class Init { static void main() { } }
