package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/** Utility class to read and store values from CommonConfig.cfg */
public class CommonConfig {
    private final int numberOfPreferredNeighbors;
    private final int unchokingInterval;
    private final int optimisticUnchokingInterval;
    private final String fileName;
    private final int fileSize;
    private final int pieceSize;

    public CommonConfig() {
        final Map<String, String> fileValueByKey = getConfigurationValuesByIndex();
        numberOfPreferredNeighbors =
                Integer.parseInt(fileValueByKey.get("NumberOfPreferredNeighbors"));
        unchokingInterval = Integer.parseInt(fileValueByKey.get("UnchokingInterval"));
        optimisticUnchokingInterval =
                Integer.parseInt(fileValueByKey.get("OptimisticUnchokingInterval"));
        fileName = fileValueByKey.get("FileName");
        fileSize = Integer.parseInt(fileValueByKey.get("FileSize"));
        pieceSize = Integer.parseInt(fileValueByKey.get("PieceSize"));
    }

    public Map<String, String> getConfigurationValuesByIndex() {
        final Map<String, String> valueByKey = new HashMap<>();
        try {
            final File commonCFGFile = new File(System.getProperty("user.dir") + "/Common.cfg");
            final Scanner reader = new Scanner(commonCFGFile);
            while (reader.hasNextLine()) {
                final String data = reader.nextLine();
                int spaceIndex = data.indexOf(" ");
                valueByKey.put(data.substring(0, spaceIndex), data.substring(spaceIndex + 1));
            }
            reader.close();
        } catch (final FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return valueByKey;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public int getUnchokingInterval() { return unchokingInterval; }

    public int getOptimisticUnchokingInterval() { return optimisticUnchokingInterval; }

    public String getFileName() {
        return fileName;
    }
}
