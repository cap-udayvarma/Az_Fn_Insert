package com.example;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import com.azure.storage.blob.*;

public class ExecuteJarFromArtifactoryFunction {

    private static final String JAR_FILE_NAME = "my-app-1.0-20231121.082035-1.jar"; // Replace with your JAR file name
    private static final String ACCESS_TOKEN = "AKCpBrv6iiN4sfAwKmECrk2U9MxP6TuNRxdLYooQzhdTg29GKeLzHu3KR8XAr4tMrgHQf5JTB"; // Replace with your access token
    private static final String AZURE_STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=autoretryapplication;AccountKey=r9UXKCT0+H7T6SslPdAI7RxR/7ZoheAFKMr8ou/PnPYIZqJ/t6NFi3270PYAVcSpJi1vUDBN9L/n+AStucoDIw==;EndpointSuffix=core.windows.net"; // Replace with your Azure Storage connection string
    private static final String CONTAINER_NAME = "autoretry"; // Replace with your container name in Azure Blob Storage

    @FunctionName("ExecuteJarFromArtifactory")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        Logger logger = context.getLogger();
        String artifactoryUrl = "https://udayvarma.jfrog.io/artifactory/check-libs-snapshot-local/";
        String jarPathInArtifactory = "/com/mycompany/app/my-app/1.0-SNAPSHOT/my-app-1.0-20231121.082035-1.jar";

        String jarBlobName = "autoretyjar/" + JAR_FILE_NAME; // Replace with the desired path inside the container

        try {
            // Initialize Azure Blob Storage client
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(AZURE_STORAGE_CONNECTION_STRING).buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);

            // Create the container if it doesn't exist
            if (!containerClient.exists()) {
                containerClient.create();
            }

            // Download JAR file from Artifactory
            byte[] jarBytes = downloadJarFromArtifactory(artifactoryUrl, jarPathInArtifactory, logger);

            // Upload JAR file to Azure Blob Storage
            BlobClient blobClient = containerClient.getBlobClient(jarBlobName);
            blobClient.upload(new ByteArrayInputStream(jarBytes), jarBytes.length, true);

            // Execute JAR file from Azure Blob Storage
            String jarOutput = executeJarAndGetOutput(blobClient.getBlobUrl(), logger);

            return request.createResponseBuilder(HttpStatus.OK).body("JAR executed successfully. Output: " + jarOutput).build();
        } catch (Exception e) {
            logger.warning("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage()).build();
        }
    }

    private byte[] downloadJarFromArtifactory(String artifactoryUrl, String jarPathInArtifactory, Logger logger) throws IOException {
        URL url = new URL(artifactoryUrl + "/" + jarPathInArtifactory);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);

        try (InputStream in = connection.getInputStream()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            logger.warning("Error downloading JAR: " + e.getMessage());
            throw e;
        }
    }

    private String executeJarAndGetOutput(String jarBlobUrl, Logger logger) {
        try {
            Process process = Runtime.getRuntime().exec("java -jar " + jarBlobUrl);

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

