package main;

import DocumentClasses.CosineDistance;
import DocumentClasses.DocumentCollection;
import DocumentClasses.TextVector;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.DoubleStream;


public class KNearestNeighbors {
    public static final int PRECISION = 0;
    public static final int RECALL = 1;
    public static final int F1 = 2;

    /**
     * The number of closestDocuments to store under the hood. Saves repeat computations.
     */
    private static final int numClosestDocuments = 3000;

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
    public int tuneK(double threshold, int maxK) {
        HashMap<Integer, DocumentCollection> computerJudgement = new HashMap<>();
        int initialK = 50;
        int bestK = initialK;
        double prevF1 = 0;

        double[] prevF1Diffs = {threshold, threshold, threshold};

        for (int k = initialK; k <= maxK; k+=50) {
            System.out.printf("[%s] Trying k = %d\n",
                    new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new java.util.Date()),
                    k);
            double[] metrics = test(k, validationSet);
            double currentF1 = metrics[F1];
            System.out.printf("   precision = %f\n   recall = %f\n   F1 = %f\n",
                    metrics[PRECISION], metrics[RECALL], metrics[F1]);
            double runningDiffAvg = DoubleStream.of(prevF1Diffs).sum() / (double) prevF1Diffs.length;
            System.out.printf("Last three F1 diffs: %s (avg: %f)\ncurrent - prev = %f\n",
                    Arrays.toString(prevF1Diffs),
                    runningDiffAvg,
                    currentF1 - prevF1);
            if (runningDiffAvg >= threshold) {
                prevF1Diffs[k % prevF1Diffs.length] = currentF1 - prevF1;
                bestK = k;
                prevF1 = currentF1;
            } else {
                break;
            }
        }
        // If threshold is
//        if (DoubleStream.of(prevF1Diffs).anyMatch(d -> d == 0)) {
//            int maxAt = 0;
//            for (int i = 0; i < prevF1Diffs.length; i++) {
//                maxAt = prevF1Diffs[i] > prevF1Diffs[maxAt] ? i : maxAt;
//            }
//            return initialK * maxAt;
//        }
        return bestK;
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
        HashMap<Integer, DocumentCollection> computerJudgement = new HashMap<>();
        for (TextVector document : testingSet.getDocuments()) {
            int predictedLabel = predict(document, k);
            computerJudgement.putIfAbsent(predictedLabel, new DocumentCollection());
            computerJudgement.get(predictedLabel).addDocument(document);
        }
//        double[] metrics = calcPrecisionAndRecall(computerJudgement);
//        double f1 = calcF1(metrics[0], metrics[1]);
        return calcMetrics(computerJudgement);
    }

    public double[] test(int k, DocumentCollection dataSet) {
        HashMap<Integer, DocumentCollection> computerJudgement = new HashMap<>();
        for (int label : dataSet.uniqueLabels()) {
            computerJudgement.put(label, new DocumentCollection());
        }
        for (TextVector document : dataSet.getDocuments()) {
            int predictedLabel = predict(document, k);
            computerJudgement.get(predictedLabel).addDocument(document);
        }
        return calcMetrics(computerJudgement);
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
            ArrayList<Integer> closestDocs =
                    document.findNClosestDocuments(numClosestDocuments, trainingSet, new CosineDistance());
            closestTrainingDocs.putIfAbsent(document, closestDocs);
            nearestDocs = closestTrainingDocs.get(document).subList(0, k);
        }

        // Since the labels have encoded values, we can just have a running total and then divide by k for the average
        int labelTotal = 0;
        for (Integer id : nearestDocs) {
            labelTotal += trainingSet.getDocumentById(id).getLabel();
        }
        int prediction = (int) Math.round(labelTotal / (double)k);
        // return -1 if (-inf, -0.5),
        //         0 if [-0.5,  0.5),
        //         1 if [ 0.5,  inf)
        return (prediction < -.5) ?
                -1 :
                (prediction < .5 ? 0 : 1);
    }



    private double[] calcMetrics(HashMap<Integer, DocumentCollection> computerJudgement) {
        double[] precisionRecall = calcPrecisionAndRecall(computerJudgement);
        return new double[] {
                precisionRecall[PRECISION],
                precisionRecall[RECALL],
                calcF1(precisionRecall[PRECISION], precisionRecall[RECALL])
        };
    }

    private double[] calcPrecisionAndRecall(HashMap<Integer, DocumentCollection> computerJudgement) {
        double totalPrecision = 0.0;
        double totalRecall = 0.0;
        HashMap<Integer, Integer> humanJudgment = new HashMap<>();

         // Loop #1: Get the number of actual given labels in each category
         for (int label : computerJudgement.keySet()) {
            DocumentCollection predictedLabelCollection = computerJudgement.get(label);
            Collection<TextVector> predictions = predictedLabelCollection.getDocuments();  // all documents for labels

            for (TextVector doc : predictions) { // for every doc computer predicted for given label
                if (!humanJudgment.containsKey(doc.getLabel())) {
                    humanJudgment.put(doc.getLabel(), 0);
                }
                int newLabelCount = humanJudgment.get(doc.getLabel()) + 1;
                humanJudgment.put(doc.getLabel(), newLabelCount);
            }
        }   

        // Loop # 2: Get the number of correct label predictions (assigned vs accurate)
        for (int label : computerJudgement.keySet()) {                                      // go through each label (1, 0, -1) / bias category
            DocumentCollection predictedLabelCollection = computerJudgement.get(label);     
            Collection<TextVector> predictions = predictedLabelCollection.getDocuments();   // access all docs predicted under that category

            int numCorrect = 0;
            int numInCluster = predictions.size();

            for (TextVector doc : predictions) { // go through each of the predicted docs
                if (doc.getLabel() == label) {  // see if their actual label matches their predicted label
                    numCorrect++;
                }
            }

            // Calculates precision and recall for the category
            double precision = (double) numCorrect / numInCluster;
            double recall = (double) numCorrect / humanJudgment.get(label);

            // Adds it to total precision and recall
            totalPrecision += precision;
            totalRecall += recall;

        }

        // Calculates the macro average precision and recall
        double macroAvgPrecision = totalPrecision / computerJudgement.size();
        double macroAvgRecall = totalRecall / computerJudgement.size();
        double[] res = new double[2];
        res[PRECISION] = macroAvgPrecision;
        res[RECALL] = macroAvgRecall;
        return res;
    }


    private double calcF1(double precision, double recall) {
        return (2 * precision * recall) / (precision + recall);
    }
}



