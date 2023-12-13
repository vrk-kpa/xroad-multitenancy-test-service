FROM amazoncorretto:17-alpine

WORKDIR /app

ADD target/multitenancy-test-client-*.jar test-client.jar
ENTRYPOINT exec java $JAVA_OPTS -jar /app/test-client.jar

# Expose default port
EXPOSE 8443
