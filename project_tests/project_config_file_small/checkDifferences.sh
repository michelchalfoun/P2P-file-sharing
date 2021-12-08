file="thefile"

echo
echo "Source file(s): peer_1001/$file & peer_1006/$file"
diff "peer_1001/$file" "peer_1006/$file" && echo "Source files are identical" || echo "Source files are different"
echo

for (( i=2; i<=5; i++ ))
do
  diff "peer_1001/$file" "peer_100$i/$file" && echo "peer_1001/$file & peer_100$i/$file are identical" || echo "peer_1001/$file & peer_100$i/$file are different"
done

for (( i=7; i<=9; i++ ))
do
  diff "peer_1001/$file" "peer_100$i/$file" && echo "peer_1001/$file & peer_100$i/$file are identical" || echo "peer_1001/$file & peer_100$i/$file are different"
done

echo