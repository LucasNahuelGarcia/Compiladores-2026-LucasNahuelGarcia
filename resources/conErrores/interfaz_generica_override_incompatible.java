///[Error:C3|3]
interface I1<T>{ void m(T x); }
class C3 implements I1<String>{ void m(Object x){} }
class Init{ static void main(){} }
