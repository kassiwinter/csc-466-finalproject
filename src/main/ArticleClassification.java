package main;

import DataProcessing.DataProcessor;
import DocumentClasses.DocumentCollection;
import DocumentClasses.TextVector;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleClassification {

    private static final String dataDirPath = DataProcessor.processedDataDirPath;
    private static final ArrayList<String> labels = new ArrayList<>(List.of(DataProcessor.dataSubDirs));

    public static void main(String[] args) {
        // going into main function, we have processed data for each category at ../data/processed

        // DONE BY BLAKE : for each category in processed data build DocumentCollections,
        //       put them into one single folder
        HashMap<String, DocumentCollection> labeledDocCollections = new HashMap<>();
        try (DirectoryStream<Path> dataDirStream = Files.newDirectoryStream(Paths.get(dataDirPath),
                /* stream filter: */ path -> labels.contains(path.getFileName().toString()))) {
            for (Path classDir : dataDirStream) {
                labeledDocCollections.put(classDir.getFileName().toString(),
                        new DocumentCollection(classDir, "document"));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.err);
        }


        // DONE LOGAN: split each DocumentCollection into proportional training/validation/testing sets
        // for each labeled group, pick 70% for training, 10% for validation, 20% for testing
        // this will be a function where we can choose what % we want for training,val,testing
        DocumentCollection trainingSet = new DocumentCollection();
        DocumentCollection validationSet = new DocumentCollection();
        DocumentCollection testingSet = new DocumentCollection();
        ArrayList<ArrayList<Integer>> splits;

        // Combine our docs to ensure all docs have a unique ID
        DocumentCollection allDocs = DocumentCollection.combineCollections(labeledDocCollections.values());
        HashMap<Integer, DocumentCollection> docsByLabelValue = new HashMap<>();
        for (int label : allDocs.uniqueLabels()) {
            // By initializing new DocumentCollections using docsWithLabel(),
            // we ensure that the unique document IDs are preserved.
            docsByLabelValue.put(label, new DocumentCollection(allDocs.docsWithLabel(label)));
        }

        for (DocumentCollection docCollection : docsByLabelValue.values()) {
            splits = getSetIDs(docCollection, .1, .2);

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

    /**
     * Given a document collection representing a category of our data, i.e. left labeled articles,
     * return two ArrayLists of IDs in the document collection representing documents to be included in
     * the validation set and testing set respectively
     * @param data the collection we want IDs from
     * @param percentVal the percentage of data we want to be in the validation set
     * @param percentTest the percentage of data we want to be in the testing set
     * @return an ArrayList containing two ArrayLists of IDs where getFirst() returns the validation IDs
     * and getLast returns the testing IDs
     */
    private static ArrayList<ArrayList<Integer>> getSetIDs(DocumentCollection data, double percentVal, double percentTest) {
        int numValDocs = (int) (data.getSize() * percentVal);
        int numTestDocs = (int) (data.getSize() * percentTest);
        ArrayList<ArrayList<Integer>> setIndices = new ArrayList<>(2);
        ArrayList<Integer> valIDs = new ArrayList<>();
        ArrayList<Integer> testIDs = new ArrayList<>();
        double f;
        int maxIndex = data.getSize() - 1;
        int index;

        while (valIDs.size() < numValDocs) {
            // following Math.random() suggestion for generating a random number between [0, maxIndex]
            f = Math.random() / Math.nextDown(1.0);
            index = (int) (maxIndex * f);
            if (!valIDs.contains(index)) {
                valIDs.add(index);
            }
        }

        setIndices.add(valIDs);

        while (testIDs.size() < numTestDocs) {
            // following Math.random() suggestion for generating a random number between [0, maxIndex]
            f = Math.random() / Math.nextDown(1.0);
            index = (int) (maxIndex * f);
            if (!testIDs.contains(index) && !valIDs.contains(index)) {
                testIDs.add(index);
            }
        }

        setIndices.add(testIDs);
        return setIndices;
    }
}