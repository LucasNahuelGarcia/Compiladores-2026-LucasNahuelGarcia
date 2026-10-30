///[Error:==|5]
class Caja<T> { }
class Prueba {
    boolean comparar(Caja<String> texto, Caja<Object> objeto) {
        return texto == objeto;
    }
}
