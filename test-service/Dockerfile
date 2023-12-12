FROM amazoncorretto:17-alpine

WORKDIR /app

# Add the test service jar to container
ADD target/multitenancy-test-service-*.jar test-service.jar

# Entry with exec
ENTRYPOINT exec java $JAVA_OPTS -jar /app/test-service.jar

# Expose default port
EXPOSE 8080
