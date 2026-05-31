package spl.lae;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestGenerator {
    public static void main(String[] args) throws IOException {
        int size = 100;
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        sb.append("{\n  \"operator\": \"*\",\n  \"operands\": [\n    [\n");
        
        for (int i = 0; i < size; i++) {
            sb.append("      [");
            for (int j = 0; j < size; j++) {
                sb.append(rand.nextInt(100));
                if (j < size - 1) sb.append(",");
            }
            sb.append("]");
            if (i < size - 1) sb.append(",\n");
        }
        
        sb.append("\n    ],\n    [\n");
        
        for (int i = 0; i < size; i++) {
            sb.append("      [");
            for (int j = 0; j < size; j++) {
                sb.append(rand.nextInt(100));
                if (j < size - 1) sb.append(",");
            }
            sb.append("]");
            if (i < size - 1) sb.append(",\n");
        }
        
        sb.append("\n    ]\n  ]\n}");

        try (FileWriter writer = new FileWriter("large_test.json")) {
            writer.write(sb.toString());
        }
        System.out.println("File large_test.json created successfully!");
    }
}