package FeatureDevelopment


import breeze.linalg.DenseVector
import breeze.numerics._
import scala.collection.parallel.mutable
import scala.io.Source

/**
 * Created by mcapizzi on 5/22/15.
 */
class Word2Vec(vectorFilePath: String, clusters: Int) {

  //TODO add tuple with clusters number and cluster centroid
  def buildHashMap(clusters: Int = 0): mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]] = {
    val emptyMap = mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]]()
    /*if (clusters == 0) {*/
      for (line <- Source.fromFile(this.vectorFilePath).getLines) yield {
        val splitLine = line.split(" ")                                             //split string into elements
        val tail = splitLine.tail.toArray.map(_.toDouble)                           //build w2v vector
        val vectorizedLine = splitLine.head -> breeze.linalg.DenseVector(tail)      //build map entry
        emptyMap += vectorizedLine
      }
      emptyMap
    /*} else {
      //
    }
    emptyMap
    */
  }

  //hashMap (word -> vector)
  val w2vHashMap = buildHashMap(this.clusters)
  //reverseHashMap(word, vector)
  val w2vReverseHashMap = this.w2vHashMap.toStream

  //TODO add capacity for clusters
  def getVector(word: String): breeze.linalg.DenseVector[Double] = {
    this.w2vHashMap(word)
  }

  //TODO add capacity for clusters
  def getWord(w2vVector: breeze.linalg.DenseVector[Double]): String = {
    this.w2vReverseHashMap.find(w2v =>
      w2v._2 == w2vVector                           //find the matching element
    ).get._1                                        //extract the word
  }

  ////////////////////////w2v functions////////////////////////////

  def w2vSimilarity(wordOneVector: breeze.linalg.DenseVector, wordTwoVector: breeze.linalg.DenseVector): Double = {
    val normalized = sqrt(wordOneVector dot wordOneVector) * sqrt(wordTwoVector dot wordTwoVector)
    val dotProduct = if (wordOneVector.length == 0 || wordTwoVector.length == 0) 0 else wordOneVector dot wordTwoVector

    if (dotProduct == 0) 0 else dotProduct / normalized
  }

  def foldElementwiseSum(vectorList: Vector[DenseVector[Double]]): DenseVector[Double] = {

    def loop(vectorList: Vector[DenseVector[Double]], accum: DenseVector[Double]): DenseVector[Double] = {
      if (vectorList.tail.isEmpty) {
        accum
      }
      else {
        loop(vectorList.tail, accum + vectorList.tail.head)
      }
    }
    loop(vectorList, vectorList.head)
  }
}
