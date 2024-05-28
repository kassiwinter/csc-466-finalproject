package DocumentClasses;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class TextVector implements Serializable {

    /**
     * Stores the frequency for each non-noise word
     */
    private HashMap<String, Integer> rawVector;
    private Bias label;

    public TextVector(String category) {
        this.rawVector = new HashMap<>();
        switch (category) {
            case "left":
                this.label = Bias.LEFT;
            case "center":
                this.label = Bias.CENTER;
            case "right":
                this.label = Bias.RIGHT;
            case null, default:
                throw new IllegalArgumentException("Invalid article category: " + category);
        }
    }

    /**
     * Generates a TextVector from the file at the given path, excluding noise words in the provided HashSet.
     * Overwrites any existing data in the TextVector.
     * @param filepath a Path pointing to the file containing the document words.
     * @param noiseWords a HashSet of noiseWords to exclude from TextVector creation.
     * @return A TextVector based on the file at the path provided.
     */
    public TextVector from(Path filepath, HashSet<String> noiseWords) {
        this.rawVector = new HashMap<>();
        try {
            String text = Files.readString(filepath);
            for (String word : text.split("\\s+")) {
                if (!noiseWords.contains(word) && word.length() > 1) {
                    this.add(word);
                }
            }
        } catch (IOException e) {
            System.out.println("An exception occurred while trying to read file at " + filepath);
            System.err.println(e.getMessage());
        }
        return this;
    }

    public abstract Set<Map.Entry<String, Double>> getNormalizedVectorEntrySet();
    //returns the normalized frequency for each word

    public abstract void normalize(DocumentCollection dc);
    //will normalize the frequency of each word using the TF-IDF formula

    public abstract double getNormalizedFrequency(String word);
    //will return the normalized frequency of the word

    /**
     * Calls the getNormalizedFrequency method to get the normalized frequencies.
     * @return The square root of the sum of the squares of the frequencies.
     */
    public double getL2Norm() {
        double normFreqSquareSum = 0;
        for (Map.Entry<String, Double> entry : getNormalizedVectorEntrySet())
            normFreqSquareSum += Math.pow(entry.getValue(), 2);
        return Math.sqrt(normFreqSquareSum);
    }

    /**
     * Finds the closest documents to this TextVector in {@code documents}.
     * Calls the method findDistance on the distanceAlg variable multiple times.
     *
     * @param   documents       A collection of documents to compare to this TextVector.
     * @param   distanceAlg     The algorithm to use in calculating document distance.
     * @return  The 20 closest documents as an {@code ArrayList<Integer>}.
     */
    public ArrayList<Integer> findClosestDocuments(DocumentCollection documents, DocumentDistance distanceAlg) {
        // Calls the method findDistance on the distanceAlg variable multiple times.
        // Returns the 20 closest documents as an ArrayList<Integer>.
        Map<Double, Integer> distanceToIdMap = new HashMap<>();
        for (Map.Entry<Integer, TextVector> entry : documents.getEntrySet()) { //perhaps get self.entryset
            int docId = entry.getKey();
            TextVector document = entry.getValue();
            double distance = 0;
            if (document.getTotalWordCount() != 0) {
                distance = distanceAlg.findDistance(this, document, documents);
            }
            distanceToIdMap.put(distance, docId);
        }
        ArrayList<Integer> docIds = new ArrayList<>();
        distanceToIdMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .limit(20)
                .forEachOrdered(e -> docIds.add(e.getValue()));
        return docIds;
    }

    /**
     * @return  a mapping from each word to its frequency
     */
    public Set<Map.Entry<String, Integer>> getRawVectorEntrySet() {
        return rawVector.entrySet();
    }

    /**
     * @param   word    The word to add. Either creates an entry or increments the frequency by 1.
     */
    public void add(String word) {
        /* Adds a word to the rawVector. If word is not new, frequency is incremented by 1. */
        Integer val = rawVector.get(word);
        if (val == null)
            rawVector.put(word, 1);
        else
            rawVector.replace(word, val + 1);
    }

    /**
     * @param   word    the word of interest
     * @return  a Boolean representing whether the word is in the rawVector
     */
    public Boolean contains(String word) {
        /* Returns true if the word is in the rawVector, false otherwise */
        return rawVector.containsKey(word);
    }

    /**
     * @param   word  the word of interest
     * @return  the frequency of the given word
     */
    public int getRawFrequency(String word) {
        if (contains(word))
            return rawVector.get(word);
        return 0;
    }

    /**
     * @return  the total number of non-noise words that are stored for the document
     *          (i.e. if frequency of some word is 2, then count the word twice in the sum)
     */
    public int getTotalWordCount() {
        int sum = 0;
        for (Integer freq : rawVector.values())
            sum += freq;
        return sum;
    }

    /**
     * @return the number of distinct words stored in the TextVector
     */
    public int getDistinctWordCount() {
        return rawVector.size();
    }

    /**
     * @return the highest word frequency, or -1 on error
     */
    public int getHighestRawFrequency() {
        Optional<Integer> maxOpt = rawVector.values().stream().max(Integer::compareTo);
        return maxOpt.orElse(-1);
    }

    /**
     * @return the word with the highest frequency
     */
    public String getMostFrequentWord() {
        for (Map.Entry<String, Integer> entry : getRawVectorEntrySet())
            if (entry.getValue() == getHighestRawFrequency())
                return entry.getKey();
        return null;
    }

    /**
     * Enum for encoding values of each bias.
     */
    private enum Bias {
        LEFT(-1),
        CENTER(0),
        RIGHT(1);

        public final int value;

        Bias(int value) {
            this.value = value;
        }
    }
}
