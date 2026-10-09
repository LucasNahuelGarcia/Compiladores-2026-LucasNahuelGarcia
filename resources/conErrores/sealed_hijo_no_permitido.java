///[Error:C3|3]
sealed class A1 permits B2{}
class C3 extends A1{}
class B2 extends A1{}
class Init{ static void main(){} }
