FROM askyora/base-jre:11
VOLUME /tmp
EXPOSE 8080
ADD target/*.jar app.jar
ENV JAVA_OPTS="-noverify -XX:+AlwaysPreTouch"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
