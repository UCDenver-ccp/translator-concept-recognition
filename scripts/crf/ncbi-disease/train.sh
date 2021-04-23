#!/bin/bash
#
# Train a CRF model on the NLM Disease Corpus training set
#

CLASSPATH="/home/dev/CoreNLP-${STANFORD_CORENLP_VERSION}/target/stanford-corenlp-${STANFORD_CORENLP_VERSION}.jar"
PROPERTIES_FILE="/home/dev/scripts/properties/ncbi-disease.properties"

echo "CLASSPATH: $CLASSPATH"
echo "PROPERTIES_FILE: $PROPERTIES_FILE"

java -Xmx12G -cp ${CLASSPATH} edu.stanford.nlp.ie.crf.CRFClassifier \
     -prop ${PROPERTIES_FILE}


