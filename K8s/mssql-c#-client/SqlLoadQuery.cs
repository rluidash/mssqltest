using System;
using System.Data.SqlClient;
using System.Threading;
using System.Collections.Generic;
using System.IO;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using System.Runtime.InteropServices;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.DependencyInjection;

namespace SqlLoadQuery
{
    public class Program
    {
        // Change to 'public static' to make it accessible from other classes
        public static readonly string connectionString =
            "Server=mssql-server;Database=AdventureWorks;User Id=SA;Password=password123;Encrypt=true;TrustServerCertificate=true;";
        
        // List of SQL queries to run (Change to 'public static')
        public static readonly List<string> queries = new List<string>
        {
            "SELECT * FROM Person.Address",
            "SELECT * FROM Person.Person JOIN Person.BusinessEntity ON Person.Person.BusinessEntityID = Person.BusinessEntity.BusinessEntityId JOIN Person.BusinessEntityAddress ON Person.BusinessEntityAddress.BusinessEntityID = Person.Person.BusinessEntityID",
            "SELECT * FROM Production.Product",
            "SELECT * FROM Sales.SalesOrderDetail s INNER JOIN Production.Product p ON s.ProductID = p.ProductID"
        };

        public static void Main(string[] args)
        {
            // Print runtime identifier information
            Console.WriteLine("Runtime Identifier: " + RuntimeInformation.RuntimeIdentifier);

            CreateHostBuilder(args).Build().Run();
        }

        public static IHostBuilder CreateHostBuilder(string[] args) =>
            Host.CreateDefaultBuilder(args)
                .ConfigureWebHostDefaults(webBuilder =>
                {
                    webBuilder.UseStartup<Startup>();
                    webBuilder.UseUrls("http://*:80");  // Run on port 80
                });
    }

    public class Startup
    {
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddRouting();
        }

        public void Configure(Microsoft.AspNetCore.Builder.IApplicationBuilder app, ILogger<Startup> logger)
        {
            app.UseRouting();

            app.UseEndpoints(endpoints =>
            {
                // Frontend endpoints to display queries
                for (int i = 0; i < Program.queries.Count; i++)
                {
                    int index = i;  // Capture variable in closure
                    endpoints.MapGet($"/frontend/query{index + 1}", async context =>
                    {
                        string queryText = Program.queries[index];
                        await context.Response.WriteAsync($"<h1>Query {index + 1}</h1><p>{queryText}</p><a href='/result/query{index + 1}'>Run Query</a>");
                    });
                }

                // Result endpoints to execute queries and display results
                for (int i = 0; i < Program.queries.Count; i++)
                {
                    int index = i;  // Capture variable in closure
                    endpoints.MapGet($"/result/query{index + 1}", async context =>
                    {
                        string result = RunQuery(index, logger);
                        await context.Response.WriteAsync($"<h1>Results for Query {index + 1}</h1><pre>{result}</pre><a href='/frontend/query{index + 1}'>Back to Query</a>");
                    });
                }

                // Health check endpoint
                endpoints.MapGet("/health", async context =>
                {
                    await context.Response.WriteAsync("OK");
                });
            });
        }

        // Execute the query and return the result
        private static string RunQuery(int queryIndex, ILogger logger)
        {
            string result = "";
            string query = Program.queries[queryIndex];

            using (SqlConnection connection = new SqlConnection(Program.connectionString))  // Accessing public 'connectionString'
            {
                try
                {
                    connection.Open();
                    logger.LogInformation($"Executing query {queryIndex + 1}: {query}");

                    using (SqlCommand command = new SqlCommand(query, connection))
                    {
                        using (SqlDataReader reader = command.ExecuteReader())
                        {
                            int rowCount = 0;
                            while (reader.Read())
                            {
                                rowCount++;
                            }
                            result = $"Query {queryIndex + 1} completed successfully. {rowCount} rows affected.";
                            logger.LogInformation(result);
                        }
                    }
                }
                catch (Exception ex)
                {
                    result = $"Error occurred while querying SQL Server: {ex.Message}";
                    logger.LogError(result);
                }
                finally
                {
                    connection.Close();
                    logger.LogInformation($"Connection closed for query {queryIndex + 1}.");
                }
            }
            return result;
        }
    }
}
