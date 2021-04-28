#
# When run, this container will train and evaluate a CRF model based on the NLM-Chem Corpus
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
    maven \
    wget \
    jq \
    unzip \
    gettext

RUN groupadd --gid 9001 dev && \
    useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev
    
WORKDIR /home/dev/corpus

# Download the NLM-Chem corpus
RUN wget https://ftp.ncbi.nlm.nih.gov/pub/lu/NLMChem/NLM-Chem-corpus.zip && \
    unzip NLM-Chem-corpus.zip && rm NLM-Chem-corpus.zip

WORKDIR /home/dev/corpus/FINAL_v1
RUN chmod 755 make_splits.sh && ./make_splits.sh

# /home/dev/corpus/FINAL_v1/Split_Dev
# /home/dev/corpus/FINAL_v1/Split_Test
# /home/dev/corpus/FINAL_v1/Split_Train

WORKDIR /home/dev

# Download and build the Stanford CoreNLP library
# Note that version 4.2.0 produces a jar with version 4.1.0 -- so we rename it to use 4.2.0
ENV STANFORD_CORENLP_VERSION 4.2.0
RUN wget https://github.com/stanfordnlp/CoreNLP/archive/refs/tags/v4.2.0.tar.gz && \
    tar -xvf v4.2.0.tar.gz && \
    rm v4.2.0.tar.gz && \
    mvn package -f CoreNLP-4.2.0/pom.xml && \
    mv /home/dev/CoreNLP-4.2.0/target/stanford-corenlp-4.1.0.jar /home/dev/CoreNLP-4.2.0/target/stanford-corenlp-4.2.0.jar

# Download the Java dependencies as the dev user - this only gets run if the pom.xml file 
# changes and saves time below (during development) when code changes are made and the build is run.
COPY code/java/crf-utility/pom.xml /home/dev/code/
WORKDIR /home/dev/code
USER dev
RUN mvn dependency:resolve
USER root

# Copy the IOB file generation code to the container
COPY code/java/crf-utility /home/dev/code
COPY scripts-crf-models/nlm-chem /home/dev/scripts
COPY scripts-crf-models/crf-performance-to-json.sh /home/dev/scripts
COPY MODEL_VERSIONS /home/dev/

# Give ownership to the dev user
RUN chown -R dev:dev /home/dev
USER dev
WORKDIR /home/dev/code

# Compile the IOB code
RUN mvn clean install

# Create the IOB files and split into train/test subsets
RUN mkdir /home/dev/iob-output && \
    mvn exec:java -Dexec.mainClass="edu.cuanschutz.ccp.iob.chemical.NlmChemCorpusToOBFormat" -Dexec.args="/home/dev/corpus/FINAL_v1/Split_Dev /home/dev/iob-output/dev OB" && \
    mvn exec:java -Dexec.mainClass="edu.cuanschutz.ccp.iob.chemical.NlmChemCorpusToOBFormat" -Dexec.args="/home/dev/corpus/FINAL_v1/Split_Test /home/dev/iob-output/test OB" && \
    mvn exec:java -Dexec.mainClass="edu.cuanschutz.ccp.iob.chemical.NlmChemCorpusToOBFormat" -Dexec.args="/home/dev/corpus/FINAL_v1/Split_Train /home/dev/iob-output/train OB"

# aggregate ob files here
RUN chmod 755 /home/dev/scripts/*.sh && \
    mkdir /home/dev/crf-performance && \
    mkdir /home/dev/crf-models && \
    /home/dev/scripts/aggregate.sh

ENTRYPOINT ["/home/dev/scripts/nlm-chem-crf-entrypoint.sh"]