# Use the official Microsoft .NET SDK image to build the app
FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /app

# Copy the .csproj and restore any dependencies
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
    apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql17 mssql-tools unixodbc-dev && \
    echo 'export PATH="$PATH:/opt/mssql-tools/bin"' >> ~/.bashrc && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy the built app from the previous stage
COPY --from=build /app/out .

# Run the app
ENTRYPOINT ["dotnet", "SqlLoadQuery.dll"]
