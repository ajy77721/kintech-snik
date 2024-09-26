# SINK

project for Spring Boot

## Project Description

The `sink` project is a demo application built using Spring Boot. It leverages various Spring Boot starters and dependencies to provide a robust backend service. The project is designed to interact with a MongoDB database and exposes RESTful APIs documented using Swagger.

### Key Features:
- **Java 21**: The project is built using Java 21, ensuring modern language features and performance improvements.
- **Spring Boot**: Utilizes Spring Boot for rapid application development and dependency management.
- **MongoDB**: Integrates with MongoDB for data storage and retrieval.
- **Swagger**: Provides API documentation and exploration through Swagger UI.
- **Security**: Implements security features using Spring Security and JWT (JSON Web Tokens).
- **Actuator**: Provides production-ready features to help monitor and manage the application.

### Dependencies:
- **Spring Boot Starters**: Includes starters for Actuator, Data JPA, Data MongoDB, Security, Validation, and Web.
- **Springdoc OpenAPI**: For generating API documentation.
- **Jackson**: For JSON processing.
- **Lombok**: To reduce boilerplate code.
- **JUnit**: For testing purposes.

### APIs:
The project exposes several RESTful APIs for CRUD operations and other functionalities. These APIs are documented and can be explored using the Swagger UI available at `/swagger-ui.html`.

## Requirements

- Java 21
- MongoDB\*

## Setup

### Java 21

Ensure you have Java 21 installed on your machine. You can download it from the [official website](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html).

### MongoDB

1. Download and install MongoDB from the [official website](https://www.mongodb.com/try/download/community).
2. Start the MongoDB server:
    ```sh
    mongod
    ```

## Running the Application

1. Clone the repository:
    ```sh
    git clone <repository-url>
    ```
2. Navigate to the project directory:
    ```sh
    cd sink
    ```
3. Build the project using Maven:
    ```sh
    mvn clean install
    ```
4. Run the application:
    ```sh
    mvn spring-boot:run
    ```

## Swagger

You can access the Swagger UI to explore the API documentation at: `/swagger-ui.html`

## Actuator

Spring Boot Actuator provides several endpoints to monitor and manage your application. Some of the key endpoints include:
- `/actuator/health`: Shows application health information.
- `/actuator/info`: Displays arbitrary application info.
- `/actuator/metrics`: Shows metrics information.

These endpoints can be accessed and configured in the `application.properties` file.


### Access Logging

To enable access logging, you need to add the following VM option and properties:

**VM Option:**
```sh
-Dserver.tomcat.basedir=./logs/access/
```
**Properties:**
```properties
server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.directory=./logs
server.tomcat.accesslog.prefix=access_log
server.tomcat.accesslog.suffix=.log
server.tomcat.accesslog.pattern=%h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\"
server.tomcat.accesslog.request-attributes-enabled=true
server.tomcat.accesslog.file-date-format=.yyyy-MM-dd
server.tomcat.accesslog.include-query-string=true
server.tomcat.accesslog.include-headers=false
server.tomcat.accesslog.rotate=true
```

