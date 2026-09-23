package analizadorSintactico;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import main.MainSint;

public class VisibilidadOpcionalTest {
    @Test
    public void aceptaMiembrosConVisibilidadOpcional() throws IOException {
        Path source = Files.createTempFile("visibilidad-opcional", ".java");
        String program = "class Visibilidad {\n"
                + "  private int privado;\n"
                + "  protected char protegido;\n"
                + "}\n";
        Files.write(source, program.getBytes(StandardCharsets.UTF_8));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output));
            MainSint.main(new String[] { source.toString() });
        } finally {
            System.setOut(originalOut);
            Files.deleteIfExists(source);
        }

        String result = output.toString(StandardCharsets.UTF_8.name());
        assertThat(result, containsString("[SinErrores]"));
        assertFalse(result.contains("[Error:"));
    }
}
