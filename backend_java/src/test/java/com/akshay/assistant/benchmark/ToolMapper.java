package com.akshay.assistant.benchmark;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ToolMapper {

    static List<OllamaRequest.Tool> tools = new ArrayList<>();
    public static List<OllamaRequest.Tool> fromSpringAiComponent(Object targetComponent) {

        for (Method method : targetComponent.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                Tool toolAnnotation = method.getAnnotation(Tool.class);
                
                String name = toolAnnotation.name().isEmpty() 
                        ? method.getName() 
                        : toolAnnotation.name();
                String description = toolAnnotation.description();

                Map<String, Object> parametersSchema = buildJsonSchema(method);

                tools.add(OllamaRequest.Tool.function(name, description, parametersSchema));
            }
        }
        return tools;
    }

    private static Map<String, Object> buildJsonSchema(Method method) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(ToolParam.class)) {
                ToolParam toolParam = param.getAnnotation(ToolParam.class);
                
                Map<String, Object> propDetails = new HashMap<>();
                propDetails.put("type", getJsonType(param.getType()));
                propDetails.put("description", toolParam.description());

                properties.put(param.getName(), propDetails);

                if (toolParam.required()) {
                    required.add(param.getName());
                }
            }
        }

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        return schema;
    }

    private static String getJsonType(Class<?> clazz) {
        if (clazz.equals(String.class)) return "string";
        if (clazz.equals(Integer.class) || clazz.equals(int.class) || 
            clazz.equals(Long.class) || clazz.equals(long.class)) return "integer";
        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) return "boolean";
        return "string";
    }
}