#!/bin/bash
#
# Evaluate the HPO model on the HPO test set
#

CLASSPATH="/home/dev/CoreNLP-${STANFORD_CORENLP_VERSION}/target/stanford-corenlp-${STANFORD_CORENLP_VERSION}.jar"
MODEL_FILE="/home/dev/crf-models/hp-ner-model-${VERSION}.ser.gz"
TEST_FILE="/home/dev/iob-output/aggregated/hp.test.ob"

echo "CLASSPATH: $CLASSPATH"
echo "MODEL_FILE: $MODEL_FILE"
echo "TEST_FILE: $TEST_FILE"

java -Xmx12G -cp $CLASSPATH edu.stanford.nlp.ie.crf.CRFClassifier \
     -loadClassifier $MODEL_FILE -testFile $TEST_FILE
