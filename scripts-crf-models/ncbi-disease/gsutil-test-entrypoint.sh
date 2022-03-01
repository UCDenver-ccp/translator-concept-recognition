#!/bin/bash
#
# Train a CRF model on the NCBI Disease Corpus and evaluate it against the NCBI Disease Corpus test set
#

# gsutil the model and the performance data to cloud storage
gsutil cp /home/dev/scripts/gsutil-test-entrypoint.sh gs://translator-text-workflow-dev-public/models/entities/crf/ncbidisease/