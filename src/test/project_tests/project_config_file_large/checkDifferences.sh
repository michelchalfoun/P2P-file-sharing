#$1 -> file to compare with
#$2 -> number of peers

for i in $( eval echo {1..$2} )
do
  c=$((i+1000));
  diff $1 "peer_${c}/$1" && echo "True" || echo "False"
done