package com.api.testrunner;

import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/test-runner")
@Slf4j
public class TestRunnerController {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicReference<String> lastOutput = new AtomicReference<>("No tests run yet.");
    private final AtomicReference<String> status = new AtomicReference<>("IDLE");

    @PostMapping("/run")
    public String runTests(@RequestParam(defaultValue = "com.api.auth.AuthControllerIntegrationTest") String testName) {
        log.info("Received request to run tests for: {}", testName);
        if (isRunning.get()) {
            return "Tests are already running...";
        }

        isRunning.set(true);
        status.set("RUNNING");
        lastOutput.set("Starting tests for: " + testName + "\n");

        CompletableFuture.runAsync(() -> {
            try {
                String projectDir = System.getProperty("user.dir");
                log.info("Starting process in directory: {}", projectDir);
                ProcessBuilder pb = new ProcessBuilder("./gradlew", "test", "--tests", testName);
                pb.directory(new java.io.File(projectDir));
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                        lastOutput.set(output.toString());
                    }
                }

                int exitCode = process.waitFor();
                log.info("Test execution finished with exit code: {}", exitCode);
                status.set(exitCode == 0 ? "SUCCESS" : "FAILED");
            } catch (Exception e) {
                log.error("Error while running tests", e);
                status.set("ERROR");
                lastOutput.set("Error running tests: " + e.getMessage());
            } finally {
                isRunning.set(false);
            }
        });

        return "Test execution started...";
    }

    @GetMapping("/status")
    public TestStatus getStatus() {
        return new TestStatus(isRunning.get(), status.get(), lastOutput.get());
    }

    public record TestStatus(boolean running, String status, String output) {
    }
}
