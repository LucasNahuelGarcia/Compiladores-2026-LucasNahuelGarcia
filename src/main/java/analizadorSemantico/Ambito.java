package analizadorSemantico;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class Ambito {
    private final Map<String, VariableLocal> variables = new LinkedHashMap<>();

    VariableLocal get(String nombre) { return variables.get(nombre); }
    boolean contiene(String nombre) { return variables.containsKey(nombre); }
    void agregar(VariableLocal variable) { variables.put(variable.getNombre(), variable); }
    Map<String, VariableLocal> getVariables() { return Collections.unmodifiableMap(variables); }
}
