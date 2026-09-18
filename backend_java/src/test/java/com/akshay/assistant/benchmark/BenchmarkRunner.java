package com.akshay.assistant.benchmark;

import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;



@Component
public class BenchmarkRunner {

    private final OllamaBenchmarkClient ollama;
    private final ObjectMapper mapper;
    private BufferedWriter writer;
    public BenchmarkRunner(
            OllamaBenchmarkClient ollama,
            ObjectMapper mapper
    ) {
        this.ollama = ollama;
        this.mapper = mapper;
        try{
                writer = new BufferedWriter(new FileWriter("benchmark-results/results.csv"));
        }
        catch(IOException e){
                System.err.println("Could not intitialize csv buffered writer \n" + e.getMessage());
        }
    }

    public void run() throws Exception {

        List<String> models = loadModels();
        List<BenchmarkTestCase> tests = loadTests();

        String[] benchmarkResultFields = {
                "model",
                "prompt",
                "expectedTool",
                "actualTool",
                "toolCorrect",
                "argumentsCorrect",
                "latencyMs",
                "inputTokens",
                "outputTokens",
                "tokensPerSecond",
                "response",
                "error"
        };
        writer.write(String.join(",", benchmarkResultFields));
        writer.newLine();
        for (String model : models) {

            System.out.println();
            System.out.println("================================");
            System.out.println("MODEL: " + model);
            System.out.println("================================");

            for (BenchmarkTestCase test : tests) {

                runTest(model, test);
            }
        }
        writer.close();
    }

    private void runTest(
            String model,
            BenchmarkTestCase test
    ) {

        System.out.println();
        System.out.println("Test: " + test.id());
        System.out.println("Prompt: " + test.prompt());

        try {

            long start = System.nanoTime();

            OllamaResponse response =
                    ollama.run(model, test.prompt());

            long end = System.nanoTime();

            long latency =
                    (end - start) / 1_000_000;

            double tokPerSec = 0;

            if (response.eval_duration() != null &&
                    response.eval_duration() > 0) {

                tokPerSec =
                        response.eval_count()
                                / (response.eval_duration()
                                / 1_000_000_000.0);
            }

            System.out.println(
                    "Response: " + response.message()
            );

            System.out.println(
                    "Latency: " + latency + " ms"
            );

            System.out.println(
                    "Tokens/sec: " + tokPerSec
            );
            StringBuffer actualToolsCalled = new StringBuffer();
            
            if (response.hasToolCalls()) {
                response.message().tool_calls().forEach(call -> {
                    System.out.println("Selected Tool: " + call.function().name());
                    actualToolsCalled.append(call.function().name());
                    System.out.println("Arguments: " + call.function().arguments());
                });
            } else {
                System.out.println("No tool selected. Response: " + response.message().content());
            }

            writeResultToCSV(new BenchmarkResult(model, test.prompt(), test.expectedTool(), actualToolsCalled.toString(), false, false, latency, response.getInputTokens(), response.getOutputTokens(), tokPerSec, response.message().content(), null));
        } catch (Exception e) {

            System.err.println(
                    "ERROR: " + e.getMessage()
            );
        }
    }

    private List<String> loadModels() throws IOException {
        Path path = Paths.get("models.json");
        InputStream stream = Files.newInputStream(path);
        return mapper.readValue(
                stream,
                new TypeReference<>() {}
        );
    }

    private List<BenchmarkTestCase> loadTests() throws Exception {
         Path path = Paths.get("test-cases.json");
        InputStream stream = Files.newInputStream(path);
        return mapper.readValue(
                stream,
                new TypeReference<>() {}
        );
    }
    private void writeResultToCSV(BenchmarkResult result) throws IOException{
         writer.write(String.join(",", new String[]{
                result.model(),
                result.prompt(),
                result.expectedTool(),
                result.actualTool(),
                String.valueOf(result.toolCorrect()),
                String.valueOf(result.argumentsCorrect()),
                String.valueOf(result.latencyMs()),
                String.valueOf(result.inputTokens()),
                String.valueOf(result.outputTokens()),
                String.valueOf(result.tokensPerSecond()),
                result.response(),
                result.error()
        }));
        writer.newLine();
        System.out.println("Line written");
        
    }
}