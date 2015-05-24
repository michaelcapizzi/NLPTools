package FeatureDevelopment


import java.io.Serializable

import breeze.linalg.DenseVector
import breeze.numerics._
import scala.collection
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.mutable
import scala.io.Source

/**
 * Created by mcapizzi on 5/22/15.
 */
class Word2Vec(
                vectorFilePath: String,
                vocabulary: Vector[String],
                clusters: Int = 0,
                build: Boolean = false
              ) {


  //TODO add tuple with clusters number and cluster centroid
  def buildHashMap(clusters: Int = 0): mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]] = {
    val emptyMap = new mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]]()
    /*if (clusters == 0) {*/
      //for (line <- Source.fromFile(this.vectorFilePath).getLines.toVector.par) yield {
      for (word <- vocabulary.par) yield {
        val line = Source.fromFile(this.vectorFilePath).getLines.find(it => it.split(" ").head == word).get
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
  val w2vHashMap = if (build) buildHashMap(this.clusters) else mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]]()
  //stream (word, vector)
  val w2vStream = this.w2vHashMap.toStream



  //TODO add capacity for clusters
  //get w2v vector for given word
  def getVector(word: String): breeze.linalg.DenseVector[Double] = {
    if (build) this.w2vHashMap(word)
    else {
      val found = Source.fromFile(this.vectorFilePath).getLines.find(line =>
        line.split(" ").head == word                                          //find match in file
      ).get.
        split(" ").                                                           //split into list
        tail.toArray.map(_.toDouble)                                          //take tail as Array[Double]
      DenseVector(found)                                                      //convert to DenseVector
    }
  }


  //TODO add capacity for clusters
  //TODO investigate why doesn't work
  //get closest word for given w2v vector
  def getWord(w2vVector: breeze.linalg.DenseVector[Double]): Serializable = {
    if (build) {
      this.w2vStream.find(w2v =>
        w2v._2 == w2vVector                                 //find the matching element
      ).getOrElse(findClosestWord(w2vVector, 1).head._1)    //extract the exact match word or the closest word
    } else {
      Source.fromFile(this.vectorFilePath).getLines.find(line =>
        line.split(" ").tail.toArray.map(_.toDouble) == w2vVector.toArray
      ).getOrElse(findClosestWord(w2vVector, 1).head._1)
    }
  }

  ////////////////////////w2v functions////////////////////////////

  //cosine similarity
  def w2vCosSim(wordOneVector: breeze.linalg.DenseVector[Double], wordTwoVector: breeze.linalg.DenseVector[Double]): Double = {
    val normalized = sqrt(wordOneVector dot wordOneVector) * sqrt(wordTwoVector dot wordTwoVector)
    val dotProduct = if (wordOneVector.length == 0 || wordTwoVector.length == 0) 0 else wordOneVector dot wordTwoVector
    if (dotProduct == 0) 0 else dotProduct / normalized
  }

  //TODO add cluster capacity
  //closest matching word
  def findClosestWord(w2vVector: breeze.linalg.DenseVector[Double], take: Int): ParVector[(String, Double)] = {
    if (build) {
      (for (word <- w2vStream) yield {
        word._1 -> w2vCosSim(word._2, w2vVector)
      }).toVector.sortBy(_._2).reverse.par.take(take)
    } else {
      val buffer = scala.collection.mutable.Buffer[(String, breeze.linalg.DenseVector[Double])]()
      for (line <- Source.fromFile(this.vectorFilePath).getLines) {
        if (line.matches("""[A-Za-z]""")) {
          val splitLine = line.split(" ")
          val word = splitLine.head
          val vector = DenseVector(splitLine.tail.toArray.map(_.toDouble))
          buffer += ((word, vector))
        }
      }
      (for (word <- buffer) yield {
        word._1 -> w2vCosSim(word._2, w2vVector)
      }).toVector.sortBy(_._2).reverse.par.take(take)
    }
  }


  //add list of vectors in a list
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
