package DocumentClasses;

import java.util.Map;

public class CosineDistance implements DocumentDistance {
    @Override
    public double findDistance(TextVector query, TextVector document, DocumentCollection documents) {
        // If query or document is empty, just return 0
        if (query.getRawVectorEntrySet().isEmpty() || document.getRawVectorEntrySet().isEmpty()) {
            return 0;
        }

        double dotProduct = 0.0;
        for (Map.Entry<String, Double> entry : document.getNormalizedVectorEntrySet()) {
            double queryFreq = 0;
            if (query.contains(entry.getKey())) {
                queryFreq = query.getNormalizedFrequency(entry.getKey());
            }
            double docFreq = entry.getValue();
            dotProduct += queryFreq * docFreq;
        }
        return dotProduct / (query.getL2Norm() * document.getL2Norm());
    }
}
