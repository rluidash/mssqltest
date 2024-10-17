1. Build

Build the docker image name with custom-mssql-server-with-tools

```
docker build -t custom-mssql-server-with-tools .
```
OR

```
docker build --platform linux/amd64 -t custom-mssql-server-with-tools .
```

To build a custom MS SQL server 2022 image

```
docker build -t custom-mssql-server-with-tools --build-arg SQL_SERVER_VERSION=2012-latest .
```

2. Run the MS SQL docker container locally

```
docker run -d --name my-mssql-container -p 1433:1433 \
-v mssql_data:/var/opt/mssql \
custom-mssql-server-with-tools
```

OR Run with different SA password

```
docker run -d --name my-mssql-container -e SA_PASSWORD=NewStrongPassword123 -p 1433:1433 \
-v mssql_data:/var/opt/mssql \
custom-mssql-server-with-tools
```

3. Run SQL load script

prepare python dependency, assume the python version is 3.6 or above

```

/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"

brew update
# brew update may ask you to fetch homebrew-core and homebrew-cask, see below
# git -C /usr/local/Homebrew/Library/Taps/homebrew/homebrew-core fetch --unshallow
# git -C /usr/local/Homebrew/Library/Taps/homebrew/homebrew-cask fetch --unshallow

brew tap microsoft/mssql-release https://github.com/Microsoft/homebrew-mssql-release

HOMEBREW_ACCEPT_EULA=Y brew install msodbcsql18 mssql-tools18

brew install freetds
pip install -r requirement.txt

```

Run the sqlloadquery.py script

```
python sqlloadquery.py
```

The query will run several Select statements in every 10 min against the AdventureWorks database running on MS SQL container 


