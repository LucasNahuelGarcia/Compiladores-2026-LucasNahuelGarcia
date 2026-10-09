///[SinErrores]
class Caja<T>{}
interface I1<T>{ void m(Caja<T> valor); }
interface J2 extends I1<String>{}
class C3 implements J2{ void m(Caja<String> valor){} }
class Init{ static void main(){} }
