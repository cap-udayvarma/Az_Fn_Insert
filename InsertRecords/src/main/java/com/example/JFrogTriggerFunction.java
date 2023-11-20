package com.example;

import com.microsoft.azure.functions.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;


import com.microsoft.azure.functions.*;

public class JFrogTriggerFunction {

    @FunctionName("JFrogExecution")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("JFrog Execution triggered.");

        // Code to execute your JAR file in JFrog
        try {
            URL jarURL = new URL("https://udayvarma.jfrog.io/artifactory/autoretry-libs-snapshot-local/com/example/Hello_World_ACR/0.0.1-SNAPSHOT/Hello_World_ACR-0.0.1-20231120.154851-1.jar");
            HttpURLConnection connection = (HttpURLConnection) jarURL.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            // Read the JAR file or perform other necessary operations

            // Execute the JAR file (example command)
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "Hello_World_ACR-0.0.1-20231120.154851-1.jar");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return request.createResponseBuilder(HttpStatus.OK).body("JAR executed successfully.").build();
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("JAR execution failed.").build();
            }
        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error executing JAR file.").build();
        }
    }
}
