name := "HW3"

version := "1.0"

scalaVersion := "2.11.8"
scalaVersion in ThisBuild := "2.11.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.2"
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.3.2"