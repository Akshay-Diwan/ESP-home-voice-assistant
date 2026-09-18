package com.akshay.assistant.benchmark;
import java.util.Map;

public record BenchmarkTestCase(
        String id,
        String prompt,
        String expectedTool,
        Map<String, Object> expectedArguments
) {
}