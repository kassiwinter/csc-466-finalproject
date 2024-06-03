package main;

import DataProcessing.DataProcessor;
import DocumentClasses.DocumentCollection;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArticleClassification {

    private static final String dataDirPath = DataProcessor.processedDataDirPath;
    private static final ArrayList<String> labels = new ArrayList<>(List.of(DataProcessor.dataSubDirs));

    private static final String serializedTrainingSetPath = "src/main/files/serializedTrainingSet.ser";
    private static final String serializedTestingSetPath = "src/main/files/serializedTestingSet.ser";
    private static final String serializedValidationSetPath = "src/main/files/serializedValidationSet.ser";

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

        // Attempt to load testing/training/validation sets from serialized objects. (Blake)
        DocumentCollection trainingSet = readDocumentCollection(serializedTrainingSetPath);
        DocumentCollection testingSet = readDocumentCollection(serializedTestingSetPath);
        DocumentCollection validationSet = readDocumentCollection(serializedValidationSetPath);

        // If any set couldn't be loaded, new sets must be constructed.
        if (trainingSet == null || testingSet == null || validationSet == null) {
            System.out.println("Failed to read serialized training, testing, or validation set.\n" +
                    "Generating new DocumentCollections...");
            // DONE LOGAN: split each DocumentCollection into proportional training/validation/testing sets
            // for each labeled group, pick 70% for training, 10% for validation, 20% for testing
            // this will be a function where we can choose what % we want for training,val,testing
            trainingSet = new DocumentCollection();
            validationSet = new DocumentCollection();
            testingSet = new DocumentCollection();
            ArrayList<ArrayList<Integer>> splits;

            for (DocumentCollection docCollection : labeledDocCollections.values()) {
                splits = getSetIDs(docCollection, .1, .2);

                for (int i = 1; i <= docCollection.getSize(); i++) {
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
            System.out.println("Normalizing training set...");
            trainingSet.normalize(trainingSet);
            System.out.println("Normalizing testing set...");
            validationSet.normalize(trainingSet);
            System.out.println("Normalizing validation set...");
            testingSet.normalize(trainingSet);

            //Serialize generated sets (Blake)
            System.out.println("Serializing normalized training set...");
            writeDocumentCollection(trainingSet, serializedTrainingSetPath);
            System.out.println("Serializing normalized testing set...");
            writeDocumentCollection(testingSet, serializedTestingSetPath);
            System.out.println("Serializing normalized validation set...");
            writeDocumentCollection(validationSet, serializedValidationSetPath);
        }

        double threshold = 0.1;
        KNearestNeighbors knn = new KNearestNeighbors(trainingSet, validationSet, testingSet);
        System.out.println("Tuning k...");
        int k = knn.tuneK(threshold);
        System.out.println("Testing k = " + k + "...");
        double[] metrics = knn.test(k);
        System.out.println("Precision: " + metrics[0]);
        System.out.println("Recall: " + metrics[1]);
        System.out.println("F1: " + metrics[2]);


        // TODO GRANT: normalize each subset with respect to the training set.
        // this will require us to normalize training set with its self, and then normalize the
        // validation/testing sets with respect to the tfidf values from the training normalization


        // TODO KASSI: Make actual KNN function referencing Logan's KNN description

        // TODO: use the validation set to find the best k value.
        //       Measuring "best" here is a design decision we will have to make.

        // TODO: use our selected k-value to evaluate with the testing set.
    }

    /*
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
        int maxIndex = data.getSize();
        int index;

        while (valIDs.size() < numValDocs) {
            // following Math.random() suggestion for generating a random number between [0, maxIndex]
            f = Math.random() / Math.nextDown(1.0);
            index = (int) (1 * (1.0 - f) + maxIndex * f);
            if (!valIDs.contains(index)) {
                valIDs.add(index);
            }
        }

        setIndices.add(valIDs);

        while (testIDs.size() < numTestDocs) {
            // following Math.random() suggestion for generating a random number between [0, maxIndex]
            f = Math.random() / Math.nextDown(1.0);
            index = (int) (1 * (1.0 - f) + maxIndex * f);
            if (!testIDs.contains(index) && !valIDs.contains(index)) {
                testIDs.add(index);
            }
        }

        setIndices.add(testIDs);
        return setIndices;
    }
    
    /**
     * Attempts to read a DocumentCollection from the given path.
     *
     * @param path a string representation of a path.
     * @return the DocumentCollection if successful, null otherwise.
     */
    private static DocumentCollection readDocumentCollection(String path) {
        DocumentCollection collection = null;
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(path))) {
            collection = (DocumentCollection) is.readObject();
            System.out.println("Loaded DocumentCollection from path " + path);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
            System.out.println("Couldn't load DocumentCollection from path " + path);
            e.printStackTrace(System.err);
        }
        return collection;
    }

    /**
     * Attempts to write a DocumentCollection to a serialized object.
     *
     * @param dc   The object to serialize.
     * @param path The destination to write to.
     */
    private static void writeDocumentCollection(DocumentCollection dc, String path) {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path))) {
            os.writeObject(dc);
        } catch (Exception e) {
            System.err.println("Serializing DocumentCollection to path " + path + " failed.");
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
