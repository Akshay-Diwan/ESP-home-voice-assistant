package com.akshay.assistant.benchmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.akshay.assistant.TestApplication;

@SpringBootTest(classes = TestApplication.class)
@Import(TestConfig.class)
class ModelBenchmarkTest {

    @Autowired
    BenchmarkRunner benchmarkRunner;

    @Test
    void benchmarkModels() throws Exception {

        benchmarkRunner.run();
    }
}