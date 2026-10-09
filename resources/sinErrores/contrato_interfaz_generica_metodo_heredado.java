///[SinErrores]
interface I1<T>{ void m(T x); }
class P2{ void m(String x){} }
class C3 extends P2 implements I1<String>{}
class Init{ static void main(){} }
