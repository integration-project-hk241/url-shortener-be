# Program Integration Project 01

This is my program integration project 01 programmed by using framework Spring Boot application with Docker support. Follow the instructions below to set up and run the project locally using IntelliJ IDEA, JDK 17, and Docker.

# Spring Boot Project

This is a Spring Boot application with Docker support. Follow the instructions below to set up and run the project locally using IntelliJ IDEA, JDK 17, and Docker.

## Prerequisites

Before you can run the application, make sure you have the following tools installed:

1. **IntelliJ IDEA**: A popular IDE for Java development.
    - Download and install IntelliJ IDEA from [here](https://www.jetbrains.com/idea/download/).
    - After installation, open the project in IntelliJ IDEA.

2. **JDK 17**: This project is built with Java 17. Make sure you have JDK 17 installed.
    - You can download and install JDK 17 from [here](https://adoptopenjdk.net/).
    - In IntelliJ IDEA, configure the project to use JDK 17 by going to `File > Project Structure > Project > Project SDK` and selecting JDK 17.

3. **Docker**: You need Docker to build and run the application in containers.
    - Download and install Docker from [here](https://www.docker.com/products/docker-desktop).
    - After installation, ensure Docker is running by checking the Docker icon in your system tray.

4. **Docker Compose**: Docker Compose is required to run multi-container applications.
    - Docker Compose comes bundled with Docker Desktop, so no additional installation is required.

---

## Setting Up the Project

1. **Clone the Repository**:
   Clone the repository to your local machine.

   ```bash
   git clone https://your-repository-url.git
   cd your-project-directory
   ```

2. **Open the Project in IntelliJ IDEA**:
   Open IntelliJ IDEA, then choose **File > Open** and select the project directory.

3. **Configure JDK**:
   Make sure JDK 17 is set as the project SDK in IntelliJ IDEA:
    - Go to **File > Project Structure > Project** and set the **Project SDK** to JDK 17.

---

## Building and Running the Application with Docker and Docker Compose

To build and run the Spring Boot application along with PostgreSQL using Docker and Docker Compose, follow the steps below:

### 1. **Build and Start the Application**

In the project directory, run the following command to build the Docker images and start the containers:

```bash
docker-compose up --build
```

This command will:
- Build the Docker images for both the Spring Boot application and PostgreSQL.
- Start the containers and link them together.

### 2. **Access the Application**

- The **Spring Boot application** will be accessible at [http://localhost:8080](http://localhost:8080).
- PostgreSQL will be available on [localhost:5432](localhost:5432).

### 3. **Stop the Application**

To stop and remove the containers, use the following command:

```bash
docker-compose down
```

This command will stop the running containers and remove them. However, your PostgreSQL data will be persisted in the Docker volume, so it won't be lost.

### 4. **View Logs**

To view the logs of the running services, use:

```bash
docker-compose logs
```

To view logs for a specific service (e.g., Spring Boot), use:

```bash
docker-compose logs springboot
```

---

## Troubleshooting

- **Docker is not running**: Ensure that Docker is started and running on your system. You can check the Docker status from the Docker Desktop application.
- **Port 8080 already in use**: If port `8080` is already used by another application, you can change the port in the `docker-compose.yml` file and rebuild the containers.
- **Database connection issues**: Ensure that the database service (`db`) is up and running before trying to access the Spring Boot application. If there are connection issues, check the logs using `docker-compose logs db`.

---

## Additional Notes

- **Docker Compose File**: The `docker-compose.yml` file defines two services: `springboot` (your Spring Boot application) and `db` (PostgreSQL database).
- **Dockerfiles**: The project includes two Dockerfiles:
    - `Dockerfile`: For building the Spring Boot application.
    - `Dockerfile.postgres`: For building the PostgreSQL service with custom settings (optional).

---
