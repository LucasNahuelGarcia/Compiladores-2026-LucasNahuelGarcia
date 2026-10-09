///[Error:m|4]
class Caja<T>{}
class A1<T>{ Caja<T>[] m(Caja<T>[] valor){} }
class B2 extends A1<String>{ Caja<Object>[] m(Caja<Object>[] valor){} }
class Init{ static void main(){} }
