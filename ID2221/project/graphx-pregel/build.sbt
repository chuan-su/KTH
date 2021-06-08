name := "graphx-pregel"

version := "0.1"

scalaVersion := "2.11.12"
scalaVersion in ThisBuild := "2.11.12"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.4"
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.4.4"
libraryDependencies += "org.apache.kafka" % "kafka_2.11" % "2.0.0"

