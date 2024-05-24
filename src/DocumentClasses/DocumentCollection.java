package DocumentClasses;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DocumentCollection implements Serializable {

    private static final String stopWordsPath = "src/DocumentClasses/files/stopwords-en.txt";

    /**
     * Maps each document ID to a {@link TextVector}.
     */
    private final HashMap<Integer, TextVector> documents;

    public static String[] noiseWordArray = readWordList(stopWordsPath);

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

    private static String[] readWordList(String filepath) {
        String[] wordList = {};
        try {
            List<String> lines = Files.readAllLines(Paths.get(filepath));
            String allText = String.join(" ", lines);
            wordList = allText.split("\\s+");
        } catch (IOException e) {
            System.out.println("An exception occurred while reading the word list.");
            System.err.println(e.getMessage());
        }
        return wordList;
    }
}
