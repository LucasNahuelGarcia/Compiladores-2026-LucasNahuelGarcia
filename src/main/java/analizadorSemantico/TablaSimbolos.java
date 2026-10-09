package analizadorSemantico;

import analizadorLexico.Token;
import analizadorLexico.TokenType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

public class TablaSimbolos {
    private Map<String, EntradaClase> clases;

    public TablaSimbolos() {
        clases = new LinkedHashMap<>();
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
        if (clases.containsKey(clase.getNombre()))
            throw new ExcepcionSemantica(clase.getToken(),
                    "la clase o interfaz '" + clase.getNombre() + "' ya fue declarada");
        clases.put(clase.getNombre(), clase);
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

        Map<String, EstadoVisita> estados = new HashMap<>();
        for (EntradaClase clase : clases.values())
            validarJerarquia(clase, estados);

        for (EntradaClase clase : clases.values())
            clase.estaBienDeclarado(this);
        for (EntradaClase clase : clases.values())
            clase.validarContratosDeInterfaces(this);
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
            validarArgumentoGenerico(claseBase, padre);
            validarJerarquia(padre, estados);
        }
        for (TipoReferencia interfaz : clase.getInterfaces()) {
            EntradaClase entradaInterfaz = clases.get(interfaz.getNombreClase());
            if (entradaInterfaz == null)
                throw new ExcepcionSemantica(interfaz.getToken(),
                        "la interfaz '" + interfaz.getNombreClase() + "' no fue declarada");
            if (!entradaInterfaz.isInterfaz())
                throw new ExcepcionSemantica(interfaz.getToken(),
                        "una clase solo puede implementar interfaces");
            validarArgumentoGenerico(interfaz, entradaInterfaz);
            validarJerarquia(entradaInterfaz, estados);
        }
        estados.put(clase.getNombre(), EstadoVisita.VISITADA);
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