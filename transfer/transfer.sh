ssh -t thunder.cise.ufl.edu ssh lin114-00 'rm -rf ~/project && mkdir ~/project && mkdir ~/project/logs'

for i in $(ls); do
  scp -o ProxyCommand="ssh thunder.cise.ufl.edu nc localhost 22" "$i" lin114-00:"~/project/$i"
done;