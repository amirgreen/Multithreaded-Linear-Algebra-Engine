import spl.lae.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class ThreadCrashIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void testWorkerThreadCrashWritesToOutput() throws IOException {
        Path inputFile = tempDir.resolve("crash_input.json");
        Path outputFile = Path.of("output_error.json");

        String badJson = "{\"operator\": \"+\", \"operands\": [[[1,1],[1,1]], [[2,2],[2,2,2]]]}";
        
        Files.writeString(inputFile, badJson);

        String[] args = {"3", inputFile.toString(), outputFile.toString()};
        
        Main.main(args);

        assertTrue(Files.exists(outputFile), "Output should be created");
        
        String content = Files.readString(outputFile);
        assertFalse(content.isEmpty(), "Output shouldnt be empty");

        assertTrue(content.toLowerCase().contains("error"), 
            "Output should contain exception" + content);
        
        System.out.println("Error captured in output file: " + content);
    }
    
    @Test
    void testNegativeThreadsCrash() throws IOException {

        Path inputFile = tempDir.resolve("dummy.json");
        Path outputFile = Path.of("NegativeThreadsCrash_out.json");
        Files.writeString(inputFile, "[[1.0]]");

        String[] args = {"-5", inputFile.toString(), outputFile.toString()};
        
        Main.main(args);
        
        assertTrue(Files.exists(outputFile));
        String errorMsg = Files.readString(outputFile);
        
        assertTrue(errorMsg.contains("must be positive") || errorMsg.isEmpty());
    }
}