#!/bin/bash
#
# Parses the console output (that has been saved to a file) from evaluating a CRF model on a test set, and
# outputs a JSON snippet containing the evaluation metrics
#
# {
#   "Entity": "cl",
#   "Corpus": "craft",
#   "Precision": "0.8074",
#   "Recall": "0.7277",
#   "F1": "0.7655",
#   "TP": "1996",
#   "FP": "476",
#   "FN": "747"
# }

ENTITY_TYPE=$1
CORPUS=$2
CRF_PERFORMANCE_FILE=$3

grep ENTITY "$CRF_PERFORMANCE_FILE" | awk '{split($0,a,"\t"); print "Entity|Corpus|Precision|Recall|F1|TP|FP|FN\n'"$ENTITY_TYPE|$CORPUS"'|",a[2],"|",a[3],"|",a[4],"|",a[5],"|",a[6],"|",a[7]}' | tr -d " " | \
jq -Rn '
( input  | split("|") ) as $keys |
( inputs | split("|") ) as $vals |
[[$keys, $vals] | transpose[] | {key:.[0],value:.[1]}] | from_entries'

# jq code from https://stackoverflow.com/questions/38860529/create-json-using-jq-from-pipe-separated-keys-and-values-in-bash