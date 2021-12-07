#rm -rf 1002 && rm -rf 1003 && rm -rf 1004 && rm -rf 1005
#rm -rf logs
#
#sleep 5

java -jar peerProcess.jar 1001 &
sleep 1
java -jar peerProcess.jar 1002 &
sleep 1
java -jar peerProcess.jar 1003 &
sleep 1
java -jar peerProcess.jar 1004 &
sleep 1
java -jar peerProcess.jar 1005 &
sleep 1
#cmp --silent 1001/img.JPG 1002/img.JPG && cmp --silent 1001/img.JPG 1003/img.JPG && cmp --silent 1001/img.JPG 1004/img.JPG && cmp --silent 1001/img.JPG 1005/img.JPG && echo "All files are the same" || echo "A file is  different"
