![Java CI workflow](https://github.com/UCDenver-ccp/translator-concept-recognition/workflows/Java%20CI/badge.svg)



# translator-concept-recognition
A repository dedicated to training and evaluating concept recognition pipelines and components that are used by the Text Mining Provider for the NCATS Biomedical Translator Program.


# Repository structure

```
translator-concept-recognition/
    |
    |--- build-crf-models/        ** Docker configuation for building & publishing CRF models **
    |
    |--- build-crf-services/      ** Docker configuration for building & publishing services to serve CRF models **
    |
    |--- code/
    |     |
    |     |--- java/        
    |           |
    |           |--- crf-service  ** Code for the RESTful Spring service that hosts the CRF process **
    |           |
    |           |--- crf-utility  ** Code for building the CRF models **
    |
    |--- scripts-crf-models/  ** scripts used by the 'build-crf-models' Docker files **
            
```



