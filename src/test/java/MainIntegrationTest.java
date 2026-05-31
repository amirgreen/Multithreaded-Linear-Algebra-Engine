import spl.lae.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void testMainWritesGeneralExceptionToOutputFile() throws IOException {

        Path inputFile = tempDir.resolve("invalid_input.json");
        Files.writeString(inputFile, "{ \"nodeType\": \"UNKNOWN_TYPE\" }"); 

        Path outputFile = Path.of("GeneralException_output.json");

        String[] args = {
            "4",                      
            inputFile.toString(),     
            outputFile.toString()     
        };

        Main.main(args);

        assertTrue(Files.exists(outputFile), "Output file should be created");
        
        List<String> lines = Files.readAllLines(outputFile);
        assertFalse(lines.isEmpty(), "Output file should contain the error message");
        
        System.out.println("Output file content: " + lines.get(0));
    }

    @Test
    void testMainInvalidThreadNumberPrintsToErr() {
        Path inputFile = tempDir.resolve("dummy.json");
        Path outputFile = Path.of("InvalidThreadNumber_output.json");

        String[] args = {"-1", inputFile.toString(), outputFile.toString()};

        assertDoesNotThrow(() -> Main.main(args));
    }
}