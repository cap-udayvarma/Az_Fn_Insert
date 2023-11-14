package com.example;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;

public class StoreOutputInDatabaseFunction {
    @FunctionName("StoreOutputInDatabaseFunction")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Map<String, Object>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        Map<String, Object> requestBody = request.getBody();
        if (requestBody == null || requestBody.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please provide data to store").build();
        }

        // Assuming the input is in the format "Employee details inserted: Name = [Employee Name], Department = [Employee Department]"
        String outputToStore = (String) requestBody.get("output");

        try {
            // JDBC Connection to your database
            String url = "jdbc:sqlserver://your.database.url:1433;database=your_database;user=your_username;password=your_password";
            Connection connection = DriverManager.getConnection(url);

            // Insert output into a table in the database
            String insertQuery = "INSERT INTO output_table (output_data) VALUES (?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, outputToStore);
            preparedStatement.executeUpdate();

            connection.close();

            return request.createResponseBuilder(HttpStatus.OK).body("Output stored in the database").build();
        } catch (Exception e) {
            context.getLogger().severe("Exception: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error storing data in the database").build();
        }
    }
}
