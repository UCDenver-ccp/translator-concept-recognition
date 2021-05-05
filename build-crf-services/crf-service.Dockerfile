FROM adoptopenjdk:8-jdk

# these arguments must be set in the `docker build` command.
# MODEL_KEY_ARG is the key in the MODEL_VERSIONS file that will be used to look up the model version
ARG MODEL_KEY_ARG
# MODEL_FILE_PREFIX_ARG is the prefix for the cloud storage location for the serialied model
ARG MODEL_FILE_PREFIX_ARG

ARG MODEL_VERSION_ARG

ENV MODEL_KEY  ${MODEL_KEY_ARG}
ENV MODEL_FILE_PREFIX ${MODEL_FILE_PREFIX_ARG}
ENV MODEL_VERSION ${MODEL_VERSION_ARG}

RUN apt-get update && apt-get install -y \
    maven \
    wget
    
# create the 'spring' user
RUN groupadd spring && \
    useradd --create-home --shell /bin/bash --no-log-init -g spring spring

# Download the CRF model
COPY MODEL_VERSIONS /home/spring/
WORKDIR /home/spring/crf-service
RUN wget "https://storage.googleapis.com/${MODEL_FILE_PREFIX}${MODEL_VERSION}.ser.gz" -O ner-model.ser.gz

# Download the Java dependencies as the spring user - this only gets run if the pom.xml file 
# changes and saves time below (during development) when code changes are made and the build is run.
COPY code/java/crf-service/pom.xml /home/spring/code/
WORKDIR /home/spring/code
USER spring
RUN mvn dependency:resolve
USER root

# Copy the code into the container
COPY code/java/crf-service /home/spring/code

# extract the version and artifactId from pom.xml and serialize them to 
# files so that they can be loaded as environment variables in later steps
RUN mvn help:evaluate -Dexpression=project.version -q -DforceStdout -f /home/spring/code/pom.xml > /home/spring/pom-version.txt
RUN mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout -f /home/spring/code/pom.xml > /home/spring/pom-artifact-id.txt

RUN chown -R spring:spring /home/spring
USER spring
WORKDIR /home/spring/code

RUN mvn clean install 

RUN POM_VERSION=$(cat /home/spring/pom-version.txt) && \
    POM_ARTIFACT_ID=$( cat /home/spring/pom-artifact-id.txt) && \
    cp /home/spring/code/target/${POM_ARTIFACT_ID}-${POM_VERSION}.jar /home/spring/crf-service/service.jar

ENV MAIN_OPTS=''
ENV JAVA_OPTS='-Xmx5g'

# RUN chmod 755 /home/spring/crf-service/entity-crf-service-entrypoint.sh
WORKDIR /home/spring/crf-service
ENTRYPOINT java $JAVA_OPTS -jar ./service.jar $MAIN_OPTS
EXPOSE 8080

