FROM eclipse-temurin:8-jdk

ARG JAR_FILE=target/*.war
COPY ${JAR_FILE} event-log-service-webapp.war

ENTRYPOINT exec java $JAVA_OPTS -jar /event-log-service-webapp.war
