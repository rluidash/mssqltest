#!/bin/bash

# Start SQL Server in the background
/opt/mssql/bin/sqlservr &

# Wait for SQL Server to start
echo "Waiting for SQL Server to start..."

# Loop to check if SQL Server is ready
until /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P $SA_PASSWORD -Q "SELECT 1" > /dev/null 2>&1; do
    echo "SQL Server is still starting..."
    sleep 5  # Wait for 5 seconds before checking again
done

echo "SQL Server has started."

# Run the SQL script to initialize the database
/opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P $SA_PASSWORD -i /usr/src/sqlscripts/oltp-install-script/instawdb.sql -l 120 | tee /usr/src/sqlscripts/oltp-install-script/instawdb_run.log

# Keep the container running after executing the script
wait
