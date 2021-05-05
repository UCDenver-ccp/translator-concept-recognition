#
# When run, this container will train and evaluate a CRF model based on the NCBI Disease Corpus
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
#     maven \
#     wget \
#     jq \
#     unzip \
#     gettext \
    apt-transport-https \
    ca-certificates \
    gnupg \
    curl 
    
# Install the Google Cloud SDK
RUN echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list && \
    curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key --keyring /usr/share/keyrings/cloud.google.gpg add - && \
    apt-get update && apt-get install -y google-cloud-sdk

# Make sure gsutil will use the default service account
RUN echo ‘[GoogleCompute]\nservice_account = default’ > /etc/boto.cfg

# Create the dev user
RUN groupadd --gid 9001 dev && \
    useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev
    
WORKDIR /home/dev/corpus

COPY scripts-crf-models/ncbi-disease /home/dev/scripts

# Give ownership to the dev user
RUN chown -R dev:dev /home/dev
USER dev
RUN chmod 755 home/dev/scripts/gsutil-test-entrypoint.sh

ENTRYPOINT ["/home/dev/scripts/gsutil-test-entrypoint.sh"]