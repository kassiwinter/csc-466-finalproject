package main;

import DocumentClasses.CosineDistance;
import DocumentClasses.DocumentCollection;
import DocumentClasses.DocumentVector;
import DocumentClasses.TextVector;
import jdk.incubator.vector.VectorOperators;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KNearestNeighbors {
    private final DocumentCollection trainingSet;
    private final DocumentCollection validationSet;
    private final DocumentCollection testingSet;

    private Map<Integer, Map<Integer, TextVector>> trainingDocsByLabel;

    public KNearestNeighbors(DocumentCollection trainingSet,
                             DocumentCollection validationSet,
                             DocumentCollection testingSet) {
        this.trainingSet = trainingSet;
        this.validationSet = validationSet;
        this.testingSet = testingSet;
        Set<Integer> uniqueLabels = this.testingSet.uniqueLabels();
        this.trainingDocsByLabel = new HashMap<>();
        for (Integer label : uniqueLabels) {
            this.trainingDocsByLabel.put(label, this.trainingSet.docsWithLabel(label));
        }
    }

    /**
     * Uses the validation set to find the optimal value of k.
     * <p>
     *     Optimality is determined by diminishing f1 score improvement for a given increment of k. When the
     *     improvement is lower than the specified threshold, the previous value of k is considered optimal.
     * </p>
     * @param threshold The minimum improvement in f1 score required to consider an increase in k worthwhile.
     * @return The value of k determined to be optimal.
     */
    public int tuneK(double threshold) {
        // TODO: Implement tuneK
        return 0;
    }

    /**
     * Tests the performance of kNN using the given value of k.
     * <p>
     *     For each document in the testing set, finds the nearest k documents in the training set. The document is
     *     classified by finding the average value of the nearest neighbors' classifications.
     * </p>
     * @param k the number of nearest neighbors to check.
     * @return an array of doubles representing (in order): precision, recall, f1 score
     */
    public double[] test(int k) {
        // TODO: Implement test
        double[] metrics = {0.0, 0.0, 0.0};
        return metrics;
    }

    /**
     * Predicts the class of the given document using the {@code k} nearest neighbors in the training set.
     * @param document The document to classify.
     * @param k The number of neighbors to check.
     * @return The predicted label of the document.
     */
    private int predict(DocumentVector document, int k) {
        ArrayList<Integer> nearestDocs = document.findNClosestDocuments(k, trainingSet, new CosineDistance());
        return -100;
    }

    private double calcPrecision() {
        // TODO: Implement calcPrecision
        // make predictions for each doc
        HashMap<Integer, Map<Integer, TextVector>> predictionsByLabel = new HashMap<>();
        return 0.0;
    }

    private double calcRecall() {
        // TODO: Implement calcRecall
        return 0.0;
    }

    private double calcF1() {
        // TODO: Implement calcF1
        return 0.0;
    }
}