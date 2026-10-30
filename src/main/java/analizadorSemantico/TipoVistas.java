package analizadorSemantico;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Builds immutable member-type views for a receiver along its inheritance graph. */
public final class TipoVistas {
    private TipoVistas() {}

    public static Tipo vista(Tipo declarado, Tipo tipoReceptor, EntradaClase claseDeclaracion,
                             TablaSimbolos tabla) {
        if (declarado == null || claseDeclaracion == null || !(tipoReceptor instanceof TipoReferencia))
            return declarado;
        TipoReferencia receptor = (TipoReferencia) tipoReceptor;
        if (receptor.esParametroTipo())
            return declarado;
        Map<ParametroTipo, Tipo> sustituciones = buscarSustituciones(
                receptor, claseDeclaracion.getNombre(), tabla, new HashSet<>());
        return sustituciones == null ? declarado : ConformidadTipos.vista(declarado, sustituciones);
    }

    private static Map<ParametroTipo, Tipo> buscarSustituciones(
            TipoReferencia actual, String destino, TablaSimbolos tabla, Set<String> visitados) {
        String clave = actual.getNombreClase() + "<" + tipoClave(actual.getArgumentoGenerico()) + ">";
        if (!visitados.add(clave))
            return null;
        EntradaClase clase = tabla.getClase(actual.getNombreClase());
        if (clase == null)
            return null;
        Map<ParametroTipo, Tipo> sustituciones = sustitucionesDe(clase, actual);
        if (clase.getNombre().equals(destino))
            return sustituciones;

        if (clase.getClaseBase() != null) {
            TipoReferencia padre = (TipoReferencia) ConformidadTipos.vista(
                    clase.getClaseBase(), sustituciones);
            padre = completarRaw(padre, tabla.getClase(padre.getNombreClase()));
            Map<ParametroTipo, Tipo> resultado = buscarSustituciones(padre, destino, tabla, visitados);
            if (resultado != null)
                return resultado;
        }
        for (TipoReferencia interfazDeclarada : clase.getInterfaces()) {
            TipoReferencia interfaz = (TipoReferencia) ConformidadTipos.vista(
                    interfazDeclarada, sustituciones);
            interfaz = completarRaw(interfaz, tabla.getClase(interfaz.getNombreClase()));
            Map<ParametroTipo, Tipo> resultado = buscarSustituciones(interfaz, destino, tabla, visitados);
            if (resultado != null)
                return resultado;
        }
        return null;
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

    private static String tipoClave(Tipo tipo) {
        if (tipo == null)
            return "-";
        if (tipo instanceof TipoParametro)
            return "P" + System.identityHashCode(((TipoParametro) tipo).getParametro());
        if (tipo instanceof TipoArreglo)
            return "[" + tipoClave(((TipoArreglo) tipo).getTipoElemento()) + "]";
        if (tipo instanceof TipoReferencia) {
            TipoReferencia referencia = (TipoReferencia) tipo;
            return referencia.getNombreClase() + "<" + tipoClave(referencia.getArgumentoGenerico()) + ">";
        }
        return tipo.getNombre();
    }
}
