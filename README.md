# Article Classification using k-Nearest Neighbors
#### Group: Grant Baer, Logan Baker, Blake Silton, and Kassi Winter

### Overall Idea
* Split docs into training, testing, validation sets (80-10-10)
  * We can encode the labels as 0, 1, 2; 
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
#### main 
* Build a document collection to be used later when evaluating
* Iterate through validation directory, start with k = 1, call KNN on each file where text in file is treated as a query vector, compute the accuracy and store it.
If the increase in accuracy is less than some epsilon, stop.
* Run KNN on the files in the testing set with the ideal value of K, compute the accuracy and evaluate.

### DocumentCollection Class
* Modify the constructor to loop through files in a directory, and either:
  * take a `label` argument with which to initialize the `TextVector`, or
  * initialize each `TextVector` with the name of the directory.
* The constructor should only add non-stopwords to each `TextVector`.
* Add a method to combine `DocumentCollection`instances.\
  *Doing this instead of iterating through multiple directories in the constructor 
reduces complexity & makes code more understandable.*

### TextVector Class
#### Label Attribute
* Add a label attribute that gets stored based on the name of the parent directory of the file,
i.e. if the file is in the center directory, it's label should be `"center"`.