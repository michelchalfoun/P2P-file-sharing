rm -rf 1002 && rm -rf 1003 && rm -rf 1004 && rm -rf 1005 && rm -rf 1006
rm -rf logs

sleep 5

java -jar peerProcess.jar 1001 &
PID1=$!
sleep 5
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
sleep 1
java -jar peerProcess.jar 1006 &
PID6=$!

sleep 30

echo "\n**Done**\n"

kill $PID1
kill $PID2
kill $PID3
kill $PID4
kill $PID5
kill $PID6

cmp --silent 1001/tree.jpg 1002/tree.jpg && cmp --silent 1001/tree.jpg 1003/tree.jpg && cmp --silent 1001/tree.jpg 1004/tree.jpg && cmp --silent 1001/tree.jpg 1005/tree.jpg && cmp --silent 1001/tree.jpg 1006/tree.jpg && echo "All files are the same" || echo "A file is  different"