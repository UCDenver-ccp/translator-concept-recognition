#!/bin/bash
#
# This script works in conjuntion with the File Changes Github Action (https://github.com/marketplace/actions/file-changes-action).
# It is used for workflow logic by looking for a pattern in the list of files that changed for a commit and returning an error code if
# the pattern is not found. The error code causes downstream workflow steps to be skipped.
#


PATTERN=$1
FILE=$2

# if anything in the src/ directory was changed, then we want to rebuild the models, so return 0
if grep -q src "$FILE"; then
  exit 0
fi

if grep -q "$PATTERN" "$FILE"; then
  exit 0
fi

exit 1