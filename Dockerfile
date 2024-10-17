# Define an argument for the SQL Server version, defaulting to 2017-latest
ARG SQL_SERVER_VERSION=2017-latest

# Use the SQL Server version argument in the FROM clause
FROM mcr.microsoft.com/mssql/server:${SQL_SERVER_VERSION}

# Set environment variables
ENV ACCEPT_EULA=Y

# Allow SA_PASSWORD to be set dynamically at runtime
ENV SA_PASSWORD=password123
# Default password, can be overridden

# Install the mssql-tools and dependencies
RUN apt-get update && apt-get install -y curl gnupg

RUN curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add -

RUN curl https://packages.microsoft.com/config/ubuntu/16.04/mssql-server-2017.list | tee /etc/apt/sources.list.d/mssql-server.list

RUN apt-get update && apt-get install -y mssql-server-fts

RUN apt-get install -y apt-transport-https && \
    apt-get install -y dnsutils iputils-ping vim && \
    curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - && \
    curl https://packages.microsoft.com/config/ubuntu/20.04/prod.list > /etc/apt/sources.list.d/mssql-release.list && \
    apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql17 mssql-tools && \
    echo 'export PATH="$PATH:/opt/mssql-tools/bin"' >> ~/.bashrc && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Create a directory for initialization scripts
RUN mkdir -p /usr/src/sqlscripts

# Copy the SQL script to the container
COPY oltp-install-script /usr/src/sqlscripts/oltp-install-script

# Run a custom SQL script during container startup
RUN mkdir -p /usr/init

COPY init_db.sh /usr/init/init_db.sh

# Make the initialization script executable
RUN chmod +x /usr/init/init_db.sh

# Expose the SQL Server port
EXPOSE 1433

# Set the entrypoint to the initialization script
ENTRYPOINT [ "/bin/bash", "/usr/init/init_db.sh" ]
