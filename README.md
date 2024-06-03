# Article Classification using k-Nearest Neighbors
#### Group: Grant Baer, Logan Baker, Blake Silton, and Kassi Winter

### Overall Idea
* Split docs into training, testing, validation sets (70-20-10)
  * We can encode the labels as -1, 0, 1; 
    don't need to use one-hot bc `left` is inherently further away from `right` than from `center`
* Load up each into document collections
  * Do we normalize these?
* TF-IDF to find 500 (subject to change) most common words
* See how different values of k perform (using validation set for hyperparameter tuning)
    ```python
    f1_scores = []
    for k in range(max_k):
        computer_classification = {}  # Note this is a dict
        for v_doc in validation_set:  # is this a query?
            closest_docs = find_k_closest_docs(v_doc, k, training_set)
            computer_classification[v_doc] = avg_label(closest_docs)  # maybe median instead of mean?
        f1_scores.append(calc_f1(computer_classification, testing_set))
    ```
* Determine best k value (using an objective metric OR shoulder method), then use testing set to check results


### To-Do
* Design project structure
  * Which classes do we want to keep?
  * What classes do we want to create?
* Clean data set ✅
* Expand list of noise/stop words ✅
  * https://github.com/stopwords-iso/stopwords-en/blob/master/stopwords-en.txt
* Implement a stemming function?
* Read data into DocumentCollection
* Final report
    * Goals
    * Design
    * Process
    * Work
    * Results

### Design Architecture

#### Structure
```
project
├─ data/
│   ├── raw/
│   │   ├─ left/
│   │   ├─ center/
│   │   └─ right/
│   └── processed/
│       ├─ left/
│       ├─ center/
│       └─ right/
└─ src/
    ├─ DataProcessing/
    │   └─ DataProcessor.java
    ├─ DocumentClasses/
    │   ├─ CosineDistance.java
    │   ├─ DocumentCollection.java
    │   ├─ <interface> DocumentDistance.java
    │   ├─ DocumentVector.java
    │   ├─ <abstract> TextVector.java
    │   └─ files/
    │       └─ stopwords-en.txt
    └─ main/
        ├─ ArticleClassification.java
        └─ KNearestNeighbors.java
```

### Final Project Design
#### ArticleClassification
`main`
  * Build document collections for each label
  * Separate data into training, validation, and testing document collections.
  * Normalize the sets relative to the training set
    * i.e. `training.normalize(training)` `validation.normalize(training)` `testing.normalize(training)`
  * Tune a value for k
  * Test model with tuned k
  * Print Metrics

`private static ArrayList<ArrayList<Integer>> getSetIDs(DocumentCollection data, double percentVal, double percentTest)`
  * Given the documents in `data`, return an ArrayList of length 2 that corresponds to the validation and testing sets.
    The sub ArrayLists are the IDs of the documents to select. 
    The amount of indices in each list is determined by multiplying `data.getSize()` by the respective `percentVal` and `percentTest`
  * `percentVal/percentTest` represents the percentage of documents to include in this collection

### KNearestNeighbors
* `private final DocumentCollection trainingSet`
* `private final DocumentCollection validationSet`
* `private final DocumentCollection testingSet`
* `private Map<Integer, Map<Integer, TextVector>> trainingDocsByLabel`

`private int tuneK(double threshold)`
 * Choose a starting value of K, run kNN on the documents in the `validationSet` set and compute accuracy. 
   Increase K and repeat until the increase in accuracy is below `threshold`
 * Return the current value of K after stopping

`public double[] test(int k)`
  * For every document in the `testingSet`, run `predict` with `k`
  * return a `double[]` representing the precision, recall, and f1 score
    of the model

`private int predict(TextVector sample, int k)`
  * Return the majority label of the `k` most similar documents to `sample`

`private double[] calcPrecisionAndRecall(Hashmap<Integer, DocumentCollection> computerJudgement)`
* calculate and return the precision and recall of the `computerJudgement` using the macro average formula (imbalanced dataset but all labels equally important)
* P_n = numCorrect / numInCluster
* R_n = numCorrect / totalDocsInCategory
* Total P = (P1 + P2 + P3) / 3
* Total R = (R1 + R2 + R3) / 3

`private double calcF1(double precision, double recall)`
* return (2 * P * R) / (P + R)

#### DocumentCollection Class
* ✅ Modify the constructor to loop through files in a directory, and either:
  * take a `label` argument with which to initialize the `TextVector`, or
  * initialize each `TextVector` with the name of the directory.
* ✅ The constructor should only add non-stopwords to each `TextVector`.
  *  Stop words are handled by TextVector
* ✅ Add a method to combine `DocumentCollection`instances.\
  *Doing this instead of iterating through multiple directories in the constructor 
reduces complexity & makes code more understandable.*
#### TextVector Class ✅
* ✅ Add a label attribute that gets stored based on the name of the parent directory of the file,
i.e. if the file is in the center directory, it's label should be `"center"`.
