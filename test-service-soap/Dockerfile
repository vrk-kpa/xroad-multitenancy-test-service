# Base java:11
FROM eclipse-temurin:11-jre

WORKDIR /app

# Add Rest Gateway jar to container
ADD target/example-adapter-*.war example-adapter.war

# Entry in json format
ENTRYPOINT ["java", "-jar", "/app/example-adapter.war"]

# Expose Tomcat
EXPOSE 8080
