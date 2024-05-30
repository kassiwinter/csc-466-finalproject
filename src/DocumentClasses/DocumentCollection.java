package DocumentClasses;

import org.w3c.dom.Text;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DocumentCollection implements Serializable {

    private static final String stopWordsPath = "src/DocumentClasses/files/stopwords-en.txt";
    public static HashSet<String> noiseWordSet = readWordList(stopWordsPath);
    /**
     * Maps each document ID to a {@link TextVector}.
     */
    private final HashMap<Integer, TextVector> documents;
    private int maxId;

    public DocumentCollection() {
        documents = new HashMap<>();
        maxId = 0;
    }

    /**
     * Reads the folder specified as input and uses the files in the folder to populate the documents variable.
     * @param   dirPath     the path to the folder to open
     * @param   docType     if not "document", treat as query
     */
    public DocumentCollection(Path dirPath, String docType) {
        documents = new HashMap<>();
        maxId = 0;
        String label = dirPath.getFileName().toString();  // the name of the directory, e.g. "center"
        try (DirectoryStream<Path> dataDirStream = Files.newDirectoryStream(dirPath)) {  // try-with-resources
            // dataDirStream is a stream containing the path to each file in the passed directory.
            for (Path path : dataDirStream) {
                // for each filepath in the stream, make a new TextVector.
                TextVector textVector = new DocumentVector(label).from(path, noiseWordSet);
                documents.put(++maxId, textVector);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public void normalize(DocumentCollection dc) {
        // Calls the normalize(dc) function on each document in the collection
        for (TextVector document : getDocuments()) {
            document.normalize(dc);
        }
    }

    // TODO: add a function to split the document collection into subsets of specified sizes [0, 1]

    /**
     * @param   id  the document ID to get
     * @return  the document with the associated ID
     */
    public TextVector getDocumentById(int id) {
        /* Returns the TextVector for the document with the ID that is given. */
        return documents.get(id);
    }

    /**
     * @return A Set of all unique label values of TextVectors in this DocumentCollection.
     */
    public Set<Integer> uniqueLabels() {
        return this.getDocuments().stream()
                .map(TextVector::getLabel)
                .collect(Collectors.toSet());
    }

    /**
     * @return The documents with the specified label.
     */
    public Map<Integer, TextVector> docsWithLabel(int label) {
        return this.getEntrySet().stream()
                .filter(e -> e.getValue().getLabel() == label)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * @return the average length of a document, not counting noise words
     */
    public double getAverageDocumentLength() {
        int sum = 0;
        for (TextVector document : getDocuments()) {
            sum += document.getTotalWordCount();
        }
        return sum / (double) getSize();
    }

    /**
     * @return the number of documents in the DocumentCollection
     */
    public int getSize() {
        return documents.size();
    }

    /**
     * @return all documents stored in the DocumentCollection
     */
    public Collection<TextVector> getDocuments() {
        return documents.values();
    }

    /**
     * @return a mapping of document ID to Text Vector
     */
    public Set<Map.Entry<Integer, TextVector>> getEntrySet() {
        return documents.entrySet();
    }

    /**
     * @param   word    the word to search for
     * @return  the number of documents that contain the query word
     */
    public int getDocumentFrequency(String word) {
        int docFreq = 0;
        for (TextVector document : getDocuments()) {
            if (document.contains(word)) {
                docFreq++;
            }
        }
        return docFreq;
    }

    /**
     * @param   word    a word to check
     * @return  a Boolean describing if the word is a noise word
     */
    private Boolean isNoiseWord(String word) {
        return noiseWordSet.contains(word);
    }

    /**
     * Adds a given vector to the document.
     * @param doc The TextVector to add.
     * @return False if an existing document was overwritten (key collision). True otherwise.
     */
    public boolean addDocument(TextVector doc) {
        return (documents.put(++maxId, doc) == null);
    }

    /**
     * Combine an arbitrary number of DocumentCollections in an Iterable. Document IDs are not guaranteed to be the
     * same in the returned DocumentCollection as they were, but they ARE guaranteed to be unique.
     * @param collections An Iterable of DocumentCollections.
     * @return A single DocumentCollection containing all documents contained within the passed DocumentCollections.
     */
    public static DocumentCollection combineCollections(Iterable<DocumentCollection> collections) {
        DocumentCollection ret = new DocumentCollection();
        for (DocumentCollection collection : collections) {
            for (TextVector doc : collection.getDocuments()) {
                if (!ret.addDocument(doc)) {
                    System.err.println("Overwrote a document ID.");
                }
            }
        }
        return ret;
    }

    private static HashSet<String> readWordList(String filepath) {
        HashSet<String> wordList = new HashSet<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filepath));
            String allText = String.join(" ", lines);
            wordList = Arrays.stream(allText.split("\\s+")).collect(Collectors.toCollection(HashSet::new));
        } catch (IOException e) {
            System.out.println("An exception occurred while reading the word list.");
            System.err.println(e.getMessage());
        }
        return wordList;
    }
}
