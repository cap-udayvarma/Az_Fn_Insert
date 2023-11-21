package com.example;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class ExecuteJarFromArtifactoryFunction {

    private static final String JAR_FILE_NAME = "my-app-1.0-20231121.044503-1.jar"; // Replace with your JAR file name
    private static final String ACCESS_TOKEN = "AKCpBrv6iiN4sfAwKmECrk2U9MxP6TuNRxdLYooQzhdTg29GKeLzHu3KR8XAr4tMrgHQf5JTB"; // Replace with your access token

    @FunctionName("ExecuteJarFromArtifactory")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        Logger logger = context.getLogger();
        String artifactoryUrl = "https://udayvarma.jfrog.io/artifactory/demo-auto-libs-snapshot-local/";
        String jarPathInArtifactory = "/com/mycompany/app/my-app/1.0-SNAPSHOT/my-app-1.0-20231121.044503-1.jar";

        // Get a temporary directory accessible by the function runtime
        String functionTempDirectory = System.getenv("TMP") + File.separator + "functiontmp" + File.separator + JAR_FILE_NAME;

        downloadJarFromArtifactory(artifactoryUrl, jarPathInArtifactory, functionTempDirectory, logger);
        String jarOutput = executeJarAndGetOutput(functionTempDirectory, logger);

        return request.createResponseBuilder(HttpStatus.OK).body("JAR executed successfully. Output: " + jarOutput).build();
    }

    private void downloadJarFromArtifactory(String artifactoryUrl, String jarPathInArtifactory, String localJarPath, Logger logger) {
        try {
            URL url = new URL(artifactoryUrl + "/" + jarPathInArtifactory);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);

            InputStream in = connection.getInputStream();
            Files.copy(in, Paths.get(localJarPath), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Jar downloaded successfully.");
        } catch (IOException e) {
            logger.warning("Error downloading JAR: " + e.getMessage());
        }
    }

    private String executeJarAndGetOutput(String jarFilePath, Logger logger) {
        try {
            Process process = Runtime.getRuntime().exec("java -jar " + jarFilePath);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            logger.info("Jar executed successfully.");
            return output.toString();
        } catch (Exception e) {
            logger.warning("Error executing JAR: " + e.getMessage());
            return "Error executing JAR: " + e.getMessage();
        }
    }
}
