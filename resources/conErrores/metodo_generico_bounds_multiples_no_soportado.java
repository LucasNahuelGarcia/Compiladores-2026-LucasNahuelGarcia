///[Error:I2|4]
class A1{}
interface I2{}
class C3{ <T extends A1 & I2> T m(T valor){} }
class Init{ static void main(){} }
