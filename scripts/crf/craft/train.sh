#!/bin/bash
#
# Train a CRF model for the specified ontology over its corresponding training set
#

ONT=$1
CLASSPATH="/home/dev/CoreNLP-${STANFORD_CORENLP_VERSION}/target/stanford-corenlp-${STANFORD_CORENLP_VERSION}.jar"
PROPERTIES_FILE="/home/dev/scripts/properties/$ONT.properties"

echo "CLASSPATH: $CLASSPATH"
echo "PROPERTIES_FILE: $PROPERTIES_FILE"

java -Xmx12G -cp ${CLASSPATH} edu.stanford.nlp.ie.crf.CRFClassifier \
     -prop ${PROPERTIES_FILE}
