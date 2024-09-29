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


### Base URL 

```
http://localhost:8080/kitchensink
```


## Endpoints

### User Endpoints

- **Get All Users**
   - **GET** `/user`
   - **Responses**:
      - 200: Successful retrieval of users.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Save User**
   - **POST** `/user`
   - **Request Body**: `UserReqDTO`
   - **Responses**:
      - 200: User saved successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Update User**
   - **PUT** `/user`
   - **Request Body**: `UserReqDTO`
   - **Responses**:
      - 200: User updated successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Get User by ID**
   - **GET** `/user/id/{id}`
   - **Parameters**: `id` (string)
   - **Responses**:
      - 200: User details retrieved successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Get User by Email**
   - **GET** `/user/email/{email}`
   - **Parameters**: `email` (string)
   - **Responses**:
      - 200: User details retrieved successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Change User Activation Status**
   - **POST** `/user/{id}/status/{status}`
   - **Parameters**: `id` (string), `status` (string: ACTIVE | BLOCKED)
   - **Responses**:
      - 200: User status changed successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Reset Password**
   - **POST** `/user/reset-password`
   - **Request Body**: `ResetPasswordReqDTO`
   - **Responses**:
      - 200: Password reset successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Change Password**
   - **POST** `/user/change-password`
   - **Request Body**: `ChangePasswordResDTO`
   - **Responses**:
      - 200: Password changed successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Delete User**
   - **DELETE** `/user/{id}`
   - **Parameters**: `id` (string)
   - **Responses**:
      - 200: User deleted successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

### Member Endpoints

- **Get All Members**
   - **GET** `/member`
   - **Responses**:
      - 200: Successful retrieval of members.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Add Member**
   - **POST** `/member`
   - **Request Body**: `MemberReqDTO`
   - **Responses**:
      - 200: Member added successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Update Member**
   - **PUT** `/member`
   - **Request Body**: `MemberReqDTO`
   - **Responses**:
      - 200: Member updated successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Get Member by ID**
   - **GET** `/member/id/{memberId}`
   - **Parameters**: `memberId` (string)
   - **Responses**:
      - 200: Member details retrieved successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Reset Member Password**
   - **POST** `/member/reset-password`
   - **Request Body**: `ResetPasswordReqDTO`
   - **Responses**:
      - 200: Member password reset successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Register Member**
   - **POST** `/member/register`
   - **Request Body**: `MemberReqDTO`
   - **Responses**:
      - 200: Member registered successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Change Member Status**
   - **POST** `/member/change-status`
   - **Parameters**: `memberId`, `status` (APPROVED | DECLINED | PENDING)
   - **Responses**:
      - 200: Member status changed successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Delete Member**
   - **DELETE** `/member/{memberId}`
   - **Parameters**: `memberId` (string)
   - **Responses**:
      - 200: Member deleted successfully.
      - 400: Invalid Request.
      - 500: Internal Server Error.

### Authentication Endpoints

- **Log In User**
   - **POST** `/auth/login`
   - **Request Body**: `LoginRequestDTO`
   - **Responses**:
      - 200: Login successful.
      - 400: Invalid Request.
      - 500: Internal Server Error.

- **Log Out User**
   - **POST** `/auth/logout`
   - **Headers**: `Authorization`
   - **Responses**:
      - 200: Logout successful.
      - 400: Invalid Request.
      - 500: Internal Server Error.

## Data Models

### UserReqDTO
```json
{
  "id": "string",
  "name": "string",
  "email": "string",
  "phoneNumber": "string",
  "password": "string",
  "roles": ["ADMIN", "USER", "VISITOR"],
  "status": "ACTIVE | BLOCKED"
}
```
## Global Exception Handling

The Kitchen Sink API includes a global exception handler to manage errors gracefully. The `GlobalExceptionHandler` class handles various exceptions and returns appropriate `APIResponseDTO` responses.

### Exception Handlers

- **ValidationException**: Handles validation errors with a 400 status.
- **NotFoundException**: Handles not found errors with a 404 status.
- **ObjectMappingException**: Handles object mapping errors with a 500 status.
- **UsernameNotFoundException**: Handles username not found errors with a 401 status.
- **NoHandlerFoundException**: Handles cases where no handler is found with a 404 status.
- **DuplicateKeyException**: Handles duplicate key errors (e.g., email already used) with a 400 status.
- **AccessDeniedException**: Handles access denied errors with a 403 status.
- **LockedException**: Handles locked user account errors with a 401 status.
- **SessionAuthenticationException**: Handles session authentication errors with a 401 status.
- **MethodArgumentNotValidException**: Handles argument validation errors with a 400 status.
- **HttpMessageNotReadableException**: Handles unreadable HTTP message errors with a 400 status.
- **Generic Exception**: Catches all other exceptions and returns a 500 status.

Each response includes an error message detailing the issue encountered.
