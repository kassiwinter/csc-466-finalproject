package main;

import DocumentClasses.CosineDistance;
import DocumentClasses.DocumentCollection;
import DocumentClasses.TextVector;

import java.util.*;

public class KNearestNeighbors {
    /**
     * The number of closestDocuments to store under the hood. Saves repeat computations.
     */
    private static final int numClosestDocuments = 50;

    private final DocumentCollection trainingSet;
    private final DocumentCollection validationSet;
    private final DocumentCollection testingSet;

    private final Map<Integer, Map<Integer, TextVector>> trainingDocsByLabel;
    /**
     * Stores closest document mappings.
     * Specifically, should map a TextVector to a list of closest training documents,
     *      represented by their IDs and sorted by descending closeness.
     */
    private final Map<TextVector, ArrayList<Integer>> closestTrainingDocs;

    /**
     * Initialize a KNearestNeighbors object.
     * The arguments passed to the constructor are expected to be already normalized.
     * @param trainingSet   Used for classifying documents in the testing set.
     * @param validationSet Used for tuning hyperparameter k.
     * @param testingSet    Used for testing the performance of kNN classification.
     */
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
        this.closestTrainingDocs = new HashMap<>();
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
        // compute precision, recall, f1 score and insert into metrics
        return metrics;
    }

    /**
     * Predicts the class of the given document using the {@code k} nearest neighbors in the training set.
     * @param document The document to classify.
     * @param k The number of neighbors to check.
     * @return The predicted label of the document.
     */
    private int predict(TextVector document, int k) {
        // using dynamic mapping of closest documents, so they don't have to be found every time `predict()` runs
        List<Integer> nearestDocs;
        if (k > numClosestDocuments) {
            System.err.println("k > numClosestDocuments. Expect reduced performance.");
            nearestDocs = document.findNClosestDocuments(k, trainingSet, new CosineDistance());
        } else {
            closestTrainingDocs.putIfAbsent(document,
                    document.findNClosestDocuments(numClosestDocuments, trainingSet, new CosineDistance()));
            nearestDocs = closestTrainingDocs.get(document).subList(0, k);
        }

        Map<Integer, Integer> labelCount = new HashMap<>();
        for (int i = 0; i < k; i++) {
            TextVector givenDoc = trainingSet.getDocumentById(nearestDocs.get(i));
            Integer label = givenDoc.getLabel();
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private double[] calcPrecisionAndRecall(HashMap<Integer, DocumentCollection> computerJudgement) {
        double totalPrecision = 0.0;
        double totalRecall = 0.0;
        HashMap<Integer, Map<Integer, TextVector>> predictionsByLabel = new HashMap<>(); // stores true labels

        // Populate predictionsByLabel
        for (int i = 0; i < testingSet.getSize(); i++) {
            TextVector sample = testingSet.getDocumentById(i);
            int trueLabel = sample.getLabel();

            if (!predictionsByLabel.containsKey(trueLabel)) {        // note: if the true label is not in the map yet
                predictionsByLabel.put(trueLabel, new HashMap<>());
            }

            predictionsByLabel.get(trueLabel).put(i, sample); // add the prediction to the map
        }

        // Calculate precision and recall for each category
        for (int trueLabel : predictionsByLabel.keySet()) {
            Map<Integer, TextVector> predictionsForLabel = predictionsByLabel.get(trueLabel);
            int numCorrect = 0;
            int numInCluster = predictionsForLabel.size();

            // Counts the number of correctly predicted documents
            for (int documentId : predictionsForLabel.keySet()) {
                if (predictionsForLabel.get(documentId).getLabel() == trueLabel) {
                    numCorrect++;
                }
            }

            // Calculates precision and recall for the category
            double precision = (double) numCorrect / computerJudgement.get(trueLabel).getSize();
            double recall = (double) numCorrect / numInCluster;

            // Adds it to total precision and recall
            totalPrecision += precision;
            totalRecall += recall;
        }

        // Calculates the macro average precision and recall
        double macroAvgPrecision = totalPrecision / computerJudgement.size();
        double macroAvgRecall = totalRecall / computerJudgement.size();

        return new double[]{macroAvgPrecision, macroAvgRecall};

    }

    private double calcF1(double precision, double recall) {
        return (2 * precision * recall) / (precision + recall);
    }
}
