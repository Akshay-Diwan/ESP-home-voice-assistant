package com.akshay.assistant.tools.notes;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import com.akshay.assistant.config.NotionConfig;

@SpringBootApplication
@Import(NotionConfig.class)
public class NotionTestApplication {
}
