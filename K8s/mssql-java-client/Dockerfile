# Use Ubuntu as the base image
FROM ubuntu:20.04

# Install necessary packages (curl, gnupg2, and Java OpenJDK 11)
RUN apt-get update && apt-get install -y openjdk-11-jdk curl gnupg2 apt-transport-https

# Install Microsoft ODBC driver and mssql-tools18 for MS SQL server
RUN curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - && \
    curl https://packages.microsoft.com/config/ubuntu/22.04/prod.list > /etc/apt/sources.list.d/mssql-release.list
RUN apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql18 mssql-tools18 
RUN apt-get install -y unixodbc-dev

# Install Microsoft JDBC Driver for SQL Server (for Java applications)
RUN curl -L https://github.com/microsoft/mssql-jdbc/releases/download/v10.2.0/mssql-jdbc-10.2.0.jre11.jar -o /opt/mssql-jdbc.jar

# Set the working directory inside the container
WORKDIR /app

# Copy the Java source code to the container
COPY SqlLoadQuery.java .

# Compile the Java application
RUN javac SqlLoadQuery.java

ADD 'https://dtdg.co/latest-java-tracer' dd-java-agent.jar
# Run the Java application
CMD ["java", "-cp", "/opt/mssql-jdbc.jar:.", "SqlLoadQuery"]
