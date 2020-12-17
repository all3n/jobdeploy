FROM maven:3.6.3-openjdk-8
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
  && echo 'Asia/Shanghai' >/etc/timezone
ADD /target/jobdeploy-core-0.0.1-SNAPSHOT.jar /app.jar
RUN useradd -m jobdeploy
WORKDIR /src
VOLUME ["/src", "/root/.m2"]
RUN echo "java -jar /app.jar \$@" > /usr/bin/deploy && chmod +x /usr/bin/deploy 
#ENTRYPOINT ["java", "-jar", "/app.jar"]
