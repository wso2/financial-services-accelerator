# Consent Management API

A lightweight RESTful API for managing user consent records, built using JAX-RS with Apache CXF.

## 🧰 Features

- Create, retrieve, update, and revoke and track consent records
- Clean and minimal implementation for quick integration or extension
- Dockerized for easy deployment

## 🛠️ Technologies

- Java
- Apache CXF (JAX-RS)
- Maven
- Tomcat
- Docker

---

## 🚀 Getting Started

Follow these steps to build and run the project using Docker.

### 1. Clone the Repository

```bash

git clone https://github.com/ParameswaranSajeenthiran/consent-mgt-api.git

cd consent-mgt-api
```

### 2. Configure Database 

Copy the context.xml to same folder and configure the database credentials

| Placeholder       | Description                          | Example                 |
|-------------------|--------------------------------------|-------------------------|
| `<USER_NAME>`     | Your MySQL database username         | `wso2_user`             |
| `<PASSWORD>`      | Your MySQL database password         | `your_secure_password` |
| `<DATABASE_HOST>` | Hostname or IP address of the DB     | `localhost` or `db`     |
| `<DATABASE_NAME>` | Name of your MySQL database          | `wso2_consent_db`       |



```bash

cp deployment/context.xml.example deployment/context.xml
```


### 3. Build the Docker Image

```bash

docker build -t consent-rest-api -f deployment/Dockerfile .

```

### 4. Run the Docker Container

```bash

docker run -p 8080:8080 consent-rest-api

```

Explore the APIs available in the Swagger UI at http://localhost:8080/swagger-ui/index.html?url=http://localhost:8080/api/openapi.json





