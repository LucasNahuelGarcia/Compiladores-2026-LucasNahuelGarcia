///[Error:m|4]
class Caja<T>{}
interface I1<T>{ void m(Caja<T> x); }
interface J2 extends I1<String>{ void m(Caja<Object> x); }
class Init{ static void main(){} }
