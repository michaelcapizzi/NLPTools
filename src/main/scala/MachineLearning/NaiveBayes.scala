package MachineLearning

import edu.arizona.sista.processors.fastnlp.FastNLPProcessor
import edu.arizona.sista.struct.Lexicon
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.immutable._

/**
 * Created by mcapizzi on 5/8/15.
 */
class NaiveBayes (
                  val trainingData: Vector[(String, String, edu.arizona.sista.processors.Document)],    //title, label, string
                  val testDocuments: Vector[(String, String, edu.arizona.sista.processors.Document)],    //title, label, string
                  val stopWords: Vector[String] = Vector(),
                  countFrequencyThreshold: Int = 0,
                  documentFrequencyThreshold: Int = 0,
                  mutualInformationThreshold: Int = 0,  //what is elasticsearch mutualInformationBuilder?
                  val masterLocation: String = "local[4]",
                  val naiveBayesModelPath: String = ""
                   ) {

  //TODO gitignore

  def annotate = {
    val p = new FastNLPProcessor
    val allDocs = this.trainingData ++ this.testDocuments
    allDocs.map(each => p.annotate(each._3))
  }


  val possibleLabels = trainingData.map(_._2).distinct.zipWithIndex.toMap


  def convertLabel(label: String) = {
    this.possibleLabels(label).toDouble
  }


//extract all vocabulary
  def extractVocabulary(withTest: Boolean, lemma: Boolean) = {
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


  def tokenizeTrainDocuments(lemma: Boolean) = {
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


  def tokenizeTestDocuments(lemma: Boolean) = {
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
  def allVocabularyLexicon(withTest: Boolean, lemma: Boolean) = {
    val lex = new Lexicon[String]
    this.extractVocabulary(withTest, lemma).map(lex.add)
  }


  //build a feature vector for each text
  def getDocWordCounts(withTest: Boolean, lemma: Boolean) = {
    for (doc <- tokenizeTrainDocuments(lemma) ++ tokenizeTestDocuments(lemma)) yield {
      val wc = wordCount(doc._3)

      (
        doc._1,                                                             //title
        doc._2,                                                             //label
        (for (word <- this.extractVocabulary(withTest, lemma)) yield {
          wc(word).toDouble
        }).toArray
      )
    }
  }


  def buildFeatureVectors(withTest: Boolean, lemma: Boolean) = {
    for (doc <- this.getDocWordCounts(withTest, lemma)) yield {
      LabeledPoint(convertLabel(doc._2), Vectors.dense(doc._3))
    }
  }


  /////////////////with Spark////////////////////

  val conf = new SparkConf().setAppName("naiveBayes").setMaster(masterLocation)
  val sc = new SparkContext(conf)

  def wordCount(tokenizedDoc: Array[String]) = {
    val parallelized = sc.parallelize(tokenizedDoc)
    val wordCount = parallelized.map(word => (word, 1)).
      reduceByKey(_+_)
    wordCount.collectAsMap
  }




}