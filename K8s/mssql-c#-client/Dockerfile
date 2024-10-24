# Use the official Microsoft .NET SDK image to build the app
FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /app

# Copy the .csproj file and restore dependencies
COPY SqlLoadQuery.csproj ./
RUN dotnet restore

# Copy the rest of the app source code
COPY . ./

# Build the app
RUN dotnet publish -c Release -o out

# Use the official ASP.NET runtime image to run the app
FROM mcr.microsoft.com/dotnet/aspnet:6.0
WORKDIR /app

# Install Microsoft SQL Server ODBC driver and mssql-tools
RUN apt-get update && apt-get install -y curl gnupg2 apt-transport-https && \
    curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - && \
    curl https://packages.microsoft.com/config/ubuntu/20.04/prod.list > /etc/apt/sources.list.d/msprod.list && \
    apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql17 mssql-tools unixodbc-dev \
    # Install any additional package if required
    && apt-get install -y procps unzip vim && \
    echo 'export PATH="$PATH:/opt/mssql-tools/bin"' >> ~/.bashrc && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set environment variables for OpenTelemetry and Splunk Profiler
ENV OTEL_SERVICE_NAME="SqlLoadQuery.dll"
ENV OTEL_RESOURCE_ATTRIBUTES="deployment.environment=dashbase-cluster,service.version=1.0.0"
ENV SPLUNK_PROFILER_ENABLED="false"
ENV SPLUNK_PROFILER_MEMORY_ENABLED="false"

# Download and install Splunk OpenTelemetry .NET instrumentation
RUN curl -sSfL https://github.com/signalfx/splunk-otel-dotnet/releases/latest/download/splunk-otel-dotnet-install.sh -O && \
    sh ./splunk-otel-dotnet-install.sh && \
    rm ./splunk-otel-dotnet-install.sh

# Copy the built app from the previous stage
COPY --from=build /app/out .

# Run the auto-instrumentation before running the app
CMD ["/bin/bash", "-c", ". $HOME/.splunk-otel-dotnet/instrument.sh && dotnet SqlLoadQuery.dll"]
