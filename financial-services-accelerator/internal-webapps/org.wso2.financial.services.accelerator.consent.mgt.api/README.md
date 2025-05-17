# Consent Management API

A lightweight RESTful API for managing user consent records, built using JAX-RS with Apache CXF.

## Features

- Create, retrieve, update, and revoke and track consent records
- Clean and minimal implementation for quick integration or extension
- Dockerized for easy deployment

##  Technologies

- Java - 11
- Apache CXF (JAX-RS)
- Maven - 3.9.9-eclipse-temurin-17
- Tomcat - 9.0
- Docker

---

##  Getting Started

Follow these steps to build and run the project using Docker.

### 1. Clone the Repository

```bash

git clone https://github.com/wso2/financial-services-accelerator

git checkout 5.0.0-consent
cd financial-services-accelerator/internal-webapps/org.wso2.financial.services.accelerator.consent.mgt.api

```

### 2. Configure Database 

Copy the context.xml to same folder and configure the database credentials

```bash

cp deployment/context.xml.example deployment/context.xml
```

| Placeholder       | Description                        | Example                 |
|-------------------|------------------------------------|-------------------------|
| `<USER_NAME>`     | Your MySQL database username       | `wso2_user`             |
| `<PASSWORD>`      | Your MySQL database password       | `your_secure_password` |
| `<DATABASE_CONNECTION_URI>`   | Your MySQL database connection uri | `jdbc:mysql://localhost:3306/fs_consentdb_test?useSSL=false`   |
| `<DATABASE_NAME>` | Name of your MySQL database        | `wso2_consent_db`       |


### 3. Build the Docker Image

```bash

docker build -t consent-rest-api -f deployment/Dockerfile .

```

### 4. Run the Docker Container

```bash

docker run -p 8080:8080 consent-rest-api

```
