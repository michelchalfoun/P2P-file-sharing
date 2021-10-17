package Config;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner; // Import the Scanner class to read text files

/*
NumberOfPreferredNeighbors 3
UnchokingInterval 5
OptimisticUnchokingInterval 10
FileName tree.jpg
FileSize 24301474
PieceSize 16384
 */

public class CommonConfig {
    private final int numberOfPreferredNeighbors;
    private final int unchokingInterval;
    private final int optimisticUnchokingInterval;
    private final String fileName;
    private final int fileSize;
    private final int pieceSize;

    public CommonConfig() {
        Map<String, String> fileValueByKey = getConfigurationValuesByIndex();
        numberOfPreferredNeighbors = Integer.parseInt(fileValueByKey.get("NumberOfPreferredNeighbors"));
        unchokingInterval = Integer.parseInt(fileValueByKey.get("UnchokingInterval"));
        optimisticUnchokingInterval = Integer.parseInt(fileValueByKey.get("OptimisticUnchokingInterval"));
        fileName = fileValueByKey.get("FileName");
        fileSize = Integer.parseInt(fileValueByKey.get("FileSize"));
        pieceSize = Integer.parseInt(fileValueByKey.get("PieceSize"));
    }

    public Map<String, String> getConfigurationValuesByIndex() {
        final Map<String, String> valueByKey = new HashMap<>();
        try {
            final File commonCFGFile = new File("/Users/michelchalfoun/Documents/Education/Classes/USA/UF/Fall 2021/Comp. Network Fundamentals/Project/P2P-file-sharing/project_config_file_large/Common.cfg");
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

    public int getNumberOfPreferredNeighbors() {
        return numberOfPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }
}
