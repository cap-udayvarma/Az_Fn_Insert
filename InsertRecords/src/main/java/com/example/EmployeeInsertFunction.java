package com.example;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;

public class EmployeeInsertFunction {
    @FunctionName("EmployeeInsertFunction")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Map<String, Object>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        Map<String, Object> requestBody = request.getBody();
        if (requestBody == null || requestBody.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please provide employee details").build();
        }

        try {
            String name = (String) requestBody.get("name");
            String department = (String) requestBody.get("department");

            System.out.println("uuu");

            // JDBC Connection
            String url = "jdbc:sqlserver://udayvarmasql.database.windows.net:1433;database=employees;user=udayvarma@udayvarmasql;password=@Capgemini1234;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30";
            Connection connection = DriverManager.getConnection(url);
            System.out.println("-----");

            // Insert into SQL table
            String insertQuery = "INSERT INTO employee (name, department) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, department);
            preparedStatement.executeUpdate();

            connection.close();

            return request.createResponseBuilder(HttpStatus.OK).body("Employee details inserted: Name = " + name + ", Department = " + department).build();
        } catch (Exception e) {
            context.getLogger().severe("Exception: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error storing data in the database").build();
        }
    }
}
