# --- Etapa 1: BUILD ---
# Usa una imagen base con un JDK para compilar la aplicación.
# Se recomienda usar versiones específicas para la estabilidad y seguridad.
FROM eclipse-temurin:21-jdk-jammy AS build

# Establece el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copia los archivos de configuración de Maven y el pom.xml.
# Esto aprovecha el caché de Docker si las dependencias no cambian.
COPY pom.xml .

# Copia el script del Maven Wrapper (mvnw)
COPY mvnw .

# Copia el directorio de configuración del Maven Wrapper (.mvn)
COPY .mvn .mvn

# Dale permisos de ejecución al script mvnw (¡IMPORTANTE!)
RUN chmod +x ./mvnw

# Copia solo los directorios src/ de cada módulo.
# Esto es importante para proyectos multi-módulo si los tuvieras.
# Para un proyecto monolítico, 'COPY src src' sería suficiente,
# pero esta forma es más explícita y robusta.
COPY src src

# Construye el proyecto Spring Boot y crea el JAR ejecutable.
# Se usa -DskipTests para no ejecutar los tests unitarios en la etapa de construcción de la imagen.
# Los tests de integración con Testcontainers los ejecutaremos de otra forma.
RUN ./mvnw clean package -DskipTests

# --- Etapa 2: RUNTIME ---
# Usa una imagen base ligera con solo el JRE para ejecutar la aplicación.
# Alpine es muy pequeña, o puedes usar otra distribución ligera como Jammy (Ubuntu) con JRE.
FROM eclipse-temurin:21-jre-jammy

# Establece el directorio de trabajo en la imagen de ejecución.
WORKDIR /app

# Copia el JAR ejecutable de la etapa de construcción a la etapa de ejecución.
# El nombre del JAR se genera por Maven (artifactId-version.jar).
# Asume que el nombre del JAR es el que se define en tu pom.xml (ej. gestorinventarioproductos-0.0.1-SNAPSHOT.jar)
# AJUSTA ESTE NOMBRE AL DE TU ARTIFACTID Y VERSION SI ES DIFERENTE
COPY --from=build /app/target/gestorinventarioproductos-0.0.1-SNAPSHOT.jar gestorinventarioproductos.jar

# Expone el puerto en el que la aplicación Spring Boot escuchará.
# Esto no publica el puerto, solo lo documenta.
EXPOSE 8080

# Comando para ejecutar la aplicación Spring Boot cuando el contenedor se inicie.
ENTRYPOINT ["java", "-jar", "gestorinventarioproductos.jar"]

