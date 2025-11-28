FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create a non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the JAR file
COPY build/libs/*.jar app.jar

# Set ownership and permissions
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
