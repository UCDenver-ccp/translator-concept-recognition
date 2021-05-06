#!/bin/bash
#
# Train a CRF model on the HPO Corpus and evaluate it against the HPO Corpus test set
#

# extract the version from the MODEL_VERSIONS file - this is used as part of the file name for the serialized model file
export VERSION=$(grep "PHENOTYPE.HPO" /home/dev/MODEL_VERSIONS | cut -f 2 -d "=")

# replace the VERSION placeholder in the properties template file to create the properties file used by train.sh
envsubst < /home/dev/scripts/properties/hp.properties.template > /home/dev/scripts/properties/hp.properties

# train the model - model will be written to /home/dev/crf-models/hp-ner-model.ser.gz
/home/dev/scripts/train.sh

# evaluate the model on the test set and write results (the standard error output stream) to file
/home/dev/scripts/test.sh 2> /home/dev/crf-performance/phenotype.out 

# generate the JSON snippet that contains the evaluation metrics
/home/dev/scripts/crf-performance-to-json.sh hp LOBO_ET_AL /home/dev/crf-performance/phenotype.out > /home/dev/crf-performance/phenotype_${VERSION}.json

# gsutil the model and the performance data to cloud storage
gsutil cp /home/dev/crf-models/hpo-ner-model-${VERSION}.ser.gz gs://translator-text-workflow-dev-public/models/entities/crf/human-phenotype/
gsutil cp /home/dev/crf-performance/phenotype_${VERSION}.json gs://translator-text-workflow-dev-public/models/entities/crf/human-phenotype/
