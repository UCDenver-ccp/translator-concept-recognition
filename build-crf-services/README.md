# Building container to host a CRF service

Note, these builds are automated via GitHub actions. Instructions below detail how one would build/deploy/run a CRF service manually.
We will use the CRF model trained on the `CHEBI` annotations in the CRAFT corpus for this example. Setup for any of the available vocabularies will follow a similar pattern. Note if you plan to use your own custom generated model, the current implementation is specific to the Google Cloud environment and the model will need to be stored in a publicly accessible Google Storage bucket (or the code will need to be revised).

Extract the `MODEL_KEY` and `MODEL_FILE_PREFIX` variables from the `chebi-craft.env` file. In this case, those values are shown below:
```
MODEL_KEY=CHEBI.CRAFT
MODEL_FILE_PREFIX=-public/models/entities/crf/craft/chebi/chebi-ner-model-
```

```bash
docker build -t [NAME]-crf-service:[MODEL_VERSION] \
             -f build-crf-services/crf-service.Dockerfile \
             --build-arg MODEL_KEY_ARG=[MODEL_KEY]  \
             --build-arg MODEL_FILE_PREFIX_ARG=translator-text-workflow-dev[MODEL_FILE_PREFIX] \
             --build-arg MODEL_VERSION_ARG=[MODEL_VERSION] .
```

where,
* [NAME] is a unique name for this service. In our example case `chebi` would be an appropriate value.
* [MODEL_VERSION] is an available version of the trained CRF model. The most recent version for some pre-computed models are listed in the `MODEL_VERSIONS` file. If you have build your own model, then the version will depend on your own versioning scheme.
* [MODEL_KEY] is a value extracted from the appropriate *.env file for the vocabulary you have selected, e.g. `chebi-craft.env` in the case of our example.
* [MODEL_FILE_PREFIX] is a value extracted from the appropriate *.env file for the vocabulary you have selected, e.g. `chebi-craft.env` in the case of our example.

The complete example command to build a CHEBI CRF service container using a pre-computed model is:
```
docker build -t chebi-crf-service:0.1 \
             -f build-crf-services/crf-service.Dockerfile \
             --build-arg MODEL_KEY_ARG=CHEBI.CRAFT \
             --build-arg MODEL_FILE_PREFIX_ARG=translator-text-workflow-dev-public/models/entities/crf/craft/chebi/chebi-ner-model- \
             --build-arg MODEL_VERSION_ARG=0.1 .
```

# Deploying the CRF service

To run the CRF service (after building the container as described above):

```
docker run -d --name crf-chebi -p 8080:8080 chebi-crf-service:0.1
```

# Using the CRF service

Once deployed, the service can queried by providing one or more sentences to be processed, e.g.

```
curl -H "Content-type:text/html; charset=UTF-8" -d "12345	T1	sentence 0 55	Chlorine is a chemical entity mentioned in the first sentence of document 12345.
7890	T1	sentence 0 85	The first sentence in document 7890 also mentions chlorine." http://localhost:8080/crf
```

Each sentence must use the following format:
document_id `[TAB]` t_id `[TAB]` annotation_type `[SPACE]` sentence_start_offset `[SPACE]` sentence_end_offset `[TAB]` sentence_text

where,
* `document_id` is the unique identifier for the document to which the sentence belongs 
* `t_id` is an id for the sentence within the document of the form `T` spliced with an integer
* `annotation_type` is "sentence" in this case
* `sentence_start_offset` is the character offset relative to the entire document for the first character of the sentence
* `sentence_end_offset` is the character offset relative to the entire document for the end of the sentence
* `sentence_text` is the text of the sentence to be processed

### Results are returned as JSON
For the query above, the result will look like the following where any identified entity annotations are mapped to their respective document identifier.

```
{
  "docIdToBionlpEntityAnnotationsMap": {
    "7890": "T0 ENTITY 50 58    chlorine",
    "12345": "T0    ENTITY 0 8    Chlorine
              T1    ENTITY 14 22  chemical"
  }
}
```


