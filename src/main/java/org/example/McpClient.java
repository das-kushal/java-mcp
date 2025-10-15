package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class McpClient {

    public static void main(String[] args) throws Exception {
        // ✅ Your Gemini API key here
        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        if (geminiApiKey == null) {
            System.err.println("Please set GEMINI_API_KEY environment variable.");
            return;
        }

        GeminiService geminiService = new GeminiService(geminiApiKey);

        // Start the MCP server process
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", "target/classes", "org.example.Main");
        pb.redirectErrorStream(true);
        Process serverProcess = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
        OutputStreamWriter writer = new OutputStreamWriter(serverProcess.getOutputStream());

        // Example client call: get presentations
        String jsonRequest = """
        {
            "method": "call_tool",
            "params": {
                "name": "get_presentations_by_year",
                "arguments": {"year": 2025}
            }
        }
        """;

        writer.write(jsonRequest + "\n");
        writer.flush();

        // Read MCP server output
        System.out.println("MCP Server Response:");
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if (line.contains("Java")) break; // stop after reading a few lines
        }

        // Ask Gemini
        System.out.println("\n--- Ask Gemini ---");
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Ask Gemini a question: ");
        String question = userInput.readLine();

        String answer = geminiService.askGemini(question);
        System.out.println("\nGemini says:\n" + answer);

        serverProcess.destroy();
    }
}
