package com.example;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Function {
    @FunctionName("ExecuteJar")
    public void run(
        @BlobTrigger(name = "file", dataType = "binary", path = "your-blob-container/{name}", connection = "AzureWebJobsStorage") byte[] content,
        final ExecutionContext context
    ) {
        // Replace "java -jar YourJarFile.jar" with the actual command to execute your JAR file
        String command = "java -jar /path/to/your/YourJarFile.jar";

        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                context.getLogger().info(line);
            }

            int exitCode = process.waitFor();
            context.getLogger().info("Execution exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            context.getLogger().info("Error executing JAR file: " + e.getMessage());
        }
    }
}

