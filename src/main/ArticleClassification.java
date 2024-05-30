package main;

import DataProcessing.DataProcessor;
import DocumentClasses.DocumentCollection;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ArticleClassification {

    private static final String dataDirPath = DataProcessor.processedDataDirPath;

    public static void main(String[] args) {
        // going into main function, we have processed data for each category at ../data/processed

        // DONE BY BLAKE : for each category in processed data build DocumentCollections,
        //       put them into one single folder
        HashMap<String, DocumentCollection> labeledDocCollections = new HashMap<>();
        try (DirectoryStream<Path> dataDirStream = Files.newDirectoryStream(Paths.get(dataDirPath))) {
            for (Path classDir : dataDirStream) {
                if (classDir.getFileName().toString().equals("left") || classDir.getFileName().toString().equals("center") || classDir.getFileName().toString().equals("right")) {
                    labeledDocCollections.put(classDir.getFileName().toString(),
                            //
                            new DocumentCollection(classDir, "document"));
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.err);
        }


        // DONE LOGAN: split each DocumentCollection into proportional training/validation/testing sets
            // for each labeled group, pick 70% for training, 10% for valiudation, 20% for testing
            // this will be a function where we can choose what % we want for training,val,testing
        DocumentCollection trainingSet = new DocumentCollection();
        DocumentCollection validationSet = new DocumentCollection();
        DocumentCollection testingSet = new DocumentCollection();
        ArrayList<ArrayList<Integer>> splits;
        int totalDocs = labeledDocCollections.get("left").getSize() + labeledDocCollections.get("center").getSize() + labeledDocCollections.get("right").getSize();

        for (DocumentCollection docCollection : labeledDocCollections.values()) {
            splits = getSetIndices(docCollection, totalDocs, .1, .2, (double) docCollection.getSize() / totalDocs);

            for (int i = 0; i < docCollection.getSize(); i++) {
                if (!splits.getFirst().contains(i) && !splits.getLast().contains(i)) {
                    trainingSet.addDocument(docCollection.getDocumentById(i));
                }
            }
            for (int index : splits.getFirst()) {
                validationSet.addDocument(docCollection.getDocumentById(index));
            }
            for (int index : splits.getLast()) {
                testingSet.addDocument(docCollection.getDocumentById(index));
            }
        }

        // TODO GRANT: normalize each subset with respect to the training set.
            // this will require us to normalize training set with its self, and then normalize the
            // validation/testing sets with respect to the tfidf values from the training normalization


        // TODO KASSI: Make actual KNN function referencing Logan's KNN description

        // Optional: make a kNearestNeighbors class to house these functionalities.
        // TODO: use the validation set to find the best k value.
        //       Measuring "best" here is a design decision we will have to make.

        // TODO: use our selected k-value to evaluate with the testing set.
    }

    private static ArrayList<ArrayList<Integer>> getSetIndices(DocumentCollection data, int totalDocs, double percentVal, double percentTest, double ratio) {
        int numValDocs = (int) (totalDocs * percentVal * ratio);
        int numTestDocs = (int) (totalDocs * percentTest * ratio);
        ArrayList<ArrayList<Integer>> setIndices = new ArrayList<>();
        ArrayList<Integer> valIndices = new ArrayList<>();
        ArrayList<Integer> testIndices = new ArrayList<>();
        double f;
        int maxIndex = data.getSize() - 1;
        int index;

        while (valIndices.size() < numValDocs) {
            // following Math.random() suggestion for generating a random number between [0, maxIndex]
            f = Math.random() / Math.nextDown(1.0);
            index = (int) (maxIndex * f);
            if (!valIndices.contains(index)) {
                valIndices.add(index);
            }
        }

        setIndices.add(valIndices);

        while (testIndices.size() < numTestDocs) {
            // following Math.random() suggestion for generating a random number between [0, maxIndex]
            f = Math.random() / Math.nextDown(1.0);
            index = (int) (maxIndex * f);
            if (!testIndices.contains(index) && !valIndices.contains(index)) {
                testIndices.add(index);
            }
        }

        setIndices.add(testIndices);
        return setIndices;
    }
}
