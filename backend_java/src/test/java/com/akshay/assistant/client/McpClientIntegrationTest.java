package com.akshay.assistant.client;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

import com.akshay.assistant.TestApplication;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
    "mqtt.url=tcp://localhost:1883",
    "mqtt.username=user1",
    "mqtt.password=akshay"
}
)
@EnableIntegration
@IntegrationComponentScan(basePackages = "com.akshay.assistant.gateway")
class McpClientIntegrationTest {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    void mcpClientShouldDiscoverTools() {

        assertNotNull(toolCallbackProvider);

        ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();

        assertNotNull(tools);
        assertTrue(tools.length > 0);

        for (ToolCallback tool : tools) {
            System.out.println(
                    "Discovered MCP tool: "
                            + tool.getToolDefinition().name()
            );
        }
    }
}