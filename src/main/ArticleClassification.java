package main;

import DataProcessing.DataProcessor;

public class ArticleClassification {

    private static final String dataDirPath = DataProcessor.processedDataDirPath;

    public static void main(String[] args) {
        // TODO: read documents into labeled DocumentCollections

        // TODO: split each DocumentCollection into proportional training/validation/testing sets
        // These are split into two steps so we can guarantee the amount of left/center/right samples is nearly the same
        // TODO: combine the respective subsets into overall training/validation/testing sets.
        //       We can assume that each of these sets will be a DocumentCollection.

        // TODO: normalize each subset w.r.t. the training set.

        // TODO: use the validation set to find the best k value.
        //       Measuring "best" here is a design decision we will have to make.

        // TODO: use our selected k-value to evaluate with the testing set.
    }
}
