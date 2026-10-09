///[Error:C3|4]
class Caja<T>{}
interface I1<T>{ void m(Caja<T> x); }
class C3 implements I1<String>{ void m(Caja<Object> x){} }
class Init{ static void main(){} }
