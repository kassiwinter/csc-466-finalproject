package main;

import DataProcessing.DataProcessor;
import DocumentClasses.DocumentCollection;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class ArticleClassification {

    private static final String dataDirPath = DataProcessor.processedDataDirPath;

    public static void main(String[] args) {
        HashMap<String, DocumentCollection> labeledDocCollections = new HashMap<>();
        try (DirectoryStream<Path> dataDirStream = Files.newDirectoryStream(Paths.get(dataDirPath))) {
            for (Path classDir : dataDirStream) {
                labeledDocCollections.put(classDir.getFileName().toString(),
                        new DocumentCollection(classDir, "document"));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.err);
        }

        // TODO: split each DocumentCollection into proportional training/validation/testing sets
        // These are split into two steps so we can guarantee the amount of left/center/right samples is nearly the same
        // TODO: combine the respective subsets into overall training/validation/testing sets.
        //       We can assume that each of these sets will be a DocumentCollection.

        // TODO: normalize each subset w.r.t. the training set.

        // Optional: make a kNearestNeighbors class to house these functionalities.
        // TODO: use the validation set to find the best k value.
        //       Measuring "best" here is a design decision we will have to make.

        // TODO: use our selected k-value to evaluate with the testing set.
    }
}
