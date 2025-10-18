docker exec -it broker-0 /bin/kafka-topics --create --topic passages --bootstrap-server localhost:9080 --partitions 3 --replication-factor 2
pause