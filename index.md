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

El programa recibe como único argumento la ruta del archivo fuente Mini-Java:

```bash
java -jar build/libs/Compilador_2026.jar resources/sinErrores/semICorrecto01.java
```

La entrada configurada en el JAR es `main.Main`, que ejecuta el análisis léxico, sintáctico y semántico.

Salida exitosa:

```text
[SinErrores]
```

Ejemplo con errores:

```bash
java -jar build/libs/Compilador_2026.jar resources/conErrores/semIError01.java
```

## Ejecutar desde Gradle

También puede ejecutarse directamente con las clases compiladas:

```bash
./gradlew classes
java -cp build/classes/java/main main.Main resources/sinErrores/semICorrecto01.java
```

Para usar explícitamente el driver semántico:

```bash
java -cp build/classes/java/main main.MainSemantico resources/sinErrores/semICorrecto01.java
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
