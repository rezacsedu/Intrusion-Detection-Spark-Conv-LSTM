package iscx

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql._

object utils {
  def initSpark() : (SparkContext,SQLContext) = {
    val conf = new SparkConf().setAppName("Simple Application")
      .setMaster("spark://10.90.67.77:7077")
      // .setMaster("local[4]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    (sc,sqlContext)
  }

  def loadISCX(sqlContext : SQLContext, path : String) : Array[(String, DataFrame)] = {
    val days : Array[String] = Array(
      "TestbedSatJun12"
    , "TestbedSunJun13"
    , "TestbedMonJun14"
    , "TestbedTueJun15"
    , "TestbedWedJun16"
    , "TestbedThuJun17"
    )

    val xmlFiles = days.map(d => path + d + ".xml")
    val zipped = days.zip(xmlFiles)

    zipped.map { d =>
      (d._1.drop(10), sqlContext
              .read
              .format("com.databricks.spark.xml")
              .option("rowTag",d._1 + "Flows")
              .load(d._2)
              )
    }
    // TestbedJun12
    // val jun12 = sqlContext.read
    //   .format("com.databricks.spark.xml")
    //   .option("rowTag",days(0))
    //   .load(xmlFiles(0))
    // // TestbedJun13
    // val jun13 = sqlContext.read
    //   .format("com.databricks.spark.xml")
    //   .option("rowTag",days(1) + "Flows")
    //   .load(xmlFiles(1))
    // // TestbedJun14
    // val jun14 = sqlContext.read
    //   .format("com.databricks.spark.xml")
    //   .option("rowTag",days(2) + "Flows")
    //   .load(xmlFiles(2))
    // // TestbedJun15
    // val jun15 = sqlContext.read
    //   .format("com.databricks.spark.xml")
    //   .option("rowTag",days(3) + "Flows")
    //   .load(xmlFiles(3))
    // // TestbedJun16
    // val jun16 = sqlContext.read
    //   .format("com.databricks.spark.xml")
    //   .option("rowTag",days(4) + "Flows")
    //   .load(xmlFiles(4))
    // // TestbedJun17
    // val jun13 = sqlContext.read
    //   .format("com.databricks.spark.xml")
    //   .option("rowTag",days(1) + "Flows")
    //   .load(xmlFiles(2))
  }
}
