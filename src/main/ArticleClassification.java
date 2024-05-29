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
}
