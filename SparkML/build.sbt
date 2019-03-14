name := "iscx-ids-spark"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  ("org.apache.spark" %% "spark-core" % "1.6.2" % "provided").
    exclude("org.mortbay.jetty", "servlet-api").
    exclude("commons-beanutils", "commons-beanutils-core").
    exclude("commons-collections", "commons-collections").
    exclude("commons-logging", "commons-logging").
    exclude("com.esotericsoftware.minlog", "minlog"),
  "org.apache.spark" %% "spark-mllib" % "1.6.2" % "provided",
  "com.databricks" %% "spark-csv" % "1.4.0",
  "com.databricks" %% "spark-xml" % "0.3.3"
)
