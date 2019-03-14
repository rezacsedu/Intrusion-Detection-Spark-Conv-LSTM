package iscx

import org.apache.spark.SparkContext
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import utils.{loadISCX, initSpark}


object Stats {
  def main(args: Array[String]) {
    val datasetPath = args match {
       case Array(p,_*) => p
       case _           => "/var/spark/datasets/iscxids/labeled/"
     }
    val (sc,sqlContext) = initSpark()
    val dataframes = loadISCX(sqlContext,datasetPath)

    dataframes.foreach { d =>
      println("Dia: " + d._1)
      println("Número de fluxos: " + d._2.count.toString)
      val groupedByTag = d._2
                            .groupBy("Tag")
                            .agg(count("Tag").as("count"))
      // val normal = groupedByTag.filter(""Tag".equals("Normal"))
      println("Proporção normal/ataque: ")
      groupedByTag.show
    }
    sc.stop()
  }


}
