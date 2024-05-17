package DocumentClasses;

import java.util.Map;

public class OkapiDistance implements DocumentDistance {

    public final double k1;
    public final double b;
    public final int k2;
    public OkapiDistance() {
        // Default values prescribed in lab3 assignment
        this.k1 = 1.2;
        this.b = 0.75;
        this.k2 = 100;
    }

    /* NOTE:
     * - this method uses the RAW frequencies, not normalized
     * - 2nd part of Okapi BM25 uses the raw frequency of the matching word in the document, while
     *   the 3rd part uses the raw frequency of the matching word in the query
     */

    @Override
    public double findDistance(TextVector query, TextVector document, DocumentCollection documents) {

        if (query.getRawVectorEntrySet().isEmpty() || document.getRawVectorEntrySet().isEmpty()) {
            return 0;
        }

        // Use doubles across the board for math precision
        double totalDocs = documents.getSize();
        double avgDocLength = documents.getAverageDocumentLength();

        double sum = 0.0;
        for (Map.Entry<String, Integer> entry : query.getRawVectorEntrySet()) {
            String queryTerm = entry.getKey();
            double df = documents.getDocumentFrequency(queryTerm);
            double freqInDoc = document.getRawFrequency(queryTerm);
            double freqInQuery = (double) entry.getValue();
            double docLength = document.getTotalWordCount();

            double firstTerm = Math.log((totalDocs - df + 0.5) / (df + 0.5));
            double secondTerm = ((k1 + 1) * freqInDoc) / ((k1 * (1 - b + (b * docLength / avgDocLength))) + freqInDoc);
            double thirdTerm = ((k2 + 1) * freqInQuery) / (k2 + freqInQuery);

            double okapiForThisTerm = firstTerm * secondTerm * thirdTerm;
            sum += okapiForThisTerm;
        }
        return sum;
    }
}
