package DocumentClasses;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DocumentVector extends TextVector {

    private HashMap<String, Double> normalizedVector;

    public DocumentVector(String label) {
        super(label);
        this.normalizedVector = new HashMap<>();
    }

    @Override
    public Set<Map.Entry<String, Double>> getNormalizedVectorEntrySet() {
        /* Returns the normalized vector entry set.
         *
         * Key   (String): a string in the vector
         * Value (Double): a normalized frequency (between 0 and 1)
         */
        return normalizedVector.entrySet();
    }

    @Override
    public void normalize(DocumentCollection dc) {
        /* Normalize the frequency of each word in rawVector using TF-IDF */
        for (Map.Entry<String, Integer> entry : getRawVectorEntrySet()) {
            String word = entry.getKey();

            int numDocs = dc.getSize();
            int docFreq = dc.getDocumentFrequency(word);
            if (docFreq == 0) {normalizedVector.put(word, 0.0); continue;} // short circuit
            double idf = Math.log(numDocs/(double)docFreq) / Math.log(2);  // log base a of b == ln(b) / ln(a)

            int docRawFreq = getRawFrequency(word);
            int docMaxRawFreq = getHighestRawFrequency();
            double tf = docRawFreq / (double)docMaxRawFreq;

            double tfIdf = tf * idf;
            normalizedVector.put(word, tfIdf);
        }
    }

    @Override
    public double getNormalizedFrequency(String word) {
        /* Return the normalized frequency of a given word within a document */
        return normalizedVector.get(word);
    }

}
