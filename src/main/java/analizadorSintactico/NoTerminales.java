package analizadorSintactico;

import analizadorLexico.TokenType;
import analizadorLexico.Token;
import analizadorSemantico.EntradaAtributo;
import analizadorSemantico.EntradaClase;
import analizadorSemantico.EntradaMetodo;
import analizadorSemantico.EntradaParametro;

final class ListaClases implements NoTerminal {
    public void parse(ContextoSintactico c) {
        while (!c.fin()) {
            parseModificadores(c);
            if (c.es(TokenType.kw_class))
                new Clase().parse(c);
            else if (c.es(TokenType.kw_interface))
                new Interfaz().parse(c);
            else
                throw new ExcepcionSintactica(c.actual(), "class o interface");
        }
    }

    private void parseModificadores(ContextoSintactico c) {
        c.setTipoFinal(false);
        c.setTipoSealed(false);
        c.setTipoNonSealed(false);
        while (ContextoSintactico.cualquiera(c, TokenType.kw_final, TokenType.kw_sealed,
                TokenType.kw_non_sealed)) {
            if (c.es(TokenType.kw_final)) { c.match(TokenType.kw_final); c.setTipoFinal(true); }
            else if (c.es(TokenType.kw_sealed)) { c.match(TokenType.kw_sealed); c.setTipoSealed(true); }
            else { c.match(TokenType.kw_non_sealed); c.setTipoNonSealed(true); }
        }
    }
}

final class Clase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_class);
        Token tokenNombre = c.match(TokenType.identificadorDeClase);
        EntradaClase clase = new EntradaClase(tokenNombre);
        clase.setFinalClase(c.isTipoFinal());
        clase.setSealed(c.isTipoSealed());
        clase.setNonSealed(c.isTipoNonSealed());
        c.getTablaSimbolos().agregarClase(clase);
        c.setClaseActual(clase);
        new GenericidadOpcional().parse(c);
        new HerenciaOpcional().parse(c);
        parsePermisos(c, clase);
        c.match(TokenType.openBraces);
        new ListaMiembros().parse(c);
        c.match(TokenType.closeBraces);
        c.setMetodoActual(null);
        c.setClaseActual(null);
    }

    private void parsePermisos(ContextoSintactico c, EntradaClase clase) {
        if (!c.es(TokenType.kw_permits))
            return;
        c.match(TokenType.kw_permits);
        do {
            clase.agregarPermiso(c.match(TokenType.identificadorDeClase));
            if (!c.es(TokenType.comma))
                return;
            c.match(TokenType.comma);
        } while (true);
    }
}

final class Interfaz implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_interface);
        Token tokenNombre = c.match(TokenType.identificadorDeClase);
        EntradaClase interfaz = new EntradaClase(tokenNombre, true);
        interfaz.setFinalClase(c.isTipoFinal());
        interfaz.setSealed(c.isTipoSealed());
        interfaz.setNonSealed(c.isTipoNonSealed());
        c.getTablaSimbolos().agregarClase(interfaz);
        c.setClaseActual(interfaz);
        new GenericidadOpcional().parse(c);
        new ExtensionOpcional().parse(c);
        if (c.es(TokenType.kw_permits)) {
            c.match(TokenType.kw_permits);
            do {
                interfaz.agregarPermiso(c.match(TokenType.identificadorDeClase));
                if (!c.es(TokenType.comma)) break;
                c.match(TokenType.comma);
            } while (true);
        }
        c.match(TokenType.openBraces);
        new ListaMetodosInterfaz().parse(c);
        c.match(TokenType.closeBraces);
        c.setMetodoActual(null);
        c.setClaseActual(null);
    }
}

final class GenericidadOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.lessThan)) {
            c.match(TokenType.lessThan);
            Token parametro = c.es(TokenType.IdentificadorDeParametroDeTipo)
                    ? c.match(TokenType.IdentificadorDeParametroDeTipo)
                    : c.match(TokenType.identificadorDeClase);
            if (c.getClaseActual() != null)
                c.getClaseActual().setParametroGenerico(parametro.getLexema());
            c.match(TokenType.greaterThan);
        }
    }
}

final class HerenciaOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_extends)) {
            c.match(TokenType.kw_extends);
            Token tokenBase = c.match(TokenType.identificadorDeClase);
            c.getClaseActual().setClaseBase(tipoBaseConArgumento(c, tokenBase));
        }
        if (c.es(TokenType.kw_implements)) {
            c.match(TokenType.kw_implements);
            agregarInterfaces(c);
        }
    }

    private void agregarInterfaces(ContextoSintactico c) {
        do {
            Token tokenInterfaz = c.match(TokenType.identificadorDeClase);
            c.getClaseActual().agregarInterfaz(tipoBaseConArgumento(c, tokenInterfaz));
            if (!c.es(TokenType.comma))
                return;
            c.match(TokenType.comma);
        } while (true);
    }

    private analizadorSemantico.TipoReferencia tipoBaseConArgumento(ContextoSintactico c, Token tokenBase) {
        analizadorSemantico.Tipo argumento = null;
        if (c.es(TokenType.lessThan)) {
            c.match(TokenType.lessThan);
            argumento = new Tipo().parseTipo(c);
            c.match(TokenType.greaterThan);
        }
        return new analizadorSemantico.TipoReferencia(tokenBase, tokenBase.getLexema(), argumento);
    }
}

final class ExtensionOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_extends)) {
            c.match(TokenType.kw_extends);
            do {
                Token tokenBase = c.match(TokenType.identificadorDeClase);
                c.getClaseActual().agregarInterfaz(tipoBaseConArgumento(c, tokenBase));
                if (!c.es(TokenType.comma))
                    return;
                c.match(TokenType.comma);
            } while (true);
        }
    }

    private analizadorSemantico.TipoReferencia tipoBaseConArgumento(ContextoSintactico c, Token tokenBase) {
        analizadorSemantico.Tipo argumento = null;
        if (c.es(TokenType.lessThan)) {
            c.match(TokenType.lessThan);
            argumento = new Tipo().parseTipo(c);
            c.match(TokenType.greaterThan);
        }
        return new analizadorSemantico.TipoReferencia(tokenBase, tokenBase.getLexema(), argumento);
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
        new VisibilidadOpcional().parse(c);
        c.setMetodoFinal(false);
        if (c.es(TokenType.kw_final)) {
            c.match(TokenType.kw_final);
            c.setMetodoFinal(true);
        }
        new MiembroSinVisibilidad().parse(c);
    }
}

final class VisibilidadOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.setVisibilidadActual("public");
        if (c.es(TokenType.kw_public)) {
            c.match(TokenType.kw_public);
        } else if (c.es(TokenType.kw_private)) {
            c.match(TokenType.kw_private);
            c.setVisibilidadActual("private");
        } else if (c.es(TokenType.kw_protected)) {
            c.match(TokenType.kw_protected);
            c.setVisibilidadActual("protected");
        }
    }
}

final class MiembroSinVisibilidad implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.lessThan)) {
            parseParametroGenericoMetodo(c);
            boolean estatico = c.es(TokenType.kw_static);
            if (estatico)
                c.match(TokenType.kw_static);
            registrarMetodo(c, new TipoMetodo().parseTipoMetodo(c), estatico);
            return;
        }
        if (c.es(TokenType.kw_static)) {
            c.match(TokenType.kw_static);
            registrarMetodo(c, new TipoMetodo().parseTipoMetodo(c), true);
            return;
        }
        if (c.es(TokenType.kw_void)) {
            registrarMetodo(c, new TipoMetodo().parseTipoMetodo(c), false);
            return;
        }
        if (ContextoSintactico.cualquiera(c, TokenType.kw_boolean, TokenType.kw_char, TokenType.kw_int)) {
            registrarDeclaracion(c, new Tipo().parseTipo(c), false);
            return;
        }
        if (c.es(TokenType.IdentificadorDeParametroDeTipo)) {
            registrarDeclaracion(c, new Tipo().parseTipo(c), false);
            return;
        }
        registrarDeclaracion(c, new Tipo().parseTipo(c), true);
    }

    private void parseParametroGenericoMetodo(ContextoSintactico c) {
        c.match(TokenType.lessThan);
        Token parametro = c.match(TokenType.IdentificadorDeParametroDeTipo);
        c.setParametroGenericoMetodo(parametro.getLexema());
        c.match(TokenType.greaterThan);
    }

    private void registrarMetodo(ContextoSintactico c, analizadorSemantico.Tipo tipoRetorno, boolean estatico) {
        Token tokenNombre = c.match(TokenType.identificador);
        if (estatico && c.es(TokenType.semicolon)) {
            if (tipoRetorno instanceof analizadorSemantico.TipoPrimitivo
                    && ((analizadorSemantico.TipoPrimitivo) tipoRetorno).getPrimitivo()
                    == analizadorSemantico.TipoPrimitivo.Primitivo.VOID)
                throw new ExcepcionSintactica(tokenNombre, "tipo de atributo");
            c.match(TokenType.semicolon);
                c.getClaseActual().agregarAtributo(new EntradaAtributo(
                    tokenNombre, tokenNombre.getLexema(), tipoRetorno, c.getVisibilidadActual(), true));
            return;
        }
        EntradaMetodo metodo = new EntradaMetodo(tokenNombre, tokenNombre.getLexema(), tipoRetorno,
            c.getVisibilidadActual(), estatico);
        metodo.setParametroGenerico(c.getParametroGenericoMetodo());
        metodo.setFinalMethod(c.isMetodoFinal());
        c.setMetodoActual(metodo);
        new ArgsFormales().parse(c);
        c.getClaseActual().agregarMetodo(metodo);
        new Bloque().parse(c);
        c.setMetodoActual(null);
        c.setParametroGenericoMetodo(null);
    }

    private void registrarDeclaracion(ContextoSintactico c, analizadorSemantico.Tipo tipo, boolean puedeSerConstructor) {
        if (puedeSerConstructor && c.es(TokenType.openParenthesis)) {
            EntradaClase clase = c.getClaseActual();
            if (tipo instanceof analizadorSemantico.TipoReferencia
                && !clase.getNombre().equals(tipo.getNombre()))
            throw new analizadorSemantico.ExcepcionSemantica(tipo.getToken(),
                "el constructor debe llamarse '" + clase.getNombre() + "'");
            EntradaMetodo constructor = new EntradaMetodo(clase.getToken(), clase.getNombre(), null,
                c.getVisibilidadActual(), false);
            c.setMetodoActual(constructor);
            new ArgsFormales().parse(c);
            clase.agregarConstructor(constructor);
            new Bloque().parse(c);
            c.setMetodoActual(null);
            return;
        }

        Token tokenNombre = c.match(TokenType.identificador);
        if (c.es(TokenType.semicolon)) {
            c.match(TokenType.semicolon);
            c.getClaseActual().agregarAtributo(new EntradaAtributo(tokenNombre, tokenNombre.getLexema(), tipo));
            return;
        }

        EntradaMetodo metodo = new EntradaMetodo(tokenNombre, tokenNombre.getLexema(), tipo,
            c.getVisibilidadActual(), false);
        metodo.setFinalMethod(c.isMetodoFinal());
        c.setMetodoActual(metodo);
        new ArgsFormales().parse(c);
        c.getClaseActual().agregarMetodo(metodo);
        new Bloque().parse(c);
        c.setMetodoActual(null);
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

final class RestoMiembroIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.openParenthesis)) {
            new ArgsFormales().parse(c);
            new Bloque().parse(c);
        } else {
            new TipoGenericoOpcional().parse(c);
            new DimensionesOpcionales().parse(c);
            c.match(TokenType.identificador);
            new RestoMiembroConTipo().parse(c);
        }
    }
}

final class MetodoInterfaz implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new VisibilidadOpcional().parse(c);
        if (c.es(TokenType.lessThan)) {
            c.match(TokenType.lessThan);
            Token parametro = c.match(TokenType.IdentificadorDeParametroDeTipo);
            c.setParametroGenericoMetodo(parametro.getLexema());
            c.match(TokenType.greaterThan);
        }
        analizadorSemantico.Tipo tipoRetorno = new TipoMetodo().parseTipoMetodo(c);
        Token tokenNombre = c.match(TokenType.identificador);
        EntradaMetodo metodo = new EntradaMetodo(tokenNombre, tokenNombre.getLexema(), tipoRetorno);
        metodo.setParametroGenerico(c.getParametroGenericoMetodo());
        c.setMetodoActual(metodo);
        new ArgsFormales().parse(c);
        c.getClaseActual().agregarMetodo(metodo);
        c.setMetodoActual(null);
        c.setParametroGenericoMetodo(null);
        c.match(TokenType.semicolon);
    }
}

final class TipoMetodo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        parseTipoMetodo(c);
    }

    public analizadorSemantico.Tipo parseTipoMetodo(ContextoSintactico c) {
        if (c.es(TokenType.kw_void))
            return new analizadorSemantico.TipoPrimitivo(c.match(TokenType.kw_void),
                    analizadorSemantico.TipoPrimitivo.Primitivo.VOID);
        return new Tipo().parseTipo(c);
    }
}

final class Tipo implements NoTerminal {
    public void parse(ContextoSintactico c) {
        parseTipo(c);
    }

    public analizadorSemantico.Tipo parseTipo(ContextoSintactico c) {
        analizadorSemantico.Tipo tipo;
        if (c.es(TokenType.kw_boolean)) {
            tipo = new analizadorSemantico.TipoPrimitivo(c.match(TokenType.kw_boolean),
                    analizadorSemantico.TipoPrimitivo.Primitivo.BOOLEAN);
        } else if (c.es(TokenType.kw_char)) {
            tipo = new analizadorSemantico.TipoPrimitivo(c.match(TokenType.kw_char),
                    analizadorSemantico.TipoPrimitivo.Primitivo.CHAR);
        } else if (c.es(TokenType.kw_int)) {
            tipo = new analizadorSemantico.TipoPrimitivo(c.match(TokenType.kw_int),
                    analizadorSemantico.TipoPrimitivo.Primitivo.INT);
        } else if (c.es(TokenType.identificadorDeClase)) {
            Token tokenTipo = c.match(TokenType.identificadorDeClase);
            tipo = new analizadorSemantico.TipoReferencia(tokenTipo, tokenTipo.getLexema());
            tipo = agregarArgumentoGenerico(c, (analizadorSemantico.TipoReferencia) tipo);
        } else if (c.es(TokenType.IdentificadorDeParametroDeTipo)) {
            Token tokenTipo = c.match(TokenType.IdentificadorDeParametroDeTipo);
            tipo = new analizadorSemantico.TipoReferencia(tokenTipo, tokenTipo.getLexema());
        } else {
            throw new ExcepcionSintactica(c.actual(), "tipo");
        }

        while (c.es(TokenType.openSquareBracket)) {
            Token tokenArreglo = c.match(TokenType.openSquareBracket);
            c.match(TokenType.closeSquareBracket);
            tipo = new analizadorSemantico.TipoArreglo(tokenArreglo, tipo);
        }
        return tipo;
    }

    private analizadorSemantico.Tipo agregarArgumentoGenerico(ContextoSintactico c,
                                                               analizadorSemantico.TipoReferencia tipo) {
        if (c.es(TokenType.lessThan)) {
            c.match(TokenType.lessThan);
            tipo.setArgumentoGenerico(parseTipoBaseGenerico(c));
            c.match(TokenType.greaterThan);
        }
        return tipo;
    }

    private analizadorSemantico.Tipo parseTipoBaseGenerico(ContextoSintactico c) {
        if (c.es(TokenType.identificadorDeClase)) {
            Token token = c.match(TokenType.identificadorDeClase);
            analizadorSemantico.TipoReferencia tipo = new analizadorSemantico.TipoReferencia(token, token.getLexema());
            if (c.es(TokenType.lessThan)) {
                c.match(TokenType.lessThan);
                tipo.setArgumentoGenerico(parseTipoBaseGenerico(c));
                c.match(TokenType.greaterThan);
            }
            return tipo;
        }
        Token token = c.match(TokenType.IdentificadorDeParametroDeTipo);
        return new analizadorSemantico.TipoReferencia(token, token.getLexema());
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
        analizadorSemantico.Tipo tipo = new Tipo().parseTipo(c);
        Token tokenNombre = c.match(TokenType.identificador);
        if (c.getMetodoActual() != null)
            c.getMetodoActual().agregarParametro(new EntradaParametro(tokenNombre, tokenNombre.getLexema(), tipo));
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
        if (c.es(TokenType.kw_for)) {
            new For().parse(c);
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
        if (!c.es(TokenType.semicolon) && !c.es(TokenType.closeParenthesis))
            new Expresion().parse(c);
    }
}

final class For implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.kw_for);
        c.match(TokenType.openParenthesis);
        new CabeceraFor().parse(c);
        c.match(TokenType.closeParenthesis);
        new Sentencia().parse(c);
    }
}

final class CabeceraFor implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.identificadorDeClase))
            new CabeceraForIdClase().parse(c);
        else
            new CabeceraForSinIdClase().parse(c);
    }
}

final class CabeceraForIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        c.match(TokenType.identificadorDeClase);
        new TrasIdClaseEnFor().parse(c);
    }
}

final class TrasIdClaseEnFor implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (c.es(TokenType.period)) {
            c.match(TokenType.period);
            c.match(TokenType.identificador);
            new ArgsActuales().parse(c);
            new Referencia2().parse(c);
            new ExpresionCompuesta2().parse(c);
            new Expresion2().parse(c);
            c.match(TokenType.semicolon);
            new ExpresionOpcional().parse(c);
            c.match(TokenType.semicolon);
            new ExpresionOpcional().parse(c);
            return;
        }
        new TipoGenericoOpcional().parse(c);
        new DimensionesOpcionales().parse(c);
        c.match(TokenType.identificador);
        c.match(TokenType.twopoints);
        new Expresion().parse(c);
    }
}

final class CabeceraForSinIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.kw_boolean, TokenType.kw_char,
                TokenType.kw_int, TokenType.IdentificadorDeParametroDeTipo)) {
            new TipoBaseSinIdClase().parse(c);
            new DimensionesOpcionales().parse(c);
            c.match(TokenType.identificador);
            c.match(TokenType.twopoints);
            new Expresion().parse(c);
            return;
        }
        new ExpresionOpcionalSinIdClase().parse(c);
        c.match(TokenType.semicolon);
        new ExpresionOpcional().parse(c);
        c.match(TokenType.semicolon);
        new ExpresionOpcional().parse(c);
    }
}

final class TipoBaseSinIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.kw_boolean, TokenType.kw_char, TokenType.kw_int))
            new TipoPrimitivo().parse(c);
        else
            c.match(TokenType.IdentificadorDeParametroDeTipo);
    }
}

final class ExpresionOpcionalSinIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (!c.es(TokenType.semicolon))
            new ExpresionSinIdClase().parse(c);
    }
}

final class ExpresionSinIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new ExpresionCompuestaSinIdClase().parse(c);
        new Expresion2().parse(c);
    }
}

final class ExpresionCompuestaSinIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new ExpresionBasicaSinIdClase().parse(c);
        new ExpresionCompuesta2().parse(c);
    }
}

final class ExpresionBasicaSinIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.plus, TokenType.minus, TokenType.not))
            new OperadorUnario().parse(c);
        new OperandoSinIdClase().parse(c);
    }
}

final class OperandoSinIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.kw_true, TokenType.kw_false,
                TokenType.intLiteral, TokenType.charLiteral, TokenType.kw_null))
            new Primitivo().parse(c);
        else
            new ReferenciaSinIdClase().parse(c);
    }
}

final class ReferenciaSinIdClase implements NoTerminal {
    public void parse(ContextoSintactico c) {
        new PrimarioSinIdClase().parse(c);
        new Referencia2().parse(c);
    }
}

final class PrimarioSinIdClase implements NoTerminal {
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
        if (c.es(TokenType.identificador)) {
            new AccesoVarLlamadaMetodo().parse(c);
            return;
        }
        new ExpresionParentizada().parse(c);
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
            new DimensionesConTamanio().parse(c);
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
