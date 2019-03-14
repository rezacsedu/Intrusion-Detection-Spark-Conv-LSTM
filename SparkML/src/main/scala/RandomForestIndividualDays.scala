package iscx

import org.apache.spark.SparkContext
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import utils.{loadISCX, initSpark}
import org.apache.spark.sql.Row

import scala.collection.mutable.{ArrayBuffer}


import org.apache.spark.ml.{Pipeline, PipelineStage}
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer, VectorAssembler}



object RandomForestIndividualDays {
  def main(args: Array[String]) {
    val datasetPath = args match {
       case Array(p,_*) => p
       case _           => "/var/spark/datasets/iscxids/labeled/"
     }
    val (sc,sqlContext) = initSpark()
    // Array[(String, DataFrame)]
    val dataframes  = loadISCX(sqlContext,datasetPath)
    var featuresPerDay = new ArrayBuffer[(String, String)]()
     dataframes.foreach { d =>
      val data = d._2.select(
          "Tag"
        , "appName"
        , "destination"
        , "destinationPort"
        , "destinationTCPFlagsDescription"
        , "direction"
        , "protocolName"
        , "source"
        , "sourcePort"
        , "sourceTCPFlagsDescription"
        , "startDateTime"
        , "stopDateTime"
        , "totalDestinationBytes"
        , "totalDestinationPackets"
        , "totalSourceBytes"
        , "totalSourcePackets"
      ).na.fill("N/A")

    // MinMax
    // val (dstByMin, dstByMax) = data.agg(min($"totalDestinationBytes"), max($"totalDestinationBytes")).first match {
    //   case Row(x: Double, y: Double) => (x, y)
    // }

    // val scaledRange = lit(1) // Range of the scaled variable
    // val scaledMin = lit(0)  // Min value of the scaled variable
    // val vNormalized = ($"totalDestinationBytes" - vMin) / (vMax - vMin) // v normalized to (0, 1) range

    // val vScaled = scaledRange * vNormalized + scaledMin
    // /MinMax
    val filteredData = sqlContext.createDataFrame(data.map { row =>
          Row(
              row.getString(0)  // tag
            , row.getString(1)  // appName
            , row.getString(2).split("\\.").take(2).mkString(".")  // destination
            , row.getLong(3)  // destinationPort
            , row.getString(4)  // destinationTCPFlagsDescription
            , row.getString(5)  // direction
            , row.getString(6)  // protocolName
            , row.getString(7).split("\\.").take(2).mkString(".")  // destination
            , row.getLong(8) // sourcePort
            , row.getString(9) // sourceTCPFlagsDescription
            , row.getString(10).drop(11).take(2) // startDateTime
            , row.getString(11).drop(11).take(2)// stopDateTime
            , row.getLong(12) // totalDestinationBytes
            , row.getLong(13) // totalDestinationPackets
            , row.getLong(14) // totalSourceBytes
            , row.getLong(15) // totalSourcePackets
            )
    }, data.schema)


    // Transform the non-numerical features using the pipeline api
    val stringColumns = filteredData.columns
      .filter(!_.contains("Payload"))
      .filter(!_.contains("total"))
      .filter(!_.contains("Port"))
      .filter(!_.contains("Tag"))

    val longColumns = filteredData.columns
      .filter(c => c.contains("total") || c.contains("Port"))

    // minMax

    // Index labels, adding metadata to the label column.
    // Fit on whole dataset to include all labels in index.
    val labelIndexer = new StringIndexer()
      .setInputCol("Tag")
      .setOutputCol("indexedLabel")

    val transformers: Array[PipelineStage] = stringColumns
      .map(cname => new StringIndexer()
             .setInputCol(cname)
             .setOutputCol(s"${cname}_index")
    )

    val assembler  = new VectorAssembler()
      .setInputCols((stringColumns
                       .map(cname => s"${cname}_index")) ++ longColumns)
      .setOutputCol("features")

    // Automatically identify categorical features, and index them.
    // Set maxCategories so features with > 10 distinct values are treated as continuous.
    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(10)

    // Split the data into training and test sets (30% held out for testing)

    // Train a RandomForest model.
    val rf = new RandomForestClassifier()
      .setLabelCol("indexedLabel")
      .setFeaturesCol("indexedFeatures")
      .setNumTrees(32)
      .setMaxBins(10000)

    // Convert indexed labels back to original labels.
    val labelConverter = new IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(Array("Normal","Attack"))

    // Chain indexers and forest in a Pipeline

    val transformationStages : Array[PipelineStage] =
        Array(labelIndexer) ++
        transformers :+
        assembler :+
        featureIndexer
    val preProcessers = new Pipeline().setStages(transformationStages)

    val stages : Array[PipelineStage] =
      Array(rf,labelConverter)

    val dataModel = preProcessers.fit(filteredData)
    val transformedData = dataModel.transform(filteredData)

    transformedData.write
      .format("com.databricks.spark.csv")
      .option("header", "true")
    .save("/var/spark/datasets/iscx-processed/" + d._1)


    val pipeline = new Pipeline()
      .setStages(stages)

    val Array(trainingData, testData) = transformedData.randomSplit(Array(0.7, 0.3))
    trainingData.cache()
    testData.cache()
    // Train model.  This also runs the indexers.
    val model = pipeline.fit(trainingData)

    // // Make predictions.
    val predictions = model.transform(testData)

    // // Select example rows to display.
    predictions.select("predictedLabel", "Tag", "features").show(5)

    val rfModel = model.stages.init.last.asInstanceOf[RandomForestClassificationModel]
    println("Learned classification forest model:\n" + rfModel.toDebugString)
    val featuresImportance = rfModel.featureImportances.toArray.mkString(",")
    featuresPerDay += ((d._1, featuresImportance))
    println(s"Feature Importances for" + d._1)
    println(featuresImportance)

    // // Select (prediction, true label) and compute test error
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("indexedLabel")
      .setPredictionCol("prediction")
      .setMetricName("precision")
    val accuracy = evaluator.evaluate(predictions)
    println("Test Error = " + (1.0 - accuracy))

    }
    println("Features Importance for individual days:")
    featuresPerDay.foreach(println)
    sc.stop()
  }
}
