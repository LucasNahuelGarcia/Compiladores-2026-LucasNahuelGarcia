///[SinErrores]
sealed interface I1 permits J2{}
non-sealed interface J2 extends I1{}
class Init{ static void main(){} }
