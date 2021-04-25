#!/bin/bash
#
# Train a CRF model on the HPO Corpus and evaluate it against the HPO Corpus test set
#

# train the model - model will be written to /home/dev/crf-models/hp-ner-model.ser.gz
/home/dev/scripts/train.sh

# evaluate the model on the test set and write results (the standard error output stream) to file
/home/dev/scripts/test.sh 2> /home/dev/crf-performance/phenotype.out 

# generate the JSON snippet that contains the evaluation metrics
/home/dev/scripts/crf-performance-to-json.sh hp LOBO_ET_AL /home/dev/crf-performance/phenotype.out > /home/dev/crf-performance/phenotype.json

