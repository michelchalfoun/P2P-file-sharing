rm -rf 1002 && rm -rf 1003 && rm -rf 1004 && rm -rf 1005
rm -rf logs

sleep 5

java -jar peerProcess.jar 1001 &
PID1=$!
sleep 3
java -jar peerProcess.jar 1002 &
PID2=$!
sleep 1
java -jar peerProcess.jar 1003 &
PID3=$!
sleep 1
java -jar peerProcess.jar 1004 &
PID4=$!
sleep 1
java -jar peerProcess.jar 1005 &
PID5=$!


sleep 20
echo "**DONE**"

cmp --silent 1001/img.JPG 1002/img.JPG && cmp --silent 1001/img.JPG 1003/img.JPG && cmp --silent 1001/img.JPG 1004/img.JPG && cmp --silent 1001/img.JPG 1005/img.JPG && echo "All files are the same" || echo "A file is  different"
