#!/usr/bin/env bash
ssh ralf@appserver1 ' fuser -k 8088/tcp '
scp ./target/ladon_de_2.0.0.jar ralf@appserver1:~/deploy/
ssh ralf@appserver1 ' nohup java -Dserver.port=8088 -Dlocalsetup -jar ./deploy/ladon_de_2.0.0.jar  >ladon.log &'
