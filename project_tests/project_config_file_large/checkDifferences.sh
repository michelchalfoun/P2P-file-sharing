file="tree.jpg"
numPeers=6

echo
echo "Source file(s): peer_1001/"$file
echo

for (( i=2; i<=$numPeers; i++ ))
do
  diff "peer_1001/$file" "peer_100$i/$file" && echo "peer_1001/$file & peer_100$i/$file are identical" || echo "peer_1001/$file & peer_100$i/$file are different"
done

echo