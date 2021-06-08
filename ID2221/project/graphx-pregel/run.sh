spark-submit \
--conf spark.executor.extraClassPath=/Users/chuan/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/kafka/kafka_2.11/2.0.0/kafka_2.11-2.0.0.jar:/Users/chuan/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/kafka/kafka-clients/2.0.0/kafka-clients-2.0.0.jar \
--conf spark.driver.extraClassPath=/Users/chuan/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/kafka/kafka_2.11/2.0.0/kafka_2.11-2.0.0.jar:/Users/chuan/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/apache/kafka/kafka-clients/2.0.0/kafka-clients-2.0.0.jar \
--class "id2221.CommunityDetect" \
--master local[4] \
target/scala-2.11/graphx-pregel_2.11-0.1.jar
