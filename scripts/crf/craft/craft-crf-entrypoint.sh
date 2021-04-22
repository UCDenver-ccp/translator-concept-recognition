#!/bin/bash
#
# ONT = one of the ontologies used by the CRAFT corpus, e.g. chebi
#

ONT=$1

# train the model - model will be written to /home/dev/crf-models/craft/[ONT]-ner-model.ser.gz
/home/dev/scripts/train.sh ${ONT}

# evaluate the model on the test set and write results (the standard error output stream) to file
/home/dev/scripts/test.sh ${ONT} 2> /home/dev/crf-performance/${ONT}.craft.out 

# generate the JSON snippet that contains the evaluation metrics
/home/dev/scripts/crf-performance-to-json.sh ${ONT} CRAFT /home/dev/crf-performance/${ONT}.craft.out > /home/dev/crf-performance/${ONT}.craft.json

