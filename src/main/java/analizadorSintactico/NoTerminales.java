package analizadorSintactico;

import analizadorLexico.TokenType;
import analizadorLexico.Token;
import analizadorSemantico.EntradaAtributo;
import analizadorSemantico.EntradaClase;
import analizadorSemantico.EntradaMetodo;
import analizadorSemantico.EntradaParametro;
import analizadorSemantico.ast.BloqueAst;
import analizadorSemantico.ast.CicloForAst;
import analizadorSemantico.ast.ExpresionAst;
import analizadorSemantico.ast.NodoExpresionAst;
import analizadorSemantico.ast.NodoSentenciaAst;
import analizadorSemantico.ast.SentenciaAst;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static analizadorSintactico.AstNodos.sentencia;
import static analizadorSintactico.AstNodos.expresion;

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
                c.getClaseActual().setParametroGenerico(parametro.getLexema(), parametro);
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
        do {
            Token parametro = c.match(TokenType.IdentificadorDeParametroDeTipo);
            java.util.List<analizadorSemantico.Tipo> bounds = new java.util.ArrayList<>();
            c.registrarParametroGenericoMetodo(parametro, bounds);
            if (c.es(TokenType.kw_extends)) {
                c.match(TokenType.kw_extends);
                bounds.add(new Tipo().parseTipo(c));
                while (c.es(TokenType.and)) {
                    c.match(TokenType.and);
                    bounds.add(new Tipo().parseTipo(c));
                }
            }
            if (!c.es(TokenType.comma))
                break;
            c.match(TokenType.comma);
        } while (true);
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
        for (java.util.Map.Entry<String, java.util.List<analizadorSemantico.Tipo>> entrada
                : c.getParametrosGenericosMetodo().entrySet())
            metodo.agregarParametroGenerico(
                    c.getSimbolosParametrosGenericosMetodo().get(entrada.getKey()), entrada.getValue());
        metodo.setFinalMethod(c.isMetodoFinal());
        c.setMetodoActual(metodo);
        new ArgsFormales().parse(c);
        try {
            c.getClaseActual().agregarMetodo(metodo);
        } catch (analizadorSemantico.ExcepcionSemantica error) {
            c.registrarError(error);
        }
        metodo.setCuerpo(new Bloque().parse(c));
        c.setMetodoActual(null);
        c.limpiarParametrosGenericosMetodo();
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
            try {
                clase.agregarConstructor(constructor);
            } catch (analizadorSemantico.ExcepcionSemantica error) {
                c.registrarError(error);
            }
            constructor.setCuerpo(new Bloque().parse(c));
            c.setMetodoActual(null);
            return;
        }

        Token tokenNombre = c.match(TokenType.identificador);
        if (c.es(TokenType.semicolon)) {
            c.match(TokenType.semicolon);
            try {
                c.getClaseActual().agregarAtributo(new EntradaAtributo(tokenNombre, tokenNombre.getLexema(), tipo));
            } catch (analizadorSemantico.ExcepcionSemantica error) {
                c.registrarError(error);
            }
            return;
        }

        EntradaMetodo metodo = new EntradaMetodo(tokenNombre, tokenNombre.getLexema(), tipo,
            c.getVisibilidadActual(), false);
        metodo.setFinalMethod(c.isMetodoFinal());
        c.setMetodoActual(metodo);
        new ArgsFormales().parse(c);
        try {
            c.getClaseActual().agregarMetodo(metodo);
        } catch (analizadorSemantico.ExcepcionSemantica error) {
            c.registrarError(error);
        }
        metodo.setCuerpo(new Bloque().parse(c));
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
            c.registrarParametroGenericoMetodo(parametro, new java.util.ArrayList<>());
            c.match(TokenType.greaterThan);
        }
        analizadorSemantico.Tipo tipoRetorno = new TipoMetodo().parseTipoMetodo(c);
        Token tokenNombre = c.match(TokenType.identificador);
        EntradaMetodo metodo = new EntradaMetodo(tokenNombre, tokenNombre.getLexema(), tipoRetorno);
        for (java.util.Map.Entry<String, java.util.List<analizadorSemantico.Tipo>> entrada
                : c.getParametrosGenericosMetodo().entrySet())
            metodo.agregarParametroGenerico(
                    c.getSimbolosParametrosGenericosMetodo().get(entrada.getKey()), entrada.getValue());
        c.setMetodoActual(metodo);
        new ArgsFormales().parse(c);
        try {
            c.getClaseActual().agregarMetodo(metodo);
        } catch (analizadorSemantico.ExcepcionSemantica error) {
            c.registrarError(error);
        }
        c.setMetodoActual(null);
        c.limpiarParametrosGenericosMetodo();
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
            analizadorSemantico.ParametroTipo parametro = c.buscarParametroTipo(tokenTipo.getLexema());
            tipo = parametro == null
                    ? new analizadorSemantico.TipoReferencia(tokenTipo, tokenTipo.getLexema())
                    : new analizadorSemantico.TipoParametro(tokenTipo, parametro);
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
        analizadorSemantico.ParametroTipo parametro = c.buscarParametroTipo(token.getLexema());
        return parametro == null
                ? new analizadorSemantico.TipoReferencia(token, token.getLexema())
                : new analizadorSemantico.TipoParametro(token, parametro);
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
        if (c.getMetodoActual() != null) {
            try {
                c.getMetodoActual().agregarParametro(new EntradaParametro(tokenNombre, tokenNombre.getLexema(), tipo));
            } catch (analizadorSemantico.ExcepcionSemantica error) {
                c.registrarError(error);
            }
        }
    }
}

final class Bloque {
    public BloqueAst parse(ContextoSintactico c) {
        Token tokenBloque = c.match(TokenType.openBraces);
        List<SentenciaAst> sentencias = new ListaSentencias().parse(c);
        c.match(TokenType.closeBraces);
        return new BloqueAst(tokenBloque, sentencias);
    }
}

final class ListaSentencias {
    public List<SentenciaAst> parse(ContextoSintactico c) {
        List<SentenciaAst> sentencias = new ArrayList<>();
        while (!c.es(TokenType.closeBraces))
            sentencias.add(new Sentencia().parse(c));
        return sentencias;
    }
}

final class Sentencia {
    public SentenciaAst parse(ContextoSintactico c) {
        if (c.es(TokenType.semicolon)) {
            Token token = c.match(TokenType.semicolon);
            return sentencia(token, NodoSentenciaAst.Clase.VACIA, null, null,
                    Collections.emptyList(), Collections.emptyList());
        }
        if (c.es(TokenType.openBraces))
            return new Bloque().parse(c);
        if (c.es(TokenType.kw_var))
            return new VarLocal().parse(c);
        if (c.es(TokenType.kw_return))
            return new Return().parse(c);
        if (c.es(TokenType.kw_if))
            return new If().parse(c);
        if (c.es(TokenType.kw_while))
            return new While().parse(c);
        if (c.es(TokenType.kw_for))
            return new For().parse(c);
        ExpresionAst expresion = new Expresion().parse(c);
        c.match(TokenType.semicolon);
        return sentencia(expresion.getToken(), NodoSentenciaAst.Clase.EXPRESION, null, null,
                Collections.singletonList(expresion), Collections.emptyList());
    }
}

final class VarLocal {
    public SentenciaAst parse(ContextoSintactico c) {
        Token tokenVar = c.match(TokenType.kw_var);
        Token nombre = c.match(TokenType.identificador);
        c.match(TokenType.assignment);
        ExpresionAst inicializador = new Expresion().parseCompuesta(c);
        c.match(TokenType.semicolon);
        return sentencia(tokenVar, NodoSentenciaAst.Clase.VARIABLE_LOCAL, nombre, null,
                Collections.singletonList(inicializador), Collections.emptyList());
    }
}

final class Return {
    public SentenciaAst parse(ContextoSintactico c) {
        Token tokenReturn = c.match(TokenType.kw_return);
        ExpresionAst expresion = null;
        if (!c.es(TokenType.semicolon))
            expresion = new Expresion().parse(c);
        c.match(TokenType.semicolon);
        return sentencia(tokenReturn, NodoSentenciaAst.Clase.RETURN, null, null,
                expresion == null ? Collections.emptyList() : Collections.singletonList(expresion),
                Collections.emptyList());
    }
}

final class ExpresionOpcional implements NoTerminal {
    public void parse(ContextoSintactico c) {
        if (!c.es(TokenType.semicolon) && !c.es(TokenType.closeParenthesis))
            new Expresion().parse(c);
    }
}

final class For {
    public SentenciaAst parse(ContextoSintactico c) {
        Token tokenFor = c.match(TokenType.kw_for);
        c.match(TokenType.openParenthesis);
        if (c.es(TokenType.identificadorDeClase)) {
            Token inicio = c.match(TokenType.identificadorDeClase);
            if (c.es(TokenType.period)) {
                ExpresionAst inicializacion = new Expresion().parseForDesdeClase(c, inicio);
                return parseClasico(c, tokenFor, inicializacion);
            }
            analizadorSemantico.Tipo tipo = parseTipoDesdeClase(c, inicio);
            Token nombre = c.match(TokenType.identificador);
            c.match(TokenType.twopoints);
            ExpresionAst iterable = new Expresion().parse(c);
            c.match(TokenType.closeParenthesis);
            SentenciaAst cuerpo = new Sentencia().parse(c);
            return new CicloForAst(tokenFor, CicloForAst.Forma.MEJORADO, tipo, nombre,
                    null, null, null, iterable, cuerpo);
        }
        if (ContextoSintactico.cualquiera(c, TokenType.kw_boolean, TokenType.kw_char,
                TokenType.kw_int, TokenType.IdentificadorDeParametroDeTipo)) {
            analizadorSemantico.Tipo tipo = new Tipo().parseTipo(c);
            Token nombre = c.match(TokenType.identificador);
            c.match(TokenType.twopoints);
            ExpresionAst iterable = new Expresion().parse(c);
            c.match(TokenType.closeParenthesis);
            SentenciaAst cuerpo = new Sentencia().parse(c);
            return new CicloForAst(tokenFor, CicloForAst.Forma.MEJORADO, tipo, nombre,
                    null, null, null, iterable, cuerpo);
        }
        ExpresionAst inicializacion = c.es(TokenType.semicolon) ? null : new Expresion().parse(c);
        return parseClasico(c, tokenFor, inicializacion);
    }

    private SentenciaAst parseClasico(ContextoSintactico c, Token tokenFor,
                                      ExpresionAst inicializacion) {
        c.match(TokenType.semicolon);
        ExpresionAst condicion = c.es(TokenType.semicolon) ? null : new Expresion().parse(c);
        c.match(TokenType.semicolon);
        ExpresionAst actualizacion = c.es(TokenType.closeParenthesis) ? null : new Expresion().parse(c);
        c.match(TokenType.closeParenthesis);
        SentenciaAst cuerpo = new Sentencia().parse(c);
        return new CicloForAst(tokenFor, CicloForAst.Forma.CLASICO, null, null,
                inicializacion, condicion, actualizacion, null, cuerpo);
    }

    private analizadorSemantico.Tipo parseTipoDesdeClase(ContextoSintactico c, Token nombreClase) {
        analizadorSemantico.Tipo argumento = null;
        if (c.es(TokenType.lessThan)) {
            c.match(TokenType.lessThan);
            argumento = new Tipo().parseTipo(c);
            c.match(TokenType.greaterThan);
        }
        analizadorSemantico.Tipo tipo = new analizadorSemantico.TipoReferencia(
                nombreClase, nombreClase.getLexema(), argumento);
        while (c.es(TokenType.openSquareBracket)) {
            Token corchete = c.match(TokenType.openSquareBracket);
            c.match(TokenType.closeSquareBracket);
            tipo = new analizadorSemantico.TipoArreglo(corchete, tipo);
        }
        return tipo;
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

final class If {
    public SentenciaAst parse(ContextoSintactico c) {
        Token tokenIf = c.match(TokenType.kw_if);
        c.match(TokenType.openParenthesis);
        ExpresionAst condicion = new Expresion().parse(c);
        c.match(TokenType.closeParenthesis);
        SentenciaAst ramaIf = new Sentencia().parse(c);
        SentenciaAst ramaElse = new If2().parse(c);
        List<SentenciaAst> ramas = ramaElse == null
                ? Collections.singletonList(ramaIf) : Arrays.asList(ramaIf, ramaElse);
        return sentencia(tokenIf, NodoSentenciaAst.Clase.IF, null, null,
                Collections.singletonList(condicion), ramas);
    }
}

final class If2 {
    public SentenciaAst parse(ContextoSintactico c) {
        if (c.es(TokenType.kw_else)) {
            c.match(TokenType.kw_else);
            return new Sentencia().parse(c);
        }
        return null;
    }
}

final class While {
    public SentenciaAst parse(ContextoSintactico c) {
        Token tokenWhile = c.match(TokenType.kw_while);
        c.match(TokenType.openParenthesis);
        ExpresionAst condicion = new Expresion().parse(c);
        c.match(TokenType.closeParenthesis);
        SentenciaAst cuerpo = new Sentencia().parse(c);
        return sentencia(tokenWhile, NodoSentenciaAst.Clase.WHILE, null, null,
                Collections.singletonList(condicion), Collections.singletonList(cuerpo));
    }
}

final class Expresion {
    public ExpresionAst parse(ContextoSintactico c) {
        return parseAsignacion(c);
    }

    private ExpresionAst parseAsignacion(ContextoSintactico c) {
        ExpresionAst izquierda = parseCompuesta(c);
        return parseAsignacionDesde(c, izquierda);
    }

    private ExpresionAst parseAsignacionDesde(ContextoSintactico c, ExpresionAst izquierda) {
        if (!c.es(TokenType.assignment))
            return izquierda;
        Token operador = c.match(TokenType.assignment);
        ExpresionAst derecha = parseCompuesta(c);
        return expresion(operador, NodoExpresionAst.Clase.ASIGNACION, null, null, null,
                false, Arrays.asList(izquierda, derecha));
    }

    public ExpresionAst parseCompuesta(ContextoSintactico c) {
        return parseCompuestaDesde(c, parseUnaria(c));
    }

    private ExpresionAst parseCompuestaDesde(ContextoSintactico c, ExpresionAst izquierda) {
        while (esOperadorBinario(c.actual().getTokenType())) {
            Token operador = c.actual();
            c.match(operador.getTokenType());
            ExpresionAst derecha = parseUnaria(c);
            izquierda = expresion(operador, NodoExpresionAst.Clase.BINARIO, null, null,
                    operador.getLexema(), false, Arrays.asList(izquierda, derecha));
        }
        return izquierda;
    }

    private boolean esOperadorBinario(TokenType operador) {
        switch (operador) {
            case or:
            case and:
            case equals:
            case notEquals:
            case lessThan:
            case greaterThan:
            case lessThanOrEqual:
            case greaterThanOrEqual:
            case plus:
            case minus:
            case multiply:
            case operatorSlash:
            case mod:
                return true;
            default:
                return false;
        }
    }

    private ExpresionAst parseUnaria(ContextoSintactico c) {
        if (ContextoSintactico.cualquiera(c, TokenType.plus, TokenType.minus, TokenType.not)) {
            Token operador = c.actual();
            c.match(operador.getTokenType());
            ExpresionAst operando = parsePostfija(c);
            return expresion(operador, NodoExpresionAst.Clase.UNARIO, null, null,
                    operador.getLexema(), false, Collections.singletonList(operando));
        }
        return parsePostfija(c);
    }

    private ExpresionAst parsePostfija(ContextoSintactico c) {
        return parsePostfijaDesde(c, parsePrimaria(c));
    }

    private ExpresionAst parsePostfijaDesde(ContextoSintactico c, ExpresionAst expresion) {
        while (true) {
            if (c.es(TokenType.period)) {
                c.match(TokenType.period);
                Token nombre = c.match(TokenType.identificador);
                if (c.es(TokenType.openParenthesis)) {
                    List<ExpresionAst> operandos = new ArrayList<>();
                    operandos.add(expresion);
                    operandos.addAll(parseArgumentos(c));
                    expresion = expresion(nombre, NodoExpresionAst.Clase.LLAMADA_MIEMBRO,
                            nombre, null, null, false, operandos);
                } else {
                    expresion = expresion(nombre, NodoExpresionAst.Clase.ACCESO_ATRIBUTO,
                            nombre, null, null, false, Collections.singletonList(expresion));
                }
            } else if (c.es(TokenType.openSquareBracket)) {
                Token corchete = c.match(TokenType.openSquareBracket);
                ExpresionAst indice = parseAsignacion(c);
                c.match(TokenType.closeSquareBracket);
                expresion = expresion(corchete, NodoExpresionAst.Clase.ACCESO_INDICE,
                        null, null, null, false, Arrays.asList(expresion, indice));
            } else if (ContextoSintactico.cualquiera(c, TokenType.addOne, TokenType.subtractOne)) {
                Token operador = c.actual();
                c.match(operador.getTokenType());
                expresion = expresion(operador, NodoExpresionAst.Clase.INCREMENTO,
                        null, null, operador.getLexema(), true, Collections.singletonList(expresion));
            } else {
                return expresion;
            }
        }
    }

    private ExpresionAst parsePrimaria(ContextoSintactico c) {
        Token token = c.actual();
        if (ContextoSintactico.cualquiera(c, TokenType.kw_true, TokenType.kw_false)) {
            c.match(token.getTokenType());
            return expresion(token, NodoExpresionAst.Clase.LITERAL_BOOLEAN, null, null,
                    token.getLexema(), false, Collections.emptyList());
        }
        if (c.es(TokenType.intLiteral)) {
            c.match(TokenType.intLiteral);
            return expresion(token, NodoExpresionAst.Clase.LITERAL_INT, null, null,
                    token.getLexema(), false, Collections.emptyList());
        }
        if (c.es(TokenType.charLiteral)) {
            c.match(TokenType.charLiteral);
            return expresion(token, NodoExpresionAst.Clase.LITERAL_CHAR, null, null,
                    token.getLexema(), false, Collections.emptyList());
        }
        if (c.es(TokenType.stringLiteral)) {
            c.match(TokenType.stringLiteral);
            return expresion(token, NodoExpresionAst.Clase.LITERAL_STRING, null, null,
                    token.getLexema(), false, Collections.emptyList());
        }
        if (c.es(TokenType.kw_null)) {
            c.match(TokenType.kw_null);
            return expresion(token, NodoExpresionAst.Clase.LITERAL_NULL, null, null,
                    token.getLexema(), false, Collections.emptyList());
        }
        if (c.es(TokenType.kw_this)) {
            c.match(TokenType.kw_this);
            return expresion(token, NodoExpresionAst.Clase.THIS, null, null,
                    null, false, Collections.emptyList());
        }
        if (c.es(TokenType.kw_new))
            return parseNuevo(c);
        if (c.es(TokenType.identificadorDeClase))
            return parseLlamadaEstatico(c);
        if (c.es(TokenType.identificador)) {
            Token nombre = c.match(TokenType.identificador);
            if (c.es(TokenType.openParenthesis))
                return expresion(nombre, NodoExpresionAst.Clase.LLAMADA_IMPLICITA, nombre,
                        null, null, false, parseArgumentos(c));
            return expresion(nombre, NodoExpresionAst.Clase.VARIABLE, nombre,
                    null, null, false, Collections.emptyList());
        }
        Token parentesis = c.match(TokenType.openParenthesis);
        ExpresionAst interior = parseAsignacion(c);
        c.match(TokenType.closeParenthesis);
        return expresion(parentesis, NodoExpresionAst.Clase.PARENTESIS, null, null,
                null, false, Collections.singletonList(interior));
    }

    private ExpresionAst parseLlamadaEstatico(ContextoSintactico c) {
        Token clase = c.match(TokenType.identificadorDeClase);
        return parseLlamadaEstaticoDesde(c, clase);
    }

    private ExpresionAst parseLlamadaEstaticoDesde(ContextoSintactico c, Token clase) {
        analizadorSemantico.Tipo tipoClase = new analizadorSemantico.TipoReferencia(clase, clase.getLexema());
        c.match(TokenType.period);
        Token metodo = c.match(TokenType.identificador);
        List<ExpresionAst> argumentos = parseArgumentos(c);
        return expresion(metodo, NodoExpresionAst.Clase.LLAMADA_ESTATICA, metodo,
                tipoClase, clase.getLexema(), false, argumentos);
    }

    public ExpresionAst parseForDesdeClase(ContextoSintactico c, Token clase) {
        ExpresionAst inicio = parseLlamadaEstaticoDesde(c, clase);
        inicio = parsePostfijaDesde(c, inicio);
        inicio = parseCompuestaDesde(c, inicio);
        return parseAsignacionDesde(c, inicio);
    }

    private ExpresionAst parseNuevo(ContextoSintactico c) {
        Token nuevo = c.match(TokenType.kw_new);
        analizadorSemantico.Tipo base;
        if (ContextoSintactico.cualquiera(c, TokenType.kw_boolean, TokenType.kw_char, TokenType.kw_int)) {
            Token primitivo = c.actual();
            c.match(primitivo.getTokenType());
            analizadorSemantico.TipoPrimitivo.Primitivo valor = primitivo.getTokenType() == TokenType.kw_boolean
                    ? analizadorSemantico.TipoPrimitivo.Primitivo.BOOLEAN
                    : primitivo.getTokenType() == TokenType.kw_char
                        ? analizadorSemantico.TipoPrimitivo.Primitivo.CHAR
                        : analizadorSemantico.TipoPrimitivo.Primitivo.INT;
            base = new analizadorSemantico.TipoPrimitivo(primitivo, valor);
        } else {
            Token nombre = c.actual();
            if (c.es(TokenType.identificadorDeClase)) {
                c.match(TokenType.identificadorDeClase);
                analizadorSemantico.Tipo argumento = null;
                if (c.es(TokenType.lessThan)) {
                    c.match(TokenType.lessThan);
                    argumento = new Tipo().parseTipo(c);
                    c.match(TokenType.greaterThan);
                }
                base = new analizadorSemantico.TipoReferencia(nombre, nombre.getLexema(), argumento);
            } else {
                c.match(TokenType.IdentificadorDeParametroDeTipo);
                analizadorSemantico.ParametroTipo parametro = c.buscarParametroTipo(nombre.getLexema());
                base = parametro == null
                        ? new analizadorSemantico.TipoReferencia(nombre, nombre.getLexema())
                        : new analizadorSemantico.TipoParametro(nombre, parametro);
            }
        }
        if (c.es(TokenType.openParenthesis)) {
            List<ExpresionAst> argumentos = parseArgumentos(c);
            return expresion(nuevo, NodoExpresionAst.Clase.NUEVO_OBJETO, base.getToken(), base,
                    null, false, argumentos);
        }
        List<ExpresionAst> tamanios = new ArrayList<>();
        do {
            Token corchete = c.match(TokenType.openSquareBracket);
            tamanios.add(parseAsignacion(c));
            c.match(TokenType.closeSquareBracket);
            base = new analizadorSemantico.TipoArreglo(corchete, base);
        } while (c.es(TokenType.openSquareBracket));
        return expresion(nuevo, NodoExpresionAst.Clase.NUEVO_ARREGLO, base.getToken(), base,
                null, false, tamanios);
    }

    private List<ExpresionAst> parseArgumentos(ContextoSintactico c) {
        c.match(TokenType.openParenthesis);
        List<ExpresionAst> argumentos = new ArrayList<>();
        if (!c.es(TokenType.closeParenthesis)) {
            argumentos.add(parseAsignacion(c));
            while (c.es(TokenType.comma)) {
                c.match(TokenType.comma);
                argumentos.add(parseAsignacion(c));
            }
        }
        c.match(TokenType.closeParenthesis);
        return argumentos;
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

final class AstNodos {
    private AstNodos() {}

    static NodoSentenciaAst sentencia(Token token, NodoSentenciaAst.Clase clase, Token nombre,
                                      analizadorSemantico.Tipo tipo, List<ExpresionAst> expresiones,
                                      List<SentenciaAst> sentencias) {
        return new NodoSentenciaAst(token, clase, nombre, tipo, expresiones, sentencias);
    }

    static NodoExpresionAst expresion(Token token, NodoExpresionAst.Clase clase, Token nombre,
                                      analizadorSemantico.Tipo tipo, String valor, boolean postfix,
                                      List<ExpresionAst> operandos) {
        return new NodoExpresionAst(token, clase, nombre, tipo, valor, postfix, operandos);
    }
}
