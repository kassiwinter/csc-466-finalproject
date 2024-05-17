package DocumentClasses;

public interface DocumentDistance {

    // Will return the distance between the query and document
    double findDistance(TextVector query, TextVector document, DocumentCollection documents);
}
