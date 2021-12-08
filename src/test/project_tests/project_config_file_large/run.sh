rm -rf peer_1002 && rm -rf peer_1003 && rm -rf peer_1004 && rm -rf peer_1005 && rm -rf peer_1006
rm -rf logs

sleep 5

java -jar peerProcess.jar 1001 &
#sleep 5
java -jar peerProcess.jar 1002 &
#sleep 1
java -jar peerProcess.jar 1003 &
#sleep 1
java -jar peerProcess.jar 1004 &
#sleep 1
java -jar peerProcess.jar 1005 &
#sleep 1
java -jar peerProcess.jar 1006 &