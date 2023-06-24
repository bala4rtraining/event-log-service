FROM amazoncorretto:8

ENV SERVER_PORT=8057
ENV BASE_DIR=/var/run/bahmni-event-log-service
ENV CONTEXT_PATH=/event-log-service
ENV WAR_DIRECTORY=/var/run/bahmni-event-log-service/bahmni-event-log-service
ENV SERVER_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
ENV DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,address=8004,server=y,suspend=n"
ENV JAVA_HOME=/usr/java/jre1.8.0_131

# Used by envsubst command for replacing environment values at runtime
RUN yum install -y gettext

ADD https://repo.mybahmni.org/packages/build/bahmni-embedded-tomcat-8.0.42.jar /opt/bahmni-event-log-service/lib/bahmni-event-log-service.jar
COPY event-log-service/target/event-log-service.war

RUN mkdir -p ${WAR_DIRECTORY}
RUN cd ${WAR_DIRECTORY} && jar xvf /etc/bahmni-event-log-service/event-log-service.war

#COPY resources/log4j2.properties ${WAR_DIRECTORY}/WEB-INF/classes/
#COPY resources/bahmni-event-log-service.conf /etc/bahmni-event-log-service/bahmni-event-log-service.conf
#COPY resources/bahmni_eventlog_ssl.conf /etc/httpd/conf.d/bahmni_eventlog_ssl.conf
#COPY resources/run-liquibase.sh /opt/bahmni-event-log-service/etc/run-liquibase.sh
#RUN chmod +x /opt/bahmni-event-log-service/etc/run-liquibase.sh

ENTRYPOINT [ "java -Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m event-log-service/target/event-log-service.war" ]
