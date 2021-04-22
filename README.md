# translator-concept-recognition
A repository dedicated to training and evaluating concept recognition pipelines and components that are used by the Text Mining Provider for the NCATS Biomedical Translator Program.



# Training a CRF on the CRAFT Corpus

```
docker build -t craft-crf -f craft.crf.Dockerfile .
docker run --rm -v [LOCAL_MODEL_DIR]:/home/dev/crf-models -v [LOCAL_EVAL_DIR]:/home/dev/crf-performance craft-crf cl
```

where,
* [LOCAL_MODEL_DIR] is a directory on the host machine where the CRF model will be written
* [LOCAL_EVAL_DIR] is a directory on the host machine where the evaluation metrics for the trained model against the test set will be written


