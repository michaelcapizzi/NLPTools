package FeatureDevelopment

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.feature.Word2Vec

/**
 * Created by mcapizzi on 7/5/15.
 */
class Word2VecSpark(
                    vectorFilePath: String,
                    val vocabulary: Vector[String],
                    val kMeansIterations: Int = 100
                   ) {

  //Spark
  val conf = new SparkConf().setAppName("w2v").setMaster("local[4]")
  val sc = new SparkContext(conf)

  //TODO implement Word2VecManual and KMeans together

  val kMeansNumClusters = this.vocabulary.distinct.length / 10

  /*
  val parsedData = this.vocabulary.map(w => sc.makeRDD(w))

  val zz = sc.textFile("/home/mcapizzi/Desktop/rawText/0001AL_Garden.txt").map(l => l.split(" ").toVector)

  val z = parsedData.toJavaRDD()

  val word2vec = new Word2Vec()

  val model = word2vec.fit(parsedData)

  */
}
