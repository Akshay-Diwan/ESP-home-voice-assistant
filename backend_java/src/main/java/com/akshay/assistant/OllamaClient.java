package com.akshay.assistant;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OllamaClient {

    private final HttpClient client = HttpClient.newHttpClient();

    public String generate(String prompt) throws Exception {
        String json = """
                {
                    "model": "gemma3:4b",
                    "prompt": "%s",
                    "stream": false
                }
                """.formatted(prompt.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public static void main(String[] args) throws Exception {
        OllamaClient ollama = new OllamaClient();

        String response =
                ollama.generate("Explain Java interfaces in simple terms");

        System.out.println(response);
    }
}
