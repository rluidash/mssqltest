import pymssql
import time
import logging
from datetime import datetime

# Connection parameters
server = 'mssql-server'  # Assuming your SQL Server is running locally
database = 'AdventureWorks'  # The database you're connecting to
username = 'SA'  # Your SQL Server username
password = 'password123'  # Your SQL Server password

# List of SQL queries to run
queries = [
    "SELECT * FROM Person.Address",  # Original query
    """SELECT * FROM Person.Person 
       JOIN Person.BusinessEntity ON Person.Person.BusinessEntityID = Person.BusinessEntity.BusinessEntityId
       JOIN Person.BusinessEntityAddress ON Person.BusinessEntityAddress.BusinessEntityID = Person.Person.BusinessEntityID;""",
    "SELECT * FROM Production.Product;",
    """SELECT * FROM Sales.SalesOrderDetail s 
       INNER JOIN Production.Product p ON s.ProductID = p.ProductID;"""
]

# Configure logging to log to a file called "mssql-query-run-log.txt"
log_filename = 'mssql-query-run-log.txt'
logging.basicConfig(filename=log_filename, level=logging.INFO,
                    format='%(asctime)s - %(message)s', datefmt='%Y-%m-%d %H:%M:%S')

def log_message(message):
    """Log both to the file and print to the console for debugging purposes"""
    logging.info(message)  # Log to file
    print(message)  # (Optional) Print to terminal if you want to see high-level messages

def run_queries(cursor):
    """Function to execute all queries without displaying result to the terminal"""
    for query in queries:
        try:
            # Execute the query
            cursor.execute(query)

            # Fetch the rows but do not print them to the terminal to suppress the output
            cursor.fetchall()
        except Exception as e:
            log_message(f"Error executing query: {e}")

# Establishing a connection to the database
try:
    connection = pymssql.connect(server=server, user=username, password=password, database=database)
    log_message("Connection to SQL Server successful.")

    cursor = connection.cursor()
    cycle_count = 1  # Initialize the query set running cycle counter

    # Run the queries in a loop every 10 minutes
    while True:
        log_message(f"Running query set for the {cycle_count} time.")

        run_queries(cursor)

        log_message(f"Query set executed successfully for the {cycle_count} time. Waiting for 10 minutes...")
        cycle_count += 1
        time.sleep(600)  # Sleep for 600 seconds (10 minutes)

except Exception as e:
    log_message(f"Error occurred while connecting or querying SQL Server: {e}")

finally:
    # Closing the connection to the database
    if connection:
        connection.close()
        log_message("Connection closed.")

