FROM eclipse-temurin:8-jdk

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} event-log-service-webapp.jar

ENTRYPOINT exec java $JAVA_OPTS -jar /event-log-service-webapp.jar
