#!/bin/bash
#
# Train a CRF model on the NCBI Disease Corpus and evaluate it against the NCBI Disease Corpus test set
#

# extract the version from the MODEL_VERSIONS file - this is used as part of the file name for the serialized model file
export VERSION=$(grep "DISEASE.NCBIDISEASE" /home/dev/MODEL_VERSIONS | cut -f 2 -d "=")

# replace the VERSION placeholder in the properties template file to create the properties file used by train.sh
envsubst < /home/dev/scripts/properties/ncbi-disease.properties.template > /home/dev/scripts/properties/ncbi-disease.properties

# gsutil the model and the performance data to cloud storage
gsutil /home/dev/scripts/gsutil-test-entrypoint.sh gs://translator-text-workflow-dev-public/models/entities/crf/ncbidisease/