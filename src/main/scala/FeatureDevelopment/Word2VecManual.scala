package FeatureDevelopment


import java.io.Serializable
import nak.cluster._
import breeze.linalg.DenseVector
import breeze.numerics._
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.{mllib, SparkContext, SparkConf}
import scala.collection
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.mutable
import scala.io.Source

/**
 * Created by mcapizzi on 5/22/15.
 */
class Word2VecManual(
                vectorFilePath: String,
                val vocabulary: Vector[String]/*,
                build: Boolean = false*/
              ) {

  ////////////////////////clusters////////////////////////////

  /*//initialize Spark
  val conf = new SparkConf().setAppName("w2v").setMaster("local[4]")
  val sc = new SparkContext(conf)

  val breezeVectors = this.vocabulary.map(w => this.getVector(w))

  val mllibVectors = breezeVectors.map(v => Vectors.dense(v.toArray))

  val rdd = sc.makeRDD(mllibVectors)

  //KMeans
  val clusters = KMeans.train(rdd, if (this.vocabulary.distinct.length > 20) this.vocabulary.distinct.length / 10 else 2, 100)

  def getCentroids: Vector[(mllib.linalg.Vector, Int)] = {
    clusters.clusterCenters.zipWithIndex.toVector
  }

  def predictCluster(word: String): Int = {
    val wordVector = Vectors.dense(this.getVector(word).toArray)

    this.clusters.predict(wordVector)
  }*/

  ////////////////////////clusters////////////////////////////
  //TODO figure out how to implement clusters

  //TODO add situation when word not in word2vec data
  //TODO add tuple with clusters number and cluster centroid
  def buildHashMap: mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]] = {
    val emptyMap = new mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]]()
    /*if (this.clusters == 0) {*/
      //for (line <- Source.fromFile(this.vectorFilePath).getLines.toVector.par) yield {
      for (word <- vocabulary.map(each => each.toLowerCase)) yield {
        val line = Source.fromFile(this.vectorFilePath).getLines.find(it => it.split(" ").head == word).get
        //val line = Source.fromURL(this.vectorFilePath).getLines.find(it => it.split(" ").head == word).get
        val splitLine = line.split(" ")                                             //split string into elements
        val tail = splitLine.tail.toArray.map(_.toDouble)                           //build w2v vector
        val vectorizedLine = splitLine.head -> breeze.linalg.DenseVector(tail)      //build map entry
        emptyMap += vectorizedLine
      }
      emptyMap
    /*} else {
      for (word <- vocabulary.par) yield {
        val line = Source.fromFile(this.vectorFilePath).getLines.find(it => it.split(" ").head == word).get
        val splitLine = line.split(" ") //split string into elements
        val tail = splitLine.tail.toArray.map(_.toDouble) //build w2v vector
        val vectorizedLine = splitLine.head -> breeze.linalg.DenseVector(tai/*//initialize Spark
  val conf = new SparkConf().setAppName("w2v").setMaster("local[4]")
  val sc = new SparkContext(conf)

  val breezeVectors = this.vocabulary.map(w => this.getVector(w))

  val mllibVectors = breezeVectors.map(v => Vectors.dense(v.toArray))

  val rdd = sc.makeRDD(mllibVectors)

  //KMeans
  val clusters = KMeans.train(rdd, if (this.vocabulary.distinct.length > 20) this.vocabulary.distinct.length / 10 else 2, 100)

  def getCentroids: Vector[(mllib.linalg.Vector, Int)] = {
    clusters.clusterCenters.zipWithIndex.toVector
  }

  def predictCluster(word: String): Int = {
    val wordVector = Vectors.dense(this.getVector(word).toArray)

    this.clusters.predict(wordVector)
  }*/l) //build map entry
        emptyMap += vectorizedLine
      }
    //convert emptyMap values to mllib.Vectors
      //val vectorList =
    //val clusters = KMeans.train(vectorList, this.clusters, 100)    //100 iterations
    //what to do with clusters?
    }*/
  }


  //hashMap (word -> vector)
  val w2vHashMap = /*if (build)*/ this.buildHashMap /*else mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]]()*/
  //stream (word, vector)
  val w2vStream = this.w2vHashMap.toStream

  /////clustering

  val kmeans = new Kmeans[breeze.linalg.DenseVector[Double]](this.w2vStream.map(_._2).toVector)

  val k = if (this.vocabulary.length < 20) {
            1
          } else if (this.vocabulary.length >= 20 && this.vocabulary.length < 1000) {
            this.vocabulary.length / 10
          } else {
            this.vocabulary.length / 1000
          }

  val centroids = kmeans.run(k)._2



  //TODO test
  def addToHashMap(word: String): mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]] = {
    val found = Source.fromFile(this.vectorFilePath).getLines.find(line =>
      line.split(" ").head == word
    )

    val w2vVector = DenseVector(found.get.split(" ").tail.toArray.map(_.toDouble))

    this.w2vHashMap += (word -> w2vVector)
  }


  //TODO test
  def removeFromHashMap(word: String): mutable.ParHashMap[String, breeze.linalg.DenseVector[Double]] = {
    this.w2vHashMap -= word
  }

  //TODO add capacity for clusters
  //get w2v vector for given word
  def getVector(word: String): breeze.linalg.DenseVector[Double] = {
    /*if (build)*/ this.w2vHashMap(word)
   /* else {
      val found = Source.fromFile(this.vectorFilePath).getLines.find(line =>
        line.split(" ").head == word                                          //find match in file
      ).get.
        split(" ").                                                           //split into list
        tail.toArray.map(_.toDouble)                                          //take tail as Array[Double]
      DenseVector(found)                                                      //convert to DenseVector
    }*/
  }



  //TODO add capacity for clusters
  //TODO investigate why doesn't work
  //get closest word for given w2v vector
  def getWord(w2vVector: breeze.linalg.DenseVector[Double]): Serializable = {
   /* if (build) { */
      this.w2vStream.find(w2v =>
        w2v._2 == w2vVector                                 //find the matching element
      ).getOrElse(findClosestWord(w2vVector, 1).head._1)    //extract the exact match word or the closest word
    /*} else {
      Source.fromFile(this.vectorFilePath).getLines.find(line =>
        line.split(" ").tail.toArray.map(_.toDouble) == w2vVector.toArray
      ).getOrElse(findClosestWord(w2vVector, 1).head._1)
    }*/
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
    /*if (build) {*/
      (for (word <- w2vStream) yield {
        word._1 -> w2vCosSim(word._2, w2vVector)
      }).toVector.sortBy(_._2).reverse.par.take(take)
    /*} else {
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
    }*/
  }

  //TODO test word not in hashmap
  //find words most similar to given word (within cosine similarity range)
  def isSimilar(w2vWord: String, cosSimMin: Double, take: Int): ParVector[(String, Double)] = {
    (if (w2vHashMap.keySet.contains(w2vWord)) {
      for (word <- w2vHashMap.keySet) yield {
        word -> w2vCosSim(w2vHashMap(word), w2vHashMap(w2vWord))
      }
    } else {
      this.addToHashMap(w2vWord)
      for (word <- w2vHashMap.keySet) yield {
        word -> w2vCosSim(w2vHashMap(word), w2vHashMap(w2vWord))
      }
    }).toVector.filter(_._2 >= cosSimMin).sortBy(_._2).reverse.par
  }


  //TODO test
  //find words most dissimilar to given word (within cosine similarity range)
  def isNotSimilar(w2vWord: String, cosSimMax: Double, take: Int): ParVector[(String, Double)] = {
    (if (w2vHashMap.keySet.contains(w2vWord)) {
      for (word <- w2vHashMap.keySet) yield {
        word -> w2vCosSim(w2vHashMap(word), w2vHashMap(w2vWord))
      }
    } else {
      this.addToHashMap(w2vWord)
      for (word <- w2vHashMap.keySet) yield {
        word -> w2vCosSim(w2vHashMap(word), w2vHashMap(w2vWord))
      }
    }).toVector.filter(_._2 <= cosSimMax).sortBy(_._2).par
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
