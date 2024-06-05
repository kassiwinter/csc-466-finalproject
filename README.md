# Article Classification using k-Nearest Neighbors
#### Group: Grant Baer, Logan Baker, Blake Silton, and Kassi Winter

### Documents
* [Report](https://docs.google.com/document/d/1bQ4X9KE49DurhGRAjx2A1_UrcRed19MIVPOoSvn_NIw/edit?usp=sharing)
* [Work Log](https://docs.google.com/spreadsheets/d/19JqmmsZUCv2Bt1Z24hmacCzEmfV-THAkMgWOyRhToXQ/edit?usp=sharing)
* [Presentation](https://docs.google.com/presentation/d/1TYUZTP7W0tylIgqdXUCdTjfokhOWO92lUj05ZxDL8vw/edit?usp=sharing)

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
