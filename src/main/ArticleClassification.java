package main;

import DataProcessing.DataProcessor;
import DocumentClasses.DocumentCollection;
import DocumentClasses.TextVector;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.text.Position.Bias;

public class ArticleClassification {

    private static final String dataDirPath = DataProcessor.processedDataDirPath;

    public static void main(String[] args) {
        // going into main function, we have processed data for each category at ../data/processed

        // DONE BY BLAKE : for each category in processed data build DocumentCollections,
        //       put them into one single folder
        HashMap<String, DocumentCollection> labeledDocCollections = new HashMap<>();
        try (DirectoryStream<Path> dataDirStream = Files.newDirectoryStream(Paths.get(dataDirPath))) {
            for (Path classDir : dataDirStream) {
                labeledDocCollections.put(classDir.getFileName().toString(),
                        //
                        new DocumentCollection(classDir, "document"));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.err);
        }


        // TODO LOGAN: split each DocumentCollection into proportional training/validation/testing sets
            // for each labeled group, pick 70% for training, 10% for valiudation, 20% for testing
            // this will be a function where we cna choose what % we want for training,val,testing

        // TODO GRANT: normalize each subset with respect to the training set.
            // this will require us to normalize training set with its self, and then normalize the
            // validation/testing sets with respect to the tfidf values from the training normalization


        // TODO KASSI: Make actual KNN function refrencing Logan's KNN description

        // Optional: make a kNearestNeighbors class to house these functionalities.
        // TODO: use the validation set to find the best k value.
        //       Measuring "best" here is a design decision we will have to make.

        // TODO: use our selected k-value to evaluate with the testing set.
    }

    /**
     * Represents the similarity-based classification of a text document.
     * @param trainingSet The collection of documents used for training.
     * @param sample The text vector representing the sample document to classify.
     * @param k The number of nearest neighbors to consider.
     * @return The majority label among the k most similar documents.
     */
    private Integer kNN(DocumentCollection trainingSet, TextVector sample, int k) {
        // Priority queue to store documents by their distance to the given sample
        // - Ingeger being the 'document collection key'; Double being the 'document distance'
        // - Acending order (meaning lowest distance comes first)
        PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

        // Calculate the Euclidean distance between the sample and each document in the training set
        for (Map.Entry<Integer, TextVector> entry : trainingSet.getEntrySet()) {
            int key = entry.getKey();
            TextVector document = entry.getValue();
            double distance = euclideanDistance(sample, document);
            pq.offer(new AbstractMap.SimpleEntry<>(key, distance));
        }

        // Get the k closest documents
        Map<Integer, Integer> labelCount = new HashMap<>();
        for (int i = 0; i < k && !pq.isEmpty(); i++) {
            Map.Entry<Integer, Double> entry = pq.poll(); // note: retrieves first element and removes it
            TextVector closestDocument = trainingSet.getDocumentById(entry.getKey());
            Integer label = closestDocument.getLabel();
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        // Finds which label has majority closest
        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * Calculates the Euclidean distance between two text vectors.
     * @param v1 The first text vector.
     * @param v2 The second text vector.
     * @return The Euclidean distance between the two vectors.
     */
    private double euclideanDistance(TextVector v1, TextVector v2) {
        double sum = 0.0;
        Set<String> words = new HashSet<>(v1.getWords());
        words.addAll(v2.getWords());

        for (String word : words) {
            double freq1 = v1.getNormalizedFrequency(word);
            double freq2 = v2.getNormalizedFrequency(word);
            sum += Math.pow(freq1 - freq2, 2);
        }

        return Math.sqrt(sum);
    }

}
