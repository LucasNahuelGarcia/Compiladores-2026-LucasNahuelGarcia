///[Error:C3|4]
interface I1{ void m(); }
interface I2{ void n(); }
class C3 implements I1,I2{ void m(){} }
class Init{ static void main(){} }
