# Compilador Mini-Java

## Requisitos

- Java Development Kit (JDK) 17 o superior.
- No es necesario instalar Gradle: el proyecto incluye Gradle Wrapper.

Comprobar Java:

```bash
java -version
```

## Compilar

Desde la raíz del proyecto:

```bash
./gradlew clean build
```

En Windows:

```bat
gradlew.bat clean build
```

El JAR compilado queda en:

```text
build/libs/Compilador_2026.jar
```

## Ejecutar el compilador

El programa toma la ruta del archivo fuente Mini-Java del primer argumento. La forma de uso es pasar esa ruta como único argumento:

```bash
java -jar build/libs/Compilador_2026.jar resources/sinErrores/semIICorrecto01.java
```

La entrada configurada en el JAR es `main.Main`, que ejecuta el análisis léxico, sintáctico y semántico.

Salida exitosa:

```text
[SinErrores]
```

Ejemplo con errores:

```bash
java -jar build/libs/Compilador_2026.jar resources/conErrores/semIIError01.java
```

## Ejecutar desde Gradle

También puede ejecutarse directamente con las clases compiladas:

```bash
./gradlew classes
java -cp build/classes/java/main main.Main resources/sinErrores/semIICorrecto01.java
```

Para usar explícitamente el driver semántico:

```bash
java -cp build/classes/java/main main.MainSemantico resources/sinErrores/semIICorrecto01.java
```

## Ejecutar las pruebas

```bash
./gradlew test
```

Para forzar una ejecución completa sin reutilizar resultados anteriores:

```bash
./gradlew test --rerun-tasks
```

Los reportes quedan disponibles en:

```text
build/reports/tests/test/index.html
```

Los casos parametrizados se cargan automáticamente desde:

```text
resources/conErrores/
resources/sinErrores/
```
