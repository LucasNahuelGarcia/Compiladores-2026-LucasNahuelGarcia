package sourcemanager;
//Author: Juan Dingevan

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
public class SourceManagerImpl implements SourceManager{
    private BufferedReader reader;
    private String currentLine;
    private int lineNumber;
    private int lineIndexNumber;
    private boolean mustReadNextLine;
    private List<String> lines;


    public SourceManagerImpl() {
        lines = new ArrayList<>();
        currentLine = "";
        lineNumber = 0;
        lineIndexNumber = 0;
        mustReadNextLine = true;
    }

    @Override
    public void open(String filePath) throws FileNotFoundException {
        lines.clear();
        currentLine = "";
        lineNumber = 0;
        lineIndexNumber = 0;
        mustReadNextLine = true;

        try (BufferedReader cacheReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;

            while ((line = cacheReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException exception) {
            if (exception instanceof FileNotFoundException fileNotFoundException) {
                throw fileNotFoundException;
            }

            throw new RuntimeException("No se pudo abrir el archivo fuente.", exception);
        }

        FileInputStream fileInputStream = new FileInputStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);

        reader = new BufferedReader(inputStreamReader);
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }

        lines.clear();
    }

    @Override
    public char getNextChar() throws IOException {
        char currentChar = ' ';

        if(mustReadNextLine) {
            currentLine = reader.readLine();
            lineNumber++;
            lineIndexNumber = 0;
            mustReadNextLine = false;
        }

        if(lineIndexNumber < currentLine.length()) {
            currentChar = currentLine.charAt(lineIndexNumber);
            lineIndexNumber++;
        } else if (reader.ready()) {
            currentChar = '\n';
            mustReadNextLine = true;
        } else {
            if (currentLine.isEmpty()) {
                lineNumber++;
            }
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

    @Override
    public int getColNumber() {
        return lineIndexNumber;
    }

}
