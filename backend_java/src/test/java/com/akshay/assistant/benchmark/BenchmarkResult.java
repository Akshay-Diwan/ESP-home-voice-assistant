package com.akshay.assistant.benchmark;

public record BenchmarkResult(
        String model,
        String prompt,
        String expectedTool,
        String actualTool,
        boolean toolCorrect,
        boolean argumentsCorrect,
        long latencyMs,
        int inputTokens,
        int outputTokens,
        double tokensPerSecond,
        String response,
        String error
) {
}