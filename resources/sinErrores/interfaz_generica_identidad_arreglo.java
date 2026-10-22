///[SinErrores]
interface I1<T> { void procesar(T[] valores); }
class C1<T> implements I1<T> { void procesar(T[] valores) {} }
class Init { static void main() {} }
