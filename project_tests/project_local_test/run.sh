rm -rf peer_1002 && rm -rf peer_1003 && rm -rf peer_1004 && rm -rf peer_1005
rm -rf logs

java -jar peerProcess.jar 1005 &
java -jar peerProcess.jar 1004 &
java -jar peerProcess.jar 1003 &
java -jar peerProcess.jar 1002 &
java -jar peerProcess.jar 1001 &
