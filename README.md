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
* Clean data set
* Expand list of noise/stop words
  * https://github.com/stopwords-iso/stopwords-en/blob/master/stopwords-en.txt
* Implement a stemming function?
* Final report
    * Goals
    * Design
    * Process
    * Work
    * Results



Grant's water quality dataset:
- https://www.kaggle.com/datasets/adityakadiwal/water-potability
