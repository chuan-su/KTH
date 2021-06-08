DOCKER_NETWORK = docker_id2221
ENV_FILE = ./hadoop/hadoop.env
build:
	docker build -t id2221/hadoop-base ./hadoop/base
	docker build -t id2221/hadoop-namenode ./hadoop/namenode
	docker build -t id2221/hadoop-datanode ./hadoop/datanode
	docker build -t id2221/hadoop-resourcemanager ./hadoop/resourcemanager
	docker build -t id2221/hadoop-nodemanager ./hadoop/nodemanager
	docker build -t id2221/hadoop-historyserver ./hadoop/historyserver
	docker build -t id2221/hbase-base ./hbase/base
	docker build -t id2221/hbase-standalone ./hbase/standalone
up:
	docker-compose up -d
down:
	docker-compose down
wordcount:
	docker build -t id2221/hadoop-wordcount ./submit
	docker build -t hadoop-wordcount ./submit
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} id2221/hadoop-base hdfs dfs -mkdir -p /input/
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} id2221/hadoop-base hdfs dfs -copyFromLocal /opt/hadoop-3.1.2/README.txt /input/
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} hadoop-wordcount
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} id2221/hadoop-base hdfs dfs -cat /output/*
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} id2221/hadoop-base hdfs dfs -rm -r /output
	docker run --network ${DOCKER_NETWORK} --env-file ${ENV_FILE} id2221/hadoop-base hdfs dfs -rm -r /input
hadoop_shell:
	docker exec -it namenode bash
hbase_shell:
	docker exec -it hbase bash
