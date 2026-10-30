package analizadorSemantico;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Structural type equality, generic views, and subtype conformance for body checks. */
public final class ConformidadTipos {
    private ConformidadTipos() {}

    public static boolean conforma(Tipo origen, Tipo destino, TablaSimbolos tabla) {
        if (origen == null || destino == null || esVoid(origen) || esVoid(destino))
            return false;
        if (iguales(origen, destino))
            return true;
        if (origen instanceof TipoNull)
            return destino instanceof TipoReferencia || destino instanceof TipoArreglo;
        if (origen instanceof TipoParametro || destino instanceof TipoParametro)
            return false;
        if (origen instanceof TipoArreglo) {
            return destino instanceof TipoReferencia
                    && "Object".equals(((TipoReferencia) destino).getNombreClase())
                    && ((TipoReferencia) destino).getArgumentoGenerico() == null;
        }
        if (!(origen instanceof TipoReferencia) || !(destino instanceof TipoReferencia))
            return false;
        TipoReferencia referenciaOrigen = (TipoReferencia) origen;
        TipoReferencia referenciaDestino = (TipoReferencia) destino;
        if (referenciaOrigen.esParametroTipo() || referenciaDestino.esParametroTipo())
            return false;
        if ("Object".equals(referenciaDestino.getNombreClase())
                && referenciaDestino.getArgumentoGenerico() == null)
            return true;
        return buscarAncestro(referenciaOrigen, referenciaDestino, tabla, new HashSet<>());
    }

    public static boolean iguales(Tipo primero, Tipo segundo) {
        if (primero == null || segundo == null)
            return primero == segundo;
        if (esVoid(primero) || esVoid(segundo))
            return esVoid(primero) && esVoid(segundo);
        if (primero instanceof TipoNull || segundo instanceof TipoNull)
            return primero instanceof TipoNull && segundo instanceof TipoNull;
        if (primero instanceof TipoArreglo || segundo instanceof TipoArreglo)
            return primero instanceof TipoArreglo && segundo instanceof TipoArreglo
                    && iguales(((TipoArreglo) primero).getTipoElemento(),
                            ((TipoArreglo) segundo).getTipoElemento());
        if (primero instanceof TipoPrimitivo || segundo instanceof TipoPrimitivo)
            return primero instanceof TipoPrimitivo && segundo instanceof TipoPrimitivo
                    && ((TipoPrimitivo) primero).getPrimitivo()
                            == ((TipoPrimitivo) segundo).getPrimitivo();
        if (!(primero instanceof TipoReferencia) || !(segundo instanceof TipoReferencia))
            return false;
        TipoReferencia referenciaPrimera = (TipoReferencia) primero;
        TipoReferencia referenciaSegunda = (TipoReferencia) segundo;
        if (referenciaPrimera.esParametroTipo() || referenciaSegunda.esParametroTipo())
            return referenciaPrimera.esParametroTipo() && referenciaSegunda.esParametroTipo()
                    && referenciaPrimera.getParametroTipo() == referenciaSegunda.getParametroTipo();
        return referenciaPrimera.getNombreClase().equals(referenciaSegunda.getNombreClase())
                && iguales(referenciaPrimera.getArgumentoGenerico(),
                        referenciaSegunda.getArgumentoGenerico());
    }

    public static Tipo vista(Tipo tipo, Map<ParametroTipo, Tipo> sustituciones) {
        return vista(tipo, sustituciones, new HashSet<>());
    }

    private static Tipo vista(Tipo tipo, Map<ParametroTipo, Tipo> sustituciones,
                              Set<ParametroTipo> parametrosEnExpansion) {
        if (tipo == null)
            return null;
        if (tipo instanceof TipoReferencia) {
            TipoReferencia referencia = (TipoReferencia) tipo;
            if (referencia.esParametroTipo()) {
                ParametroTipo parametro = referencia.getParametroTipo();
                Tipo sustituto = sustituciones.get(parametro);
                if (sustituto == null || esMismoParametro(sustituto, parametro)
                        || !parametrosEnExpansion.add(parametro))
                    return new TipoParametro(referencia.getToken(), parametro);
                try {
                    return vista(sustituto, sustituciones, parametrosEnExpansion);
                } finally {
                    parametrosEnExpansion.remove(parametro);
                }
            }
            return new TipoReferencia(referencia.getToken(), referencia.getNombreClase(),
                    vista(referencia.getArgumentoGenerico(), sustituciones, parametrosEnExpansion));
        }
        if (tipo instanceof TipoArreglo)
            return new TipoArreglo(tipo.getToken(), vista(((TipoArreglo) tipo).getTipoElemento(), sustituciones,
                    parametrosEnExpansion));
        if (tipo instanceof TipoPrimitivo)
            return new TipoPrimitivo(tipo.getToken(), ((TipoPrimitivo) tipo).getPrimitivo());
        if (tipo instanceof TipoNull)
            return new TipoNull(tipo.getToken());
        if (tipo instanceof TipoVoid)
            return new TipoVoid(tipo.getToken());
        return tipo;
    }

    private static boolean esMismoParametro(Tipo tipo, ParametroTipo parametro) {
        return tipo instanceof TipoReferencia && ((TipoReferencia) tipo).esParametroTipo()
                && ((TipoReferencia) tipo).getParametroTipo() == parametro;
    }

    private static boolean buscarAncestro(TipoReferencia actual, TipoReferencia destino,
                                           TablaSimbolos tabla, Set<String> visitados) {
        String clave = clave(actual);
        if (!visitados.add(clave))
            return false;
        if (actual.getNombreClase().equals(destino.getNombreClase()))
            return iguales(actual.getArgumentoGenerico(), destino.getArgumentoGenerico());

        EntradaClase entrada = tabla.getClase(actual.getNombreClase());
        if (entrada == null)
            return false;
        Map<ParametroTipo, Tipo> sustituciones = sustitucionesDe(entrada, actual);
        if (entrada.getClaseBase() != null) {
            TipoReferencia padre = (TipoReferencia) vista(entrada.getClaseBase(), sustituciones);
            padre = completarRaw(padre, tabla.getClase(padre.getNombreClase()));
            if (buscarAncestro(padre, destino, tabla, visitados))
                return true;
        }
        for (TipoReferencia interfazDeclarada : entrada.getInterfaces()) {
            TipoReferencia interfaz = (TipoReferencia) vista(interfazDeclarada, sustituciones);
            interfaz = completarRaw(interfaz, tabla.getClase(interfaz.getNombreClase()));
            if (buscarAncestro(interfaz, destino, tabla, visitados))
                return true;
        }
        return false;
    }

    private static Map<ParametroTipo, Tipo> sustitucionesDe(EntradaClase clase,
                                                             TipoReferencia actual) {
        ParametroTipo formal = clase.getSimboloParametroGenerico();
        if (formal == null)
            return Collections.emptyMap();
        Tipo argumento = actual.getArgumentoGenerico();
        if (argumento == null)
            argumento = new TipoParametro(formal.getToken(), formal);
        return Collections.singletonMap(formal, argumento);
    }

    private static TipoReferencia completarRaw(TipoReferencia referencia, EntradaClase destino) {
        if (destino == null || destino.getSimboloParametroGenerico() == null
                || referencia.getArgumentoGenerico() != null)
            return referencia;
        ParametroTipo formal = destino.getSimboloParametroGenerico();
        return new TipoReferencia(referencia.getToken(), referencia.getNombreClase(),
                new TipoParametro(formal.getToken(), formal));
    }

    private static boolean esVoid(Tipo tipo) {
        return tipo instanceof TipoVoid
                || tipo instanceof TipoPrimitivo
                    && ((TipoPrimitivo) tipo).getPrimitivo() == TipoPrimitivo.Primitivo.VOID;
    }

    private static String clave(Tipo tipo) {
        if (tipo instanceof TipoParametro)
            return "P" + System.identityHashCode(((TipoParametro) tipo).getParametro());
        if (tipo instanceof TipoArreglo)
            return "[" + clave(((TipoArreglo) tipo).getTipoElemento()) + "]";
        if (tipo instanceof TipoReferencia) {
            TipoReferencia referencia = (TipoReferencia) tipo;
            return referencia.getNombreClase() + "<" + clave(referencia.getArgumentoGenerico()) + ">";
        }
        return tipo == null ? "-" : tipo.getNombre();
    }
}
