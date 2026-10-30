package test.java;

import analizadorLexico.AnalizadorLexico;
import analizadorSemantico.EntradaAtributo;
import analizadorSemantico.EntradaClase;
import analizadorSemantico.EntradaMetodo;
import analizadorSemantico.EntradaParametro;
import analizadorSemantico.EntidadDeclarativa;
import analizadorSemantico.TablaSimbolos;
import analizadorSemantico.VariableLocal;
import analizadorSemantico.ast.BloqueAst;
import analizadorSemantico.ast.CicloForAst;
import analizadorSemantico.ast.ExpresionAst;
import analizadorSemantico.ast.NodoExpresionAst;
import analizadorSemantico.ast.NodoSentenciaAst;
import analizadorSemantico.ast.SentenciaAst;
import analizadorSintactico.Inicial;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import sourcemanager.SourceManagerImpl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class VinculacionAstTest {
    @Test
    public void enlazaReferenciasAstConLasEntidadesDeclarativas() throws IOException {
        SourceManagerImpl source = new SourceManagerImpl();
        source.open("resources/sinErrores/semIICorrecto17.java");
        TablaSimbolos tabla;
        try {
            tabla = new Inicial().parse(new AnalizadorLexico(source));
        } finally {
            source.close();
        }
        tabla.chequearDeclaraciones();
        assertTrue(tabla.getErrores().toString(), tabla.getErrores().isEmpty());

        EntradaClase prueba = tabla.getClase("Prueba");
        EntradaMetodo probar = prueba.getMetodo("probar", 3);
        List<NodoExpresionAst> expresiones = new ArrayList<>();
        List<NodoSentenciaAst> sentencias = new ArrayList<>();
        List<CicloForAst> ciclosFor = new ArrayList<>();
        recolectarSentencia(probar.getCuerpo(), expresiones, sentencias, ciclosFor);

        EntradaParametro parametroDatos = probar.getParametros().get("datos");
        EntradaAtributo atributoNumero = tabla.getClase("Datos").getAtributos().get("numero");
        EntradaAtributo atributoHeredado = tabla.getClase("SuperDatos").getAtributos().get("heredado");
        EntradaMetodo metodoLeer = tabla.getClase("Lector").getMetodo("leer", 0);
        EntradaMetodo metodoHeredado = tabla.getClase("SuperDatos").getMetodo("heredadoMetodo", 0);
        EntradaMetodo metodoImplicito = prueba.getMetodo("recibir", 1);
        EntradaMetodo metodoEstatico = tabla.getClase("Datos").getMetodo("estatico", 0);
        EntradaMetodo metodoPredefinido = tabla.getClase("System").getMetodo("println", 0);
        EntradaMetodo constructorDatos = tabla.getClase("Datos").getConstructores().get("0");
        EntidadDeclarativa variableEntero = declaracionLocal(sentencias, "entero").getEntidadVinculada();

        assertSame(parametroDatos, entidad(expresiones, NodoExpresionAst.Clase.VARIABLE, "datos"));
        assertSame(atributoNumero.getDeclaracionOriginal(),
                entidad(expresiones, NodoExpresionAst.Clase.ACCESO_ATRIBUTO, "numero"));
        assertSame(atributoHeredado.getDeclaracionOriginal(),
                entidad(expresiones, NodoExpresionAst.Clase.ACCESO_ATRIBUTO, "heredado"));
        assertSame(metodoLeer.getDeclaracionOriginal(),
                entidad(expresiones, NodoExpresionAst.Clase.LLAMADA_MIEMBRO, "leer"));
        assertSame(metodoHeredado.getDeclaracionOriginal(),
                entidad(expresiones, NodoExpresionAst.Clase.LLAMADA_MIEMBRO, "heredadoMetodo"));
        assertSame(metodoImplicito.getDeclaracionOriginal(),
                entidad(expresiones, NodoExpresionAst.Clase.LLAMADA_IMPLICITA, "recibir"));
        assertSame(metodoEstatico.getDeclaracionOriginal(),
                entidad(expresiones, NodoExpresionAst.Clase.LLAMADA_ESTATICA, "estatico"));
        assertSame(metodoPredefinido.getDeclaracionOriginal(),
                entidad(expresiones, NodoExpresionAst.Clase.LLAMADA_ESTATICA, "println"));
        assertSame(constructorDatos.getDeclaracionOriginal(),
                entidad(expresiones, NodoExpresionAst.Clase.NUEVO_OBJETO, null));
        assertSame(tabla.getAtributoLengthArreglo(),
                entidad(expresiones, NodoExpresionAst.Clase.ACCESO_ATRIBUTO, "length"));
        assertSame(variableEntero, entidad(expresiones, NodoExpresionAst.Clase.VARIABLE, "entero"));

        CicloForAst cicloMejorado = ciclosFor.stream()
                .filter(ciclo -> ciclo.getForma() == CicloForAst.Forma.MEJORADO)
                .findFirst().orElseThrow(AssertionError::new);
        assertNotNull(cicloMejorado.getEntidadVinculada());
        assertSame(cicloMejorado.getEntidadVinculada(),
                entidad(expresiones, NodoExpresionAst.Clase.VARIABLE, "item"));
        assertSame(VariableLocal.class, cicloMejorado.getEntidadVinculada().getClass());
    }

    private void recolectarSentencia(SentenciaAst sentencia, List<NodoExpresionAst> expresiones,
            List<NodoSentenciaAst> sentencias, List<CicloForAst> ciclosFor) {
        if (sentencia instanceof BloqueAst) {
            for (SentenciaAst hija : ((BloqueAst) sentencia).getSentencias())
                recolectarSentencia(hija, expresiones, sentencias, ciclosFor);
        } else if (sentencia instanceof CicloForAst) {
            CicloForAst ciclo = (CicloForAst) sentencia;
            ciclosFor.add(ciclo);
            recolectarExpresion(ciclo.getIterable(), expresiones);
            recolectarSentencia(ciclo.getCuerpo(), expresiones, sentencias, ciclosFor);
        } else if (sentencia instanceof NodoSentenciaAst) {
            NodoSentenciaAst nodo = (NodoSentenciaAst) sentencia;
            sentencias.add(nodo);
            for (ExpresionAst expresion : nodo.getExpresiones())
                recolectarExpresion(expresion, expresiones);
            for (SentenciaAst hija : nodo.getSentencias())
                recolectarSentencia(hija, expresiones, sentencias, ciclosFor);
        }
    }

    private void recolectarExpresion(ExpresionAst expresion, List<NodoExpresionAst> expresiones) {
        if (!(expresion instanceof NodoExpresionAst)) return;
        NodoExpresionAst nodo = (NodoExpresionAst) expresion;
        expresiones.add(nodo);
        for (ExpresionAst operando : nodo.getOperandos())
            recolectarExpresion(operando, expresiones);
    }

    private NodoSentenciaAst declaracionLocal(List<NodoSentenciaAst> sentencias, String nombre) {
        return sentencias.stream()
                .filter(n -> n.getClase() == NodoSentenciaAst.Clase.VARIABLE_LOCAL
                        && nombre.equals(n.getNombre().getLexema()))
                .findFirst().orElseThrow(AssertionError::new);
    }

    private EntidadDeclarativa entidad(List<NodoExpresionAst> expresiones,
            NodoExpresionAst.Clase clase, String nombre) {
        return expresiones.stream()
                .filter(n -> n.getClase() == clase
                        && (nombre == null || n.getNombre() != null
                            && nombre.equals(n.getNombre().getLexema())))
                .findFirst().orElseThrow(AssertionError::new).getEntidadVinculada();
    }
}
