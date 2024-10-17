# Base image: Ubuntu 20.04
FROM ubuntu:20.04
# Set environment variables to prevent tzdata prompts during installation
ENV DEBIAN_FRONTEND=noninteractive
# Update the package lists and install required system dependencies
RUN apt-get update && apt-get install -y \
    curl \
    gnupg2 \
    apt-transport-https \
    ca-certificates \
    build-essential \
    python3.8 \
    python3.8-dev \
    python3-pip \
    unixodbc-dev \
    libpq-dev \
    libssl-dev \
    libffi-dev \
    iputils-ping \
    apt-transport-https \
    debconf-utils gnupg2 \
    dnsutils \
    iputils-ping \
    vim \
    locales \
    && rm -rf /var/lib/apt/lists/*
# Set Python 3.8 as default
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.8 1
RUN update-alternatives --set python3 /usr/bin/python3.8

# Install Microsoft ODBC Driver and mssql-tools

# adding custom MS repository
RUN curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add -
RUN curl https://packages.microsoft.com/config/ubuntu/22.04/prod.list > /etc/apt/sources.list.d/mssql-release.list

# install SQL Server drivers and tools
RUN apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql18 mssql-tools18
RUN echo 'export PATH="$PATH:/opt/mssql-tools18/bin"' >> ~/.bashrc
RUN /bin/bash -c "source ~/.bashrc"

# Install pip for Python 3.8
RUN python3.8 -m pip install --upgrade pip
RUN mkdir -p /app
# Copy requirements for Python dependencies
COPY requirements.txt .
# Install Python packages
RUN pip install -r requirements.txt
# Copy the Python script into the container
COPY sqlloadquery.py /app/sqlloadquery.py
COPY oltp-install-script /tmp/app/oltp-install-script
# Set working directory
WORKDIR /app

RUN locale-gen en_US.UTF-8
RUN update-locale LANG=en_US.UTF-8

CMD /bin/bash 
# Note: We are not specifying the CMD or ENTRYPOINT here
# Kubernetes will handle when the script is executed
