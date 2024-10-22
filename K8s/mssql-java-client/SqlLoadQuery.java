import java.sql.*;
import java.util.logging.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class SqlLoadQuery {

    // Connection parameters
    private static final String SERVER = "jdbc:sqlserver://mssql-server";
    private static final String DATABASE = "AdventureWorks";
    private static final String USERNAME = "SA";
    private static final String PASSWORD = "password123";

    // List of SQL queries to run
    private static final List<String> queries = Arrays.asList(
        "SELECT * FROM Person.Address",
        "SELECT * FROM Person.Person JOIN Person.BusinessEntity ON Person.Person.BusinessEntityID = Person.BusinessEntity.BusinessEntityId JOIN Person.BusinessEntityAddress ON Person.BusinessEntityAddress.BusinessEntityID = Person.Person.BusinessEntityID",
        "SELECT * FROM Production.Product",
        "SELECT * FROM Sales.SalesOrderDetail s INNER JOIN Production.Product p ON s.ProductID = p.ProductID"
    );

    // Configure logging to log to both console and a file
    private static final Logger logger = Logger.getLogger(SqlLoadQuery.class.getName());
    private static FileHandler fileHandler;

    static {
        try {
            // Log to a file and console
            fileHandler = new FileHandler("mssql-query-run-log.txt", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
            logger.setLevel(Level.INFO);
            logger.addHandler(new ConsoleHandler()); // Add console handler
        } catch (Exception e) {
            System.err.println("Error setting up logging: " + e.getMessage());
        }
    }

    // Main method
    public static void main(String[] args) {
        Connection connection = null;

        // Set up the HTTP server for readiness/liveness probes
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/health", new HealthHandler());
            server.setExecutor(null);  // Use default executor
            server.start();
            logMessage("HTTP server started on port 8080 for health checks.");
        } catch (IOException e) {
            logMessage("Failed to start HTTP server for health checks: " + e.getMessage());
        }

        try {
            // Register SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Modify connection URL to bypass SSL certificate validation
            String url = SERVER + ";databaseName=" + DATABASE + ";user=" + USERNAME + ";password=" + PASSWORD + ";encrypt=true;trustServerCertificate=true;";
            connection = DriverManager.getConnection(url);
            logMessage("Connection to SQL Server successful.");

            Statement statement = connection.createStatement();
            int cycleCount = 1;

            // Run the queries in a loop every 1 minute
            while (true) {
                logMessage("Running query set for the " + cycleCount + " time.");
                runQueries(statement);
                logMessage("Query set executed successfully for the " + cycleCount + " time. Waiting for 1 minute...");
                cycleCount++;
                TimeUnit.MINUTES.sleep(1);  // Sleep for 1 minute
            }
        } catch (Exception e) {
            logMessage("Error occurred while connecting or querying SQL Server: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    logMessage("Connection closed.");
                } catch (SQLException e) {
                    logMessage("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // Create a simple HTTP handler for health checks
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            t.getResponseBody().write(response.getBytes());
            t.getResponseBody().close();
        }
    }

    // Method to run all SQL queries
    private static void runQueries(Statement statement) {
        for (String query : queries) {
            try {
                // Log the query being executed
                logMessage("Executing query: " + query.substring(0, Math.min(query.length(), 50)) + "...");

                // Execute the query
                ResultSet resultSet = statement.executeQuery(query);

                // Count the rows and log the result
                int rowCount = 0;
                while (resultSet.next()) {
                    rowCount++;
                }
                logMessage("Query completed successfully. " + rowCount + " rows affected.");
            } catch (SQLException e) {
                logMessage("Error executing query: " + e.getMessage());
            }
        }
    }

    // Method to log messages
    private static void logMessage(String message) {
        logger.info(message);
        System.out.println(message);  // Optional: Print to console for container logs
    }
}
