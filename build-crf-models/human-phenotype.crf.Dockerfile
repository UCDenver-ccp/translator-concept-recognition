#
# When run, this container will train and evaluate a CRF model based on the HPO corpus
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
    maven \
    wget \
    jq \
    unrar

RUN groupadd --gid 9001 dev && \
    useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev
    
WORKDIR /home/dev/corpus

# Download the NLM Disease corpus
RUN wget https://github.com/lasigeBioTM/IHP/raw/master/GSC%2B.rar && \
    unrar x GSC+.rar && \
    rm GSC+.rar

WORKDIR /home/dev

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
COPY scripts-crf-models/human-phenotype /home/dev/scripts
COPY scripts-crf-models/crf-performance-to-json.sh /home/dev/scripts

# Give ownership to the dev user
RUN chown -R dev:dev /home/dev
USER dev
WORKDIR /home/dev/code

# Compile the IOB code
RUN mvn clean install

# Create the IOB files and split into train/test subsets
RUN mkdir -p /home/dev/iob-output && \
    mvn exec:java -Dexec.mainClass="edu.cuanschutz.ccp.iob.hpo.SplitHpoCorpusTestTrain" -Dexec.args="/home/dev/corpus /home/dev/iob-output"

RUN mvn exec:java -Dexec.mainClass="edu.cuanschutz.ccp.iob.hpo.HpoCorpusToOBFormat" -Dexec.args="/home/dev/iob-output"

# aggregate ob files here
RUN chmod 755 /home/dev/scripts/*.sh && \
    mkdir /home/dev/crf-performance && \
    mkdir /home/dev/crf-models && \
    /home/dev/scripts/aggregate.sh

ENTRYPOINT ["/home/dev/scripts/human-phenotype-crf-entrypoint.sh"]