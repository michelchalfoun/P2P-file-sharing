# P2P-file-sharing
## How to build?
Run maven to build all of the source files (run where pom.xml file is located)
```
cd P2P-file-sharing
mvn clean package
```
The following will be built in the **P2P-file-sharing/target folder**:
- .class files (P2P-file-sharing/target/classes)
- Standalone executable .jar file (P2P-file-sharing/target/peerProcess.jar)

# How to run?
Before running this project, make sure you have the Common.cpg, PeerInfo.cpg, and a /logs folder within the executing directory/path, ex:
```
 | current dir
 +-- peerProcess.java
 +-- Common.cpg
 +-- PeerInfo.cpg
 +-- logs (this is a directory that must be created manually, for now...)
```
There are mulitiple ways to run this project:
1) **RECOMMENDED OPTION**: Use the .jar file
   ```java -jar peerProcess.jar 1001```
   Run this anywhere, but make sure you also have Common.cpg, PeerInfo.cpg, and logs/ within the executing directory
2) Using Maven
   ```mvn exec:java -Dexec.mainClass="peer.Peer" -Dexec.args="1001"```
   Run this from the project directory, where the pom.xml file is located (P2P-file-sharing/)
3) Using the .class files
   ``` java peer/Peer 1001 ```
   Run this from the classes directory (P2P-file-sharing/target/class)