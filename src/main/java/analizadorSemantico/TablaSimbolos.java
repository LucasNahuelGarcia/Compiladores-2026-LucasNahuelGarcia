package analizadorSemantico;

import analizadorLexico.Token;
import analizadorLexico.TokenType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class TablaSimbolos {
    private Map<String, EntradaClase> clases;
    private List<ExcepcionSemantica> errores;

    public TablaSimbolos() {
        clases = new LinkedHashMap<>();
        errores = new ArrayList<>();
        inicializarEntornoPredefinido();
    }

    public void inicializarEntornoPredefinido() {
        if (clases.containsKey("Object")) {
            return;
        }

        EntradaClase object = new EntradaClase(tokenClase("Object"));
        EntradaClase string = new EntradaClase(tokenClase("String"));
        EntradaClase system = new EntradaClase(tokenClase("System"));

        string.setClaseBase(tipoReferencia("Object"));
        system.setClaseBase(tipoReferencia("Object"));

        EntradaMetodo debugPrint = new EntradaMetodo(
                tokenMiembro("debugPrint"),
                "debugPrint",
            tipoPrimitivo(TipoPrimitivo.Primitivo.INT),
                "public",
            true);
        object.agregarMetodo(debugPrint);

        object.agregarMetodo(new EntradaMetodo(
                tokenMiembro("toString"),
                "toString",
                tipoReferencia("String")));

        system.agregarMetodo(new EntradaMetodo(
                tokenMiembro("read"),
                "read",
                tipoPrimitivo(TipoPrimitivo.Primitivo.INT),
                "public",
                true));
        agregarMetodoConParametro(system, "println", TipoPrimitivo.Primitivo.VOID, null, null);
        agregarMetodoConParametro(system, "printB", TipoPrimitivo.Primitivo.VOID, "b", TipoPrimitivo.Primitivo.BOOLEAN);
        agregarMetodoConParametro(system, "printC", TipoPrimitivo.Primitivo.VOID, "c", TipoPrimitivo.Primitivo.CHAR);
        agregarMetodoConParametro(system, "printI", TipoPrimitivo.Primitivo.VOID, "i", TipoPrimitivo.Primitivo.INT);
        agregarMetodoConReferencia(system, "printS", "s", "String");
        agregarMetodoConParametro(system, "printBln", TipoPrimitivo.Primitivo.VOID, "b", TipoPrimitivo.Primitivo.BOOLEAN);
        agregarMetodoConParametro(system, "printCln", TipoPrimitivo.Primitivo.VOID, "c", TipoPrimitivo.Primitivo.CHAR);
        agregarMetodoConParametro(system, "printIln", TipoPrimitivo.Primitivo.VOID, "i", TipoPrimitivo.Primitivo.INT);
        agregarMetodoConReferencia(system, "printSln", "s", "String");

        agregarClase(object);
        agregarClase(string);
        agregarClase(system);
    }

    private void agregarMetodoConParametro(EntradaClase clase, String nombre, TipoPrimitivo.Primitivo retorno,
                                           String nombreParametro, TipoPrimitivo.Primitivo tipoParametro) {
        EntradaMetodo metodo = new EntradaMetodo(
                tokenMiembro(nombre),
                nombre,
                tipoPrimitivo(retorno),
                "public",
                true);
        if (nombreParametro != null) {
            metodo.agregarParametro(new EntradaParametro(
                    tokenMiembro(nombreParametro),
                    nombreParametro,
                    tipoPrimitivo(tipoParametro)));
        }
        clase.agregarMetodo(metodo);
    }

    private void agregarMetodoConReferencia(EntradaClase clase, String nombre, String nombreParametro,
                                            String nombreTipoParametro) {
        EntradaMetodo metodo = new EntradaMetodo(
                tokenMiembro(nombre),
                nombre,
                tipoPrimitivo(TipoPrimitivo.Primitivo.VOID),
                "public",
                true);
        metodo.agregarParametro(new EntradaParametro(
                tokenMiembro(nombreParametro),
                nombreParametro,
                tipoReferencia(nombreTipoParametro)));
        clase.agregarMetodo(metodo);
    }

    private TipoPrimitivo tipoPrimitivo(TipoPrimitivo.Primitivo primitivo) {
        return new TipoPrimitivo(tokenMiembro(primitivo.name().toLowerCase()), primitivo);
    }

    private TipoReferencia tipoReferencia(String nombreClase) {
        return new TipoReferencia(tokenClase(nombreClase), nombreClase);
    }

    private Token tokenClase(String lexema) {
        return new Token(TokenType.identificadorDeClase, lexema, 0);
    }

    private Token tokenMiembro(String lexema) {
        return new Token(TokenType.identificador, lexema, 0);
    }

    public void agregarClase(EntradaClase clase) {
        if (clases.containsKey(clase.getNombre())) {
            clases.remove(clase.getNombre());
            registrarError(new ExcepcionSemantica(clase.getToken(),
                    "la clase o interfaz '" + clase.getNombre() + "' ya fue declarada"));
            return;
        }
        clases.put(clase.getNombre(), clase);
    }

    public void registrarError(ExcepcionSemantica error) { errores.add(error); }

    public List<ExcepcionSemantica> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public EntradaClase getClase(String nombre) {
        return clases.get(nombre);
    }

    public Map<String, EntradaClase> getClases() {
        return Collections.unmodifiableMap(clases);
    }

    public void setClases(Map<String, EntradaClase> clases) {
        this.clases = new LinkedHashMap<>(clases);
    }

    public void estaBienDeclarado() {
        agregarHerenciaPorDefecto();
        inyectarConstructoresPorDefecto();

        Map<String, EstadoVisita> estados = new HashMap<>();
        for (EntradaClase clase : clases.values())
            validarJerarquia(clase, estados);

        for (EntradaClase clase : clases.values())
            clase.estaBienDeclarado(this);
    }

    public void chequearDeclaraciones() {
        agregarHerenciaPorDefecto();
        inyectarConstructoresPorDefecto();

        int erroresAntesDeJerarquia = errores.size();
        Map<String, EstadoVisita> estados = new HashMap<>();
        for (EntradaClase clase : new ArrayList<>(clases.values())) {
            try {
                validarJerarquia(clase, estados);
            } catch (ExcepcionSemantica error) {
                registrarError(error);
            }
        }

        for (EntradaClase clase : new ArrayList<>(clases.values())) {
            try {
                clase.estaBienDeclarado(this);
            } catch (ExcepcionSemantica error) {
                registrarError(error);
            }
        }

        if (errores.size() > erroresAntesDeJerarquia)
            return;

        for (EntradaClase clase : new ArrayList<>(clases.values())) {
            try {
                clase.consolidar(this);
            } catch (ExcepcionSemantica error) {
                registrarError(error);
            }
        }

        for (EntradaClase clase : new ArrayList<>(clases.values())) {
            try {
                clase.validarContratosDeInterfaces(this);
            } catch (ExcepcionSemantica error) {
                registrarError(error);
            }
        }
    }

    private void inyectarConstructoresPorDefecto() {
        for (EntradaClase clase : clases.values()) {
            if (!clase.isInterfaz() && !esClasePredefinida(clase)
                    && clase.getConstructores().isEmpty()) {
                clase.agregarConstructor(new EntradaMetodo(
                        clase.getToken(), clase.getNombre(), null, "public", false));
            }
        }
    }

    private boolean esClasePredefinida(EntradaClase clase) {
        return clase.getNombre().equals("Object")
                || clase.getNombre().equals("String")
                || clase.getNombre().equals("System");
    }

    public void consolidar() {
        for (EntradaClase clase : clases.values())
            clase.consolidar(this);
    }

    private void agregarHerenciaPorDefecto() {
        for (EntradaClase clase : clases.values()) {
            if (!clase.isInterfaz() && !clase.getNombre().equals("Object") && clase.getClaseBase() == null)
                clase.setClaseBase(new TipoReferencia(tokenClase("Object"), "Object"));
        }
    }

    private void validarJerarquia(EntradaClase clase, Map<String, EstadoVisita> estados) {
        EstadoVisita estado = estados.get(clase.getNombre());
        if (estado == EstadoVisita.VISITANDO)
            throw new ExcepcionSemantica(clase.getToken(),
                    "se detecto herencia circular en la clase '" + clase.getNombre() + "'");
        if (estado == EstadoVisita.VISITADA)
            return;

        estados.put(clase.getNombre(), EstadoVisita.VISITANDO);
        validarModificadores(clase);
        TipoReferencia claseBase = clase.getClaseBase();
        if (claseBase != null) {
            EntradaClase padre = clases.get(claseBase.getNombreClase());
            if (padre == null)
                throw new ExcepcionSemantica(claseBase.getToken(),
                        "la clase base '" + claseBase.getNombreClase() + "' no fue declarada");
            if ((!clase.isInterfaz() && padre.isInterfaz())
                    || (clase.isInterfaz() && !padre.isInterfaz()))
                throw new ExcepcionSemantica(claseBase.getToken(),
                        "la categoria del ancestro no es compatible");
            if (padre.isFinalClase())
                throw new ExcepcionSemantica(clase.getToken(),
                        "no se puede heredar de la clase final '" + padre.getNombre() + "'");
            if (padre.isSealed() && !contienePermiso(padre, clase.getNombre()))
                throw new ExcepcionSemantica(clase.getToken(),
                        "la clase no esta permitida por el ancestro sealed");
            validarArgumentoGenerico(claseBase, padre);
            validarJerarquia(padre, estados);
        }
        if (clase.isSealed() && clase.getPermisos().isEmpty())
            throw new ExcepcionSemantica(clase.getToken(),
                "una clase sealed debe declarar permisos");
        for (TipoReferencia interfaz : clase.getInterfaces()) {
            EntradaClase entradaInterfaz = clases.get(interfaz.getNombreClase());
            if (entradaInterfaz == null)
                throw new ExcepcionSemantica(interfaz.getToken(),
                        "la interfaz '" + interfaz.getNombreClase() + "' no fue declarada");
            if (!entradaInterfaz.isInterfaz())
                throw new ExcepcionSemantica(interfaz.getToken(),
                        "la relacion de interfaz requiere un tipo interfaz");
            if (entradaInterfaz.isSealed() && !contienePermiso(entradaInterfaz, clase.getNombre()))
                throw new ExcepcionSemantica(clase.getToken(),
                        "la clase no esta permitida por la interfaz sealed");
            validarArgumentoGenerico(interfaz, entradaInterfaz);
            validarJerarquia(entradaInterfaz, estados);
        }
        validarPermisos(clase);
        estados.put(clase.getNombre(), EstadoVisita.VISITADA);
    }

    private void validarModificadores(EntradaClase clase) {
        if (clase.isFinalClase() && (clase.isSealed() || clase.isNonSealed()))
            throw new ExcepcionSemantica(clase.getToken(),
                    "final no puede combinarse con sealed o non-sealed");
        if (clase.isSealed() && clase.isNonSealed())
            throw new ExcepcionSemantica(clase.getToken(),
                    "sealed no puede combinarse con non-sealed");
        if (clase.isNonSealed()) {
            boolean ancestroSealed = false;
            if (clase.getClaseBase() != null) {
                EntradaClase padre = clases.get(clase.getClaseBase().getNombreClase());
                ancestroSealed = padre != null && padre.isSealed();
            }
            for (TipoReferencia interfaz : clase.getInterfaces()) {
                EntradaClase padre = clases.get(interfaz.getNombreClase());
                ancestroSealed |= padre != null && padre.isSealed();
            }
            if (!ancestroSealed)
                throw new ExcepcionSemantica(clase.getToken(),
                        "non-sealed requiere un ancestro sealed");
        }
    }

    private void validarPermisos(EntradaClase clase) {
        if (clase.getPermisos().isEmpty()) {
            if (clase.isSealed())
                throw new ExcepcionSemantica(clase.getToken(),
                        "una entidad sealed debe declarar permisos");
            return;
        }
        if (!clase.isSealed())
            throw new ExcepcionSemantica(clase.getPermisos().get(0),
                    "permits solo puede usarse con sealed");
        for (Token permiso : clase.getPermisos()) {
            EntradaClase permitido = clases.get(permiso.getLexema());
            if (permitido == null)
                throw new ExcepcionSemantica(permiso,
                        "el tipo permitido no fue declarado");
            boolean heredaDirectamente = clase.getNombre().equals(
                    permitido.getClaseBase() == null ? null : permitido.getClaseBase().getNombreClase());
            for (TipoReferencia interfaz : permitido.getInterfaces())
                heredaDirectamente |= clase.getNombre().equals(interfaz.getNombreClase());
            if (!heredaDirectamente)
                throw new ExcepcionSemantica(permiso,
                        "el tipo permitido no hereda directamente de la entidad sealed");
        }
    }

    private boolean contienePermiso(EntradaClase clase, String nombre) {
        for (Token permiso : clase.getPermisos())
            if (permiso.getLexema().equals(nombre))
                return true;
        return false;
    }

    private void validarArgumentoGenerico(TipoReferencia relacion, EntradaClase destino) {
        if (relacion.getArgumentoGenerico() != null) {
            if (relacion.getArgumentoGenerico() instanceof TipoPrimitivo)
                throw new ExcepcionSemantica(relacion.getArgumentoGenerico().getToken(),
                        "los argumentos genericos deben ser tipos referencia");
            if (destino.getParametroGenerico() == null)
                throw new ExcepcionSemantica(relacion.getToken(),
                        "la clase '" + destino.getNombre() + "' no es parametrizada");
        }
    }

    private enum EstadoVisita {
        VISITANDO,
        VISITADA
    }
}