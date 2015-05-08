package MachineLearning

import edu.arizona.sista.processors.fastnlp.FastNLPProcessor
import edu.arizona.sista.struct.Lexicon
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.immutable._

/**
 * Created by mcapizzi on 5/8/15.
 */
class NaiveBayes (
                  val trainingData: Vector[edu.arizona.sista.processors.Document],
                  val testDocuments: Vector[edu.arizona.sista.processors.Document],
                  val stopWords: Vector[String] = Vector(),
                  countFrequencyThreshold: Int = 0,
                  documentFrequencyThreshold: Int = 0,
                  mutualInformationThreshold: Int = 0,  //what is elasticsearch mutualInformationBuilder?
                  val masterLocation: String = "local[4]",
                  val naiveBayesModelPath: String = ""
                   ) {

  def annotate = {
    val p = new FastNLPProcessor
    val allDocs = this.trainingData ++ this.testDocuments
    allDocs.map(each => p.annotate(each))
  }

//extract all vocabulary
  def extractVocabulary(withTest: Boolean, lemma: Boolean) = {
    if (withTest && lemma) {
      (this.trainingData ++ this.testDocuments).map(doc =>
        doc.sentences.map(sent => sent.lemmas.get.toVector)).
        flatten.flatten.
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase))
    } else if (withTest == true && lemma == false) {
      (this.trainingData ++ this.testDocuments).map(doc =>
        doc.sentences.map(sent => sent.words)).
        flatten.flatten.
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase))
    } else if (withTest == false && lemma == true) {
      this.trainingData.map(doc =>
        doc.sentences.map(sent => sent.lemmas.get.toVector)).
        flatten.flatten.
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase))
    } else {
      this.trainingData.map(doc =>
        doc.sentences.map(sent => sent.words)).
        flatten.flatten.
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase))
    }
  }

  def tokenizeTestDocuments(lemma: Boolean) = {
    if (lemma) {
      testDocuments.map(doc =>
        doc.sentences.map(sent => sent.lemmas.get.toVector).
        flatten.
        map(_.toLowerCase).
        distinct.
        diff(stopWords.map(_.toLowerCase)))
    } else {
      testDocuments.map(doc =>
        doc.sentences.map(sent => sent.words).
          flatten.
          map(_.toLowerCase).
          distinct.
          diff(stopWords.map(_.toLowerCase)))
    }
  }

//build lexicon of all vocabulary
  def allVocabularyLexicon(withTest: Boolean, lemma: Boolean) = {
    val lex = new Lexicon[String]
    this.extractVocabulary(withTest, lemma).map(lex.add)
  }

  /////////////////with Spark////////////////////

  val conf = new SparkConf().setAppName("naiveBayes").setMaster(masterLocation)
  val sc = new SparkContext(conf)

  def wordCount(withTest: Boolean, lemma: Boolean) = {
    val parallelized = sc.parallelize(this.extractVocabulary(withTest, lemma))
    val wordCount = parallelized.map(word => (word, 1)).
      reduceByKey(_+_)
    ListMap[String, Int]() ++ wordCount.collectAsMap
  }

}