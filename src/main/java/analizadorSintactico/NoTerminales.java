package analizadorSintactico;

import analizadorLexico.TokenType;

final class ListaClases implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (!c.fin()) {
            if (c.es(TokenType.kw_class))
                new Clase().parse(c);
            else if (c.es(TokenType.kw_interface))
                new Interfaz().parse(c);
            else
                throw new ExcepcionSintactica(c.actual(), "class o interface");
        }
    }
}

final class Clase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_class);
        c.match(TokenType.identificadorDeClase);
        new GenericidadOpcional().parse(c);
        new HerenciaOpcional().parse(c);
        c.match(TokenType.openBraces);
        new ListaMiembros().parse(c);
        c.match(TokenType.closeBraces);
    }
}

final class Interfaz implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_interface);
        c.match(TokenType.identificadorDeClase);
        new GenericidadOpcional().parse(c);
        new ExtensionOpcional().parse(c);
        c.match(TokenType.openBraces);
        new ListaMetodosInterfaz().parse(c);
        c.match(TokenType.closeBraces);
    }
}

final class GenericidadOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.lessThan)) {
            c.match(TokenType.lessThan);
            c.match(TokenType.IdentificadorDeParametroDeTipo);
            c.match(TokenType.greaterThan);
        }
    }
}

final class HerenciaOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_extends)) {
            c.match(TokenType.kw_extends);
            new TipoReferencia().parse(c);
        } else if (c.es(TokenType.kw_implements)) {
            c.match(TokenType.kw_implements);
            new TipoReferencia().parse(c);
        }
    }
}

final class ExtensionOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_extends)) {
            c.match(TokenType.kw_extends);
            new TipoReferencia().parse(c);
        }
    }
}

final class ListaMiembros implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (!c.es(TokenType.closeBraces))
            new Miembro().parse(c);
    }
}

final class ListaMetodosInterfaz implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (!c.es(TokenType.closeBraces))
            new MetodoInterfaz().parse(c);
    }
}

final class Miembro implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_static)) {
            c.match(TokenType.kw_static);
            new MetodoStaticResto().parse(c);
            return;
        }
        if (c.es(TokenType.kw_public)) {
            new Constructor().parse(c);
            return;
        }
        if (c.es(TokenType.kw_void)) {
            c.match(TokenType.kw_void);
            c.match(TokenType.identificador);
            new ArgsFormales().parse(c);
            new Bloque().parse(c);
            return;
        }
        new Tipo().parse(c);
        c.match(TokenType.identificador);
        new RestoMiembroConTipo().parse(c);
    }
}

final class RestoMiembroConTipo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.semicolon))
            c.match(TokenType.semicolon);
        else {
            new ArgsFormales().parse(c);
            new Bloque().parse(c);
        }
    }
}

final class MetodoStaticResto implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new TipoMetodo().parse(c);
        c.match(TokenType.identificador);
        new ArgsFormales().parse(c);
        new Bloque().parse(c);
    }
}

final class Constructor implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_public);
        c.match(TokenType.identificadorDeClase);
        new ArgsFormales().parse(c);
        new Bloque().parse(c);
    }
}

final class MetodoInterfaz implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new TipoMetodo().parse(c);
        c.match(TokenType.identificador);
        new ArgsFormales().parse(c);
        c.match(TokenType.semicolon);
    }
}

final class TipoMetodo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_void))
            c.match(TokenType.kw_void);
        else
            new Tipo().parse(c);
    }
}

final class Tipo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new TipoBase().parse(c);
        new DimensionesOpcionales().parse(c);
    }
}

final class TipoBase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.kw_boolean, TokenType.kw_char, TokenType.kw_int))
            new TipoPrimitivo().parse(c);
        else if (c.es(TokenType.identificadorDeClase))
            new TipoReferencia().parse(c);
        else if (c.es(TokenType.IdentificadorDeParametroDeTipo))
            c.match(TokenType.IdentificadorDeParametroDeTipo);
        else
            throw new ExcepcionSintactica(c.actual(), "tipo");
    }
}

final class DimensionesOpcionales implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (c.es(TokenType.openSquareBracket)) {
            c.match(TokenType.openSquareBracket);
            c.match(TokenType.closeSquareBracket);
        }
    }
}

final class TipoReferencia implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.identificadorDeClase);
        new TipoGenericoOpcional().parse(c);
    }
}

final class TipoPrimitivo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_boolean))
            c.match(TokenType.kw_boolean);
        else if (c.es(TokenType.kw_char))
            c.match(TokenType.kw_char);
        else
            c.match(TokenType.kw_int);
    }
}

final class TipoGenericoOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.lessThan)) {
            c.match(TokenType.lessThan);
            new InstanciadoOParametrico().parse(c);
            c.match(TokenType.greaterThan);
        }
    }
}

final class InstanciadoOParametrico implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.IdentificadorDeParametroDeTipo))
            c.match(TokenType.IdentificadorDeParametroDeTipo);
        else
            c.match(TokenType.identificadorDeClase);
    }
}

final class ArgsFormales implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.openParenthesis);
        new ListaArgsFormalesOpcional().parse(c);
        c.match(TokenType.closeParenthesis);
    }
}

final class ListaArgsFormalesOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (!c.es(TokenType.closeParenthesis))
            new ListaArgsFormales().parse(c);
    }
}

final class ListaArgsFormales implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new ArgFormal().parse(c);
        new ListaArgsFormalesPrima().parse(c);
    }
}

final class ListaArgsFormalesPrima implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (c.es(TokenType.comma)) {
            c.match(TokenType.comma);
            new ArgFormal().parse(c);
        }
    }
}

final class ArgFormal implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new Tipo().parse(c);
        c.match(TokenType.identificador);
    }
}

final class Bloque implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.openBraces);
        new ListaSentencias().parse(c);
        c.match(TokenType.closeBraces);
    }
}

final class ListaSentencias implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (!c.es(TokenType.closeBraces))
            new Sentencia().parse(c);
    }
}

final class Sentencia implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.semicolon)) {
            c.match(TokenType.semicolon);
            return;
        }
        if (c.es(TokenType.openBraces)) {
            new Bloque().parse(c);
            return;
        }
        if (c.es(TokenType.kw_var)) {
            new VarLocal().parse(c);
            c.match(TokenType.semicolon);
            return;
        }
        if (c.es(TokenType.kw_return)) {
            new Return().parse(c);
            c.match(TokenType.semicolon);
            return;
        }
        if (c.es(TokenType.kw_if)) {
            new If().parse(c);
            return;
        }
        if (c.es(TokenType.kw_while)) {
            new While().parse(c);
            return;
        }
        new Expresion().parse(c);
        c.match(TokenType.semicolon);
    }
}

final class VarLocal implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_var);
        c.match(TokenType.identificador);
        c.match(TokenType.assignment);
        new ExpresionCompuesta().parse(c);
    }
}

final class Return implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_return);
        new ExpresionOpcional().parse(c);
    }
}

final class ExpresionOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (!c.es(TokenType.semicolon))
            new Expresion().parse(c);
    }
}

final class If implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_if);
        c.match(TokenType.openParenthesis);
        new Expresion().parse(c);
        c.match(TokenType.closeParenthesis);
        new Sentencia().parse(c);
        new If2().parse(c);
    }
}

final class If2 implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_else)) {
            c.match(TokenType.kw_else);
            new Sentencia().parse(c);
        }
    }
}

final class While implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_while);
        c.match(TokenType.openParenthesis);
        new Expresion().parse(c);
        c.match(TokenType.closeParenthesis);
        new Sentencia().parse(c);
    }
}

final class Expresion implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new ExpresionCompuesta().parse(c);
        new Expresion2().parse(c);
    }
}

final class OperadorAsignacion implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.assignment);
    }
}

final class Expresion2 implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.assignment)) {
            new OperadorAsignacion().parse(c);
            new ExpresionCompuesta().parse(c);
        }
    }
}

final class ExpresionCompuesta implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new ExpresionBasica().parse(c);
        new ExpresionCompuesta2().parse(c);
    }
}

final class ExpresionCompuesta2 implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (ContextoSintactico.cualquiera(c, TokenType.or, TokenType.and, TokenType.equals, TokenType.notEquals,
                TokenType.lessThan, TokenType.greaterThan, TokenType.lessThanOrEqual, TokenType.greaterThanOrEqual,
                TokenType.plus, TokenType.minus, TokenType.multiply, TokenType.operatorSlash, TokenType.mod)) {
            new OperadorBinario().parse(c);
            new ExpresionBasica().parse(c);
        }
    }
}

final class OperadorBinario implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.or))
            c.match(TokenType.or);
        else if (c.es(TokenType.and))
            c.match(TokenType.and);
        else if (c.es(TokenType.equals))
            c.match(TokenType.equals);
        else if (c.es(TokenType.notEquals))
            c.match(TokenType.notEquals);
        else if (c.es(TokenType.lessThan))
            c.match(TokenType.lessThan);
        else if (c.es(TokenType.greaterThan))
            c.match(TokenType.greaterThan);
        else if (c.es(TokenType.lessThanOrEqual))
            c.match(TokenType.lessThanOrEqual);
        else if (c.es(TokenType.greaterThanOrEqual))
            c.match(TokenType.greaterThanOrEqual);
        else if (c.es(TokenType.plus))
            c.match(TokenType.plus);
        else if (c.es(TokenType.minus))
            c.match(TokenType.minus);
        else if (c.es(TokenType.multiply))
            c.match(TokenType.multiply);
        else if (c.es(TokenType.operatorSlash))
            c.match(TokenType.operatorSlash);
        else
            c.match(TokenType.mod);
    }
}

final class ExpresionBasica implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.plus, TokenType.minus, TokenType.not))
            new OperadorUnario().parse(c);
        new Operando().parse(c);
    }
}

final class OperadorUnario implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.plus))
            c.match(TokenType.plus);
        else if (c.es(TokenType.minus))
            c.match(TokenType.minus);
        else
            c.match(TokenType.not);
    }
}

final class Operando implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.kw_true, TokenType.kw_false, TokenType.intLiteral,
                TokenType.charLiteral, TokenType.kw_null))
            new Primitivo().parse(c);
        else
            new Referencia().parse(c);
    }
}

final class Primitivo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_true))
            c.match(TokenType.kw_true);
        else if (c.es(TokenType.kw_false))
            c.match(TokenType.kw_false);
        else if (c.es(TokenType.intLiteral))
            c.match(TokenType.intLiteral);
        else if (c.es(TokenType.charLiteral))
            c.match(TokenType.charLiteral);
        else
            c.match(TokenType.kw_null);
    }
}

final class Referencia implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new Primario().parse(c);
        new Referencia2().parse(c);
    }
}

final class Referencia2 implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (c.es(TokenType.period) || c.es(TokenType.openSquareBracket)) {
            if (c.es(TokenType.period)) {
                c.match(TokenType.period);
                c.match(TokenType.identificador);
                new EncadenamientoOpcional().parse(c);
            } else
                new AccesoArreglo().parse(c);
        }
    }
}

final class EncadenamientoOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.openParenthesis))
            new ArgsActuales().parse(c);
    }
}

final class Primario implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_this)) {
            c.match(TokenType.kw_this);
            return;
        }
        if (c.es(TokenType.stringLiteral)) {
            c.match(TokenType.stringLiteral);
            return;
        }
        if (c.es(TokenType.kw_new)) {
            c.match(TokenType.kw_new);
            new Instanciacion().parse(c);
            return;
        }
        if (c.es(TokenType.identificadorDeClase)) {
            new LlamadaMetodoEstatico().parse(c);
            return;
        }
        if (c.es(TokenType.identificador)) {
            new AccesoVarLlamadaMetodo().parse(c);
            return;
        }
        new ExpresionParentizada().parse(c);
    }
}

final class Instanciacion implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.kw_boolean, TokenType.kw_char, TokenType.kw_int)) {
            new TipoPrimitivo().parse(c);
            new DimensionesConTamanio().parse(c);
        } else if (c.es(TokenType.identificadorDeClase)) {
            c.match(TokenType.identificadorDeClase);
            new TipoGenericoOpcional().parse(c);
            new ArgsODimensiones().parse(c);
        } else {
            c.match(TokenType.IdentificadorDeParametroDeTipo);
            new ArgsODimensiones().parse(c);
        }
    }
}

final class ArgsODimensiones implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.openParenthesis))
            new ArgsActuales().parse(c);
        else
            new DimensionesConTamanio().parse(c);
    }
}

final class AccesoVarLlamadaMetodo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.identificador);
        new ArgsActuales2().parse(c);
    }
}

final class ArgsActuales2 implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.openParenthesis))
            new ArgsActuales().parse(c);
    }
}

final class ExpresionParentizada implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.openParenthesis);
        new Expresion().parse(c);
        c.match(TokenType.closeParenthesis);
    }
}

final class LlamadaMetodoEstatico implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.identificadorDeClase);
        c.match(TokenType.period);
        c.match(TokenType.identificador);
        new ArgsActuales().parse(c);
    }
}

final class DimensionesConTamanio implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.openSquareBracket);
        new Expresion().parse(c);
        c.match(TokenType.closeSquareBracket);
        new DimensionesConTamanioResto().parse(c);
    }
}

final class DimensionesConTamanioResto implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.openSquareBracket))
            new DimensionesConTamanio().parse(c);
    }
}

final class ArgsActuales implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.openParenthesis);
        new ListaExpsOpcional().parse(c);
        c.match(TokenType.closeParenthesis);
    }
}

final class ListaExpsOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (!c.es(TokenType.closeParenthesis))
            new ListaExps().parse(c);
    }
}

final class ListaExps implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new Expresion().parse(c);
        new ListaExps2().parse(c);
    }
}

final class ListaExps2 implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (c.es(TokenType.comma)) {
            c.match(TokenType.comma);
            new Expresion().parse(c);
        }
    }
}

final class AccesoArreglo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.openSquareBracket);
        new Expresion().parse(c);
        c.match(TokenType.closeSquareBracket);
    }
}
