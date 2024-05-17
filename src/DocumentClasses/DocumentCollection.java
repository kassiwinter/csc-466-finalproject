package DocumentClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

public class DocumentCollection implements Serializable {

    /**
     * Maps each document ID to a {@link TextVector}.
     */
    private final HashMap<Integer, TextVector> documents;

    public static String[] noiseWordArray = {"a", "about", "above", "all", "along",
            "also", "although", "am", "an", "and", "any", "are", "aren't", "as", "at",
            "be", "because", "been", "but", "by", "can", "cannot", "could", "couldn't",
            "did", "didn't", "do", "does", "doesn't", "e.g.", "either", "etc", "etc.",
            "even", "ever", "enough", "for", "from", "further", "get", "gets", "got", "had", "have",
            "hardly", "has", "hasn't", "having", "he", "hence", "her", "here",
            "hereby", "herein", "hereof", "hereon", "hereto", "herewith", "him",
            "his", "how", "however", "i", "i.e.", "if", "in", "into", "it", "it's", "its",
            "me", "more", "most", "mr", "my", "near", "nor", "now", "no", "not", "or", "on", "of", "onto",
            "other", "our", "out", "over", "really", "said", "same", "she",
            "should", "shouldn't", "since", "so", "some", "such",
            "than", "that", "the", "their", "them", "then", "there", "thereby",
            "therefore", "therefrom", "therein", "thereof", "thereon", "thereto",
            "therewith", "these", "they", "this", "those", "through", "thus", "to",
            "too", "under", "until", "unto", "upon", "us", "very", "was", "wasn't",
            "we", "were", "what", "when", "where", "whereby", "wherein", "whether",
            "which", "while", "who", "whom", "whose", "why", "with", "without",
            "would", "you", "your", "yours", "yes"};

    /**
     * Reads the file specified as input and uses the data in the file to populate the documents variable.
     * @param   filename    the path to the document to open
     * @param   docType     if not "document", treat as query
     */
    public DocumentCollection(String filename, String docType) {
        documents = new HashMap<>();
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            int docId = 0;
            TextVector textVector = null;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(".I")) {
                    if (docType.equals("document")) {  // If type is document, then ID is provided by input
                        String[] lineArr = line.split(" ");
                        docId = Integer.parseInt(lineArr[1]);
                    } else {  // Otherwise, treat as a query -> enumerate internally starting from 1
                        docId++;
                    }
                    textVector = null;  // When we come across a new document, erase previous textVector
                } else if (line.startsWith(".W")) {
                    textVector = readDocumentText(scanner, docType);
                } else if (line.startsWith(".")) {
                    // Skipping other data labels
                    continue;
                }
                if (docId > 0 && textVector != null) {
                    documents.put(docId, textVector);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private TextVector readDocumentText(Scanner scanner, String type) {
        TextVector textVector;
        if (type.equals("document")) {
            textVector = new DocumentVector();
        } else {
            textVector = new QueryVector();
        }
        while (scanner.hasNextLine() && !scanner.hasNext("\\.[A-Z].*")) {
            String line = scanner.nextLine();
            String[] lineArr = line.split("[^a-zA-Z]+");
            for (String word : lineArr) {
                String lowered = word.toLowerCase();
                if (!isNoiseWord(lowered) && word.length() > 1) {
                    textVector.add(lowered);
                }
            }
        }
        return textVector;
    }

    public void normalize(DocumentCollection dc) {
        // Calls the normalize(dc) function on each document in the collection
        for (TextVector document : getDocuments()) {
            document.normalize(dc);
        }
    }


    /**
     * @param   id  the document ID to get
     * @return  the document with the associated ID
     */
    public TextVector getDocumentById(int id) {
        /* Returns the TextVector for the document with the ID that is given. */
        return documents.get(id);
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
        for (String noiseWord : noiseWordArray) {
            if (word.equals(noiseWord)) {
                return true;
            }
        }
        return false;
    }
}
