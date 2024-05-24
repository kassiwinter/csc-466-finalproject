package DataProcessing;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataProcessor {

    private static final String rawDataDirPath = "data/raw";
    public static final String processedDataDirPath = "data/processed";
    public static final String[] dataSubDirs = {"left", "center", "right"};

    public static void main(String[] args) {
        if (!verifyFileStructure()) {
            System.err.print("Could not verify file structure.");
            System.exit(1);
        }
        Path rawDataDir = Paths.get(rawDataDirPath);
        Path processedDataDir = Paths.get(processedDataDirPath);
        for (String subDir : dataSubDirs) {
            Path rawDataSubDir = rawDataDir.resolve(subDir);
            Path processedDataSubDir = processedDataDir.resolve(subDir);
            // Iterate through every file in each subdirectory in raw/
            try (DirectoryStream<Path> rawDirStream = Files.newDirectoryStream(rawDataSubDir)) {
                for (Path path : rawDirStream) {
                    Path fileName = path.getFileName();
                    String article = Files.readString(path, StandardCharsets.UTF_8).toLowerCase();
                    article = article.replaceAll("[^a-z]+", " ");
                    // Write cleaned article to a file with the same name in processed/{subdir}
                    try (FileWriter fw = new FileWriter(processedDataSubDir.resolve(fileName).toString(), false)) {
                        fw.write(article);
                    }
                }
            } catch (Exception e) {
                System.out.println("An unspecified exception occurred.");
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    private static boolean verifyFileStructure() {
        Path rawDataDir = Paths.get(rawDataDirPath);
        Path processedDataDir = Paths.get(processedDataDirPath);
        if (Files.notExists(rawDataDir)) {
            System.out.println("Couldn't find raw data directory");
            return false;
        }
        for (String dataSubDir : dataSubDirs) {
            Path subDirPath = rawDataDir.resolve(dataSubDir);
            if (Files.notExists(subDirPath)) {
                System.out.println("Couldn't find raw data subdirectory " + subDirPath);
                return false;
            }
        }
        if (Files.notExists(processedDataDir)) {
            System.out.println("Couldn't find processed directory " + processedDataDir);
            if (!createDir(processedDataDir)) {
                return false;  // had an issue creating dir
            }
        }
        for (String dataSubDir : dataSubDirs) {
            Path subDirPath = processedDataDir.resolve(dataSubDir);
            if (Files.notExists(subDirPath)) {
                System.out.println("Couldn't find processed data subdirectory " + subDirPath);
                if (!createDir(subDirPath)) {
                    return false;  // had an issue creating subdir
                }
            }
        }
        return true;
    }

    private static boolean createDir(Path dirPath) {
        try {
            System.out.println("Attempting to create new directory...");
            Files.createDirectory(dirPath);
            System.out.println("Directory created.");
            return true;
        } catch (Exception e) {
            System.out.println("Couldn't create directory " + dirPath);
            e.printStackTrace(System.err);
        }
        return false;
    }
}
