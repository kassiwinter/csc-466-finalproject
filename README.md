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
├─ data
│   ├── raw
│   │   ├─ left/
│   │   ├─ center/
│   │   └─ right/
│   └── processed
│       ├─ left/
│       ├─ center/
│       └─ right/
└─ src
    ├─ DataProcessing
    │   └─ DataProcessor.java
    ├─ DocumentClasses
    │   ├─ CosineDistance.java
    │   ├─ DocumentCollection.java
    │   ├─ <interface> DocumentDistance.java
    │   ├─ DocumentVector.java
    │   ├─ QueryVector.java
    │   └─ <abstract> TextVector.java
    └─ main
        └─ ArticleClassification.java
```

### Final Project Design
#### Thoughts about classes to keep
* Probably don't need separate `QueryVector`, `DocumentVector` classes
since we're just treating each article as a document.
* Probably don't need a `DocumentDistance` interface since we'll likely just use cosine distance, but we can leave the
infrastructure there in case we want to try different distance calculation methods.
* In that vein, probably don't need `OkapiDistance`.
#### ArticleClassification
`main`
  * Build document collections for each label
  * Separate data into training, validation, and testing document collections.
  * Normalize the sets relative to the training set
    * i.e. `training.normalize(training)` `validation.normalize(training)` `testing.normalize(training)`
  * Iterate through validation directory, start with k = 1, call KNN on each file, compute the accuracy and store it.
  If the increase in accuracy is less than some epsilon, stop.
  * Run kNN on the files in the testing set with the ideal value of K, compute the accuracy and evaluate.

`private ArrayList<ArrayList<Integer>> getSetIndices(DocumentCollection data, int totalDocs, int percentVal, int percentTest, int ratio)`
  * Given the documents in `data`, return an ArrayList of length 2 that corresponds to the validation and testing sets.
    The sub ArrayLists are the indices of the documents to select. 
    The amount of indices in each list is determined by multiplying `totalDocs` by the respective `percentage` and `ratio`
  * `ratio` represents the ratio of documents in this collection relative to the total number of documents
  * `percentage` represents the percentage of documents to include in this collection

`private int tuneK(DocumentCollection training, DocumentCollection validation)`
 * Choose a starting value of K, run kNN on the documents in the `validation` set and compute accuracy. 
   Increase K and repeat until the increase in accuracy is below a chosen threshold
 * Return the current value of K after stopping

`private Bias kNN(DocumentCollection trainingSet, TextVector sample, int k)`
  * For every document in the `trainingSet`, compute the similarity to the `sample`
  * Return the majority label of the `k` most similar documents

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
