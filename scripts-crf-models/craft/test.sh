#!/bin/bash
#
# Evaluate the CRF model for the specified ontology over its corresponding test set
#

ONT=$1
CLASSPATH="/home/dev/CoreNLP-${STANFORD_CORENLP_VERSION}/target/stanford-corenlp-${STANFORD_CORENLP_VERSION}.jar"
MODEL_FILE="/home/dev/crf-models/${ONT}-ner-model-${VERSION}.ser.gz"
TEST_FILE="/home/dev/iob-output/aggregated/${ONT}.test.ob"

echo "CLASSPATH: $CLASSPATH"
echo "MODEL_FILE: $MODEL_FILE"
echo "TEST_FILE: $TEST_FILE"

java -Xmx12G -cp ${CLASSPATH} edu.stanford.nlp.ie.crf.CRFClassifier \
     -loadClassifier ${MODEL_FILE} -testFile ${TEST_FILE}
