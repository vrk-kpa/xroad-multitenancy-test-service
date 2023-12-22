# Base java:11
FROM amazoncorretto:11-alpine

WORKDIR /app

# Add Rest Gateway jar to container
ADD target/xroad-multitenancy-soap-test-service-*.war test-service-soap.war

# Entry in json format
ENTRYPOINT ["java", "-jar", "/app/test-service-soap.war"]

# Expose Tomcat
EXPOSE 8080
