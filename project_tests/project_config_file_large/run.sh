rm -rf 1002 && rm -rf 1003 && rm -rf 1004 && rm -rf 1005 && rm -rf 1006
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