package com.akshay.assistant.benchmark;

// public record OllamaResponse(
//         String model,
//         String response,
//         boolean done,
//         Long total_duration, 
//         Long load_duration,
//         Integer prompt_eval_count, //Input tokens count
//         Integer eval_count,     //Ouput tokens count
//         Long prompt_eval_duration,
//         Long eval_duration //generation duration
// ) {
// }
import java.util.List;
import java.util.Map;

public record OllamaResponse(
        String model,
        Message message,

        Integer prompt_eval_count, // Input tokens

        Integer eval_count,       // Output tokens

        Long total_duration,     // Nanoseconds

        Long eval_duration       // Nanoseconds
) {
    public record Message(
            String role,
            String content,


            List<ToolCall> tool_calls
    ) {}

    public record ToolCall(
            Function function
    ) {
        public record Function(
                String name,
                Map<String, Object> arguments
        ) {}
    }

    // Helper getters for clarity
    public int getInputTokens() {
        return prompt_eval_count != null ? prompt_eval_count : 0;
    }

    public int getOutputTokens() {
        return eval_count != null ? eval_count : 0;
    }

    public boolean hasToolCalls() {
        return message != null && message.tool_calls() != null && !message.tool_calls().isEmpty();
    }
}