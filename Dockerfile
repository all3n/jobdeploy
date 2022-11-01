FROM centos:7.9.2009
ENV JD_HOME /opt/jobdeploy


RUN yum install -y make python3 wget  java-1.8.0-openjdk openssh-clients && yum clean all
RUN wget --no-check-certificate https://dlcdn.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz -O /tmp/maven.tar.gz \
  && tar -zxvf /tmp/maven.tar.gz -C /opt \
  && rm /tmp/maven.tar.gz && ln -s /opt/apache-maven-3.8.6 /opt/maven

ADD target/jobdeploy-*-deploy.tar.gz $JD_HOME


ENV PATH /opt/jobdeploy/bin:/opt/maven/bin:$PATH
VOLUME /app
WORKDIR /app
RUN useradd deploy
RUN mkdir -p $JD_HOME/logs && chown deploy:deploy $JD_HOME/logs

CMD ["bash"]
