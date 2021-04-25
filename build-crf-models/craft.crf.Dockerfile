#
# When run, this container will train and evaluate a CRF model over one of the ontologies
# used by the CRAFT corpus.
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
    maven \
    wget \
    jq

RUN groupadd --gid 9001 dev && \
    useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev
    
WORKDIR /home/dev

# Download the CRAFT corpus
RUN wget https://github.com/UCDenver-ccp/CRAFT/archive/refs/tags/v4.0.1.tar.gz && \
    tar -xvf v4.0.1.tar.gz && \
    rm v4.0.1.tar.gz

# Download and build the Stanford CoreNLP library
# Note that version 4.2.0 produces a jar with version 4.1.0 -- so we rename it to use 4.2.0
ENV STANFORD_CORENLP_VERSION 4.2.0
RUN wget https://github.com/stanfordnlp/CoreNLP/archive/refs/tags/v4.2.0.tar.gz && \
    tar -xvf v4.2.0.tar.gz && \
    rm v4.2.0.tar.gz && \
    mvn package -f CoreNLP-4.2.0/pom.xml && \
    mv /home/dev/CoreNLP-4.2.0/target/stanford-corenlp-4.1.0.jar /home/dev/CoreNLP-4.2.0/target/stanford-corenlp-4.2.0.jar

# Copy the IOB file generation code to the container
COPY code/java /home/dev/code
COPY scripts-crf-models/craft /home/dev/scripts
COPY scripts-crf-models/crf-performance-to-json.sh /home/dev/scripts

# Give ownership to the dev user
RUN chown -R dev:dev /home/dev
USER dev
WORKDIR /home/dev/code

# Compile the IOB code
RUN mvn clean install

# Create the IOB files and split into train/test subsets
RUN mkdir /home/dev/iob-output && \
    mvn exec:java -Dexec.mainClass="edu.cuanschutz.ccp.iob.craft.CraftIOBFileFactory" -Dexec.args="/home/dev/CRAFT-4.0.1 /home/dev/iob-output OB"

# aggregate ob files here
RUN chmod 755 /home/dev/scripts/*.sh && \
    /home/dev/scripts/aggregate.sh && \
    mkdir /home/dev/crf-performance && \
    mkdir /home/dev/crf-models

# Copy CRF config and build scripts to the container
# USER root
# COPY scripts-crf-models/craft /home/dev/scripts
# COPY scripts-crf-models/crf-performance-to-json.sh /home/dev/scripts
# COPY scripts-crf-models/aggregate.sh /home/dev/scripts
# RUN chown -R dev:dev /home/dev/scripts
    

# USER dev
# RUN chmod 755 /home/dev/scripts/*.sh && \
#     mkdir /home/dev/crf-performance && \
#     # mkdir /home/dev/iob-output/aggregated && \
#     mkdir /home/dev/crf-models && \
#     /home/dev/scripts/aggregate.sh
    
ENTRYPOINT ["/home/dev/scripts/craft-crf-entrypoint.sh"]