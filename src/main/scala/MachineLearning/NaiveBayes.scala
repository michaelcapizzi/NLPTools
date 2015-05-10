package MachineLearning

import edu.arizona.sista.processors.Document
import edu.arizona.sista.processors.fastnlp.FastNLPProcessor
import edu.arizona.sista.struct.Lexicon
import org.apache.spark.mllib.classification.{NaiveBayesModel, NaiveBayes}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection
import scala.collection.immutable._

/**
 * Created by mcapizzi on 5/8/15.
 */
class NaiveBayes (
                  val multinomial: Boolean,
                  val trainingData: Vector[(String, String, edu.arizona.sista.processors.Document)],    //title, label, string
                  val testDocuments: Vector[(String, String, edu.arizona.sista.processors.Document)],    //title, label, string
                  val stopWords: Vector[String] = Vector(),
                  countFrequencyThreshold: Int = 0,
                  documentFrequencyThreshold: Int = 0,
                  mutualInformationThreshold: Int = 0,  //what is elasticsearch mutualInformationBuilder?
                  val masterLocation: String = "local[4]",
                  val naiveBayesModelPath: String = ""
                   ) {


  //TODO test Bernoulli v. Multinomial

  //Spark
  val conf = new SparkConf().setAppName("naiveBayes").setMaster(masterLocation)
  val sc = new SparkContext(conf)


  def annotate: scala.Vector[Document] = {
    val p = new FastNLPProcessor
    val allDocs = this.trainingData ++ this.testDocuments
    allDocs.map(each => p.annotate(each._3))
  }


  val possibleLabels = trainingData.map(_._2).distinct.zipWithIndex.toMap


  def convertLabel(label: String): Double = {
    this.possibleLabels(label).toDouble
  }


//extract all vocabulary
  def extractVocabulary(withTest: Boolean, lemma: Boolean): scala.Vector[String] = {
    if (withTest && lemma) {
      (this.trainingData ++ this.testDocuments).map(doc =>
        doc._3.sentences.map(sent => sent.lemmas.get.toVector)).
        flatten.flatten.
        filter(_.matches("[A-Za-z]+")).
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase))
    } else if (withTest == true && lemma == false) {
      (this.trainingData ++ this.testDocuments).map(doc =>
        doc._3.sentences.map(sent => sent.words)).
        flatten.flatten.
        filter(_.matches("[A-Za-z]+")).
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase))
    } else if (withTest == false && lemma == true) {
      this.trainingData.map(doc =>
        doc._3.sentences.map(sent => sent.lemmas.get.toVector)).
        flatten.flatten.
        filter(_.matches("[A-Za-z]+")).
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase))
    } else {
      this.trainingData.map(doc =>
        doc._3.sentences.map(sent => sent.words)).
        flatten.flatten.
        filter(_.matches("[A-Za-z]+")).
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase))
    }
  }


  def tokenizeTrainDocuments(lemma: Boolean): scala.Vector[(String, String, Array[String])] = {
    if (lemma) {
      for (doc <- trainingData) yield {
        (
          doc._1,                                                     //title
          doc._2,                                                     //label
          doc._3.sentences.map(sent => sent.lemmas.get.toVector).
            flatten.
            filter(_.matches("[A-Za-z]+")).
            map(_.toLowerCase).
            diff(stopWords.map(_.toLowerCase))
        )
      }
    } else {
      for (doc <- trainingData) yield {
        (
          doc._1,                                                   //title
          doc._2,                                                   //label
          doc._3.sentences.map(sent => sent.words).
            flatten.
            filter(_.matches("[A-Za-z]+")).
            map(_.toLowerCase).
            diff(stopWords.map(_.toLowerCase))
        )
      }
    }
  }


  def tokenizeTestDocuments(lemma: Boolean): scala.Vector[(String, String, Array[String])] = {
    if (lemma) {
      for (doc <- testDocuments) yield {
        (
          doc._1,                                                   //title
          doc._2,                                                   //label
          doc._3.sentences.map(sent => sent.lemmas.get.toVector).
            flatten.
            filter(_.matches("[A-Za-z]+")).
            map(_.toLowerCase).
            diff(stopWords.map(_.toLowerCase))
        )
      }
    } else {
      for (doc <- testDocuments) yield {
        (
          doc._1,                                                   //title
          doc._2,                                                   //label
          doc._3.sentences.map(sent => sent.words).
            flatten.
            filter(_.matches("[A-Za-z]+")).
            map(_.toLowerCase).
            diff(stopWords.map(_.toLowerCase))
        )
      }
    }
  }


//build lexicon of all vocabulary
  def allVocabularyLexicon(withTest: Boolean, lemma: Boolean): scala.Vector[Int] = {
    val lex = new Lexicon[String]
    this.extractVocabulary(withTest, lemma).map(lex.add)
  }


 //build a feature vector for each text
  def getDocWordCounts(withTest: Boolean, lemma: Boolean): scala.Vector[(String, String, Array[Double])] = {
    if (this.multinomial == false) {
      if (withTest) {
        for (doc <- tokenizeTrainDocuments(lemma) ++ tokenizeTestDocuments(lemma)) yield {
          val wc = wordCount(doc._3)

          (
            doc._1,                                                             //title
            doc._2,                                                             //label
            (for (word <- this.extractVocabulary(withTest, lemma)) yield {
              wc.getOrElse(word, 0).toDouble
            }).toArray
          )
        }
      } else {
        for (doc <- tokenizeTrainDocuments(lemma)) yield {
          val wc = wordCount(doc._3)

          (
            doc._1,                                                             //title
            doc._2,                                                             //label
            (for (word <- this.extractVocabulary(withTest, lemma)) yield {
              wc.getOrElse(word, 0).toDouble
            }).toArray
          )
        }
      }
    } else {
      if (withTest) {
        for (doc <- tokenizeTrainDocuments(lemma) ++ tokenizeTestDocuments(lemma)) yield {
          val wc = wordCount(doc._3)

          (
            doc._1,                                                             //title
            doc._2,                                                             //label
            (for (word <- this.extractVocabulary(withTest, lemma)) yield {
              if (wc.getOrElse(word, 0) != 0) {
                1.0
              } else 0.0
            }).toArray
          )
        }
      } else {
        for (doc <- tokenizeTrainDocuments(lemma)) yield {
          val wc = wordCount(doc._3)

          (
            doc._1,                                                             //title
            doc._2,                                                             //label
            (for (word <- this.extractVocabulary(withTest, lemma)) yield {
              if (wc.getOrElse(word, 0) != 0) {
                1.0
              } else 0.0
            }).toArray
          )
        }
      }
    }
  }


  //TODO build getTestDocWordCounts
  def getTestDocWordCounts = {
    //
  }


  def buildFeatureVectors(withTest: Boolean, lemma: Boolean): scala.Vector[LabeledPoint] = {
    for (doc <- this.getDocWordCounts(withTest, lemma)) yield {
      LabeledPoint(convertLabel(doc._2), Vectors.dense(doc._3))
    }
  }


  //TODO finish building this method
  def buildTestFeatureVectors(lemma: Boolean) = {
    //
  }


  def wordCount(tokenizedDoc: Array[String]): collection.Map[String, Int] = {
    val parallelized = sc.parallelize(tokenizedDoc)
    val wordCount = parallelized.map(word => (word, 1)).
      reduceByKey(_+_)
    wordCount.collectAsMap
  }


  def buildTrainingModel(withTest: Boolean, lemma: Boolean, smoothing: Double, savePath: String): NaiveBayesModel = {
    val dataRDD = sc.parallelize(this.buildFeatureVectors(withTest, lemma))
    val model = NaiveBayes.train(dataRDD, lambda = smoothing)
    if (savePath != "") {
      model.save(sc, savePath)
      model
    } else model
  }


  //TODO finish building this method
  def getPredictions(withTest: Boolean, lemma: Boolean, smoothing: Double, savePath: String) = {
    //
  }

}