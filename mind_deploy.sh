#!/usr/bin/env bash
ssh ralf@appserver1 ' fuser -k 8088/tcp '
scp ./target/ladon_de_2.*.jar ralf@appserver1:~/deploy/
ssh ralf@appserver1 ' nohup java -Dserver.port=8088 -Dlocalsetup=true -jar ./deploy/ladon_de_2.*.jar  >ladon.log &'
