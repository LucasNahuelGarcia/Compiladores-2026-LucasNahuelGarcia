///[SinErrores]
class Caja1<X> {}
interface I1<T> { void procesar(Caja1<T> valor); }
class C1<T> implements I1<T> { void procesar(Caja1<T> valor) {} }
class Init { static void main() {} }
