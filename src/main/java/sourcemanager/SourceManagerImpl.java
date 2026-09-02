package sourcemanager;
//Author: Juan Dingevan

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SourceManagerImpl implements SourceManager {
    private List<String> lines;
    private String currentLine;
    private int lineNumber;
    private int lineIndexNumber;
    private int currentLineIndex;

    public SourceManagerImpl() {
        lines = new ArrayList<>();
        currentLine = "";
        lineNumber = 0;
        lineIndexNumber = 0;
        currentLineIndex = -1;
    }

    @Override
    public void open(String filePath) throws FileNotFoundException {
        lines.clear();
        lineNumber = 0;
        lineIndexNumber = 0;
        currentLineIndex = -1;
        currentLine = "";

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException exception) {
            if (exception instanceof FileNotFoundException fileNotFoundException) {
                throw fileNotFoundException;
            }

            throw new RuntimeException("No se pudo abrir el archivo fuente.", exception);
        }
    }

    @Override
    public void close() throws IOException {
        lines.clear();
        currentLine = "";
        lineNumber = 0;
        lineIndexNumber = 0;
        currentLineIndex = -1;
    }

    @Override
    public char getNextChar() throws IOException {
        char currentChar = ' ';

        if (currentLineIndex == -1 || lineIndexNumber > currentLine.length()) {
            currentLineIndex++;

            if (currentLineIndex >= lines.size()) {
                lineNumber = currentLineIndex + 1;
                currentChar = END_OF_FILE;
                return currentChar;
            }

            currentLine = lines.get(currentLineIndex);
            lineNumber = currentLineIndex + 1;
            lineIndexNumber = 0;
        }

        if (lineIndexNumber < currentLine.length()) {
            currentChar = currentLine.charAt(lineIndexNumber);
            lineIndexNumber++;
        } else if (lineIndexNumber == currentLine.length() && currentLineIndex < lines.size() - 1) {
            currentChar = '\n';
            lineIndexNumber++;
        } else {
            lineNumber = currentLineIndex + 2;
            currentChar = END_OF_FILE;
        }

        return currentChar;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public boolean isEOF(char caracterActual) {
        return caracterActual == END_OF_FILE;
    }

    @Override
    public String getLine(int nroLinea) {
        if (nroLinea < 1 || nroLinea > lines.size()) {
            return "";
        }

        return lines.get(nroLinea - 1);
    }

}
