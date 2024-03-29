package MachineLearning

/**
 * Created by mcapizzi on 8/17/15.
 */
class NaiveBayesManual
//(
//                        val trainingData: Vector[TextDocument],
//                        val testDocuments: Vector[TextDocument],
//                        val stopWords: Vector[String] = Vector(),
//                        countFrequencyThreshold: Int = 0,
//                        documentFrequencyThreshold: Int = 0,
//                        mutualInformationThreshold: Int = 0
//                        )
{

  //TODO modify to handle generic text file



//  //total # of training documents
//  def trainingDataSize = {
//    trainingData.length.toDouble
//  }
//
//  //all possible classes
//  def possibleClasses = {
//    trainingData.map(_.gradeLevel).distinct
//  }
//
//  //class counts
//  def classCounts = {
//    (for (possibleClass <- this.possibleClasses) yield {
//      possibleClass -> trainingData.count(doc => doc.gradeLevel == possibleClass).toDouble
//    }).toMap
//  }
//
//  //calculate class *prior probabilities*
//  def priorProbabilities = {
//    (for (possibleClass <- this.classCounts.keySet) yield {
//      possibleClass -> classCounts(possibleClass) / this.trainingDataSize             //number of documents in a class / total number of documents
//    }).toMap
//  }
//
//  //extract all vocabulary from all documents in training and testing
//  def extractVocabulary = {
//    (this.trainingData ++ this.testDocuments).map(textDocument =>                     //for each document,
//      textDocument.getWords.map(                                                        //get words
//        _.toLowerCase)).flatten.distinct.                                                 //put all to lowercase, flatten, and take only distinct
//      diff(stopWords.map(_.toLowerCase))                                                //filter out stop words
//  }
//
//  //tokenize test document
//  def testDocumentTokenize = {
//    testDocuments.map(_.getWords.map(_.toLowerCase))
//  }
//
//
//
//  ///////////////////////////////with vectors///////////////////////////////////////
//
//
//  //build a lexicon for all vocabulary
//  def allVocabularyLexicon = {
//    val lex = new Lexicon[String]
//    this.extractVocabulary.map(lex.add)
//  }
//
//
//  //build a feature vector for each text
//  def makeFeatureVectors = {
//    for (text <- this.trainingData) yield {                                     //for each text
//    val counter = new Counter[String]                                           //build a counter
//      text.getWords.map(_.toLowerCase).map(counter.incrementCount(_))             //count each word instance
//
//      (                                                                           //build a tuple
//        (text.title, text.gradeLevel),                                              //title and gradeLevel
//        for (word <- this.extractVocabulary) yield {                                      //word and count
//          counter.getCount(word)
//        }
//        )
//    }
//  }
//
//
//  //concatenate by class
//  def makeFeatureVectorsConcatenized = {
//    (for (individualClass <- this.possibleClasses) yield {                                         //for each class
//    val sparseMatches = this.makeFeatureVectors.filter(tuple => tuple._1._2 == individualClass).map(each =>       //find matching feature vectors
//        SparseVector(each._2.toArray))                                                                  //build each into sparse vector
//
//      individualClass ->                                                                              //class
//        foldElementwiseSum(sparseMatches).toArray                                                     //elementwise summed feature vector
//
//    }).toMap
//  }
//
//
//  def vectorConditionalProbabilities = {
//    val vocabulary = this.allVocabularyLexicon
//    val docConcat = this.makeFeatureVectorsConcatenized
//
//    for (individualClass <- possibleClasses) yield {
//      val smoothingDenominator = sum(docConcat(individualClass)) + vocabulary.size.toDouble     //total word count of concatenized class + size of entire vocabulary
//      (
//        individualClass,
//        log(1d / smoothingDenominator),
//        for (word <- docConcat(individualClass)) yield {
//          log((word + 1d) / smoothingDenominator.toDouble)
//        }
//        )
//    }
//  }
//
//
//
//  def vectorTest = {
//    val priors = this.priorProbabilities
//    val concatDocs = this.makeFeatureVectorsConcatenized
//
//    for (doc <- this.testDocuments) yield {
//      //for each text
//      val counter = new Counter[String] //build a counter
//      doc.getWords.map(_.toLowerCase).map(counter.incrementCount(_)) //count each word instance
//
//      val vector =  (
//        (doc.title, doc.gradeLevel),                    //title and gradeLevel
//        for (word <- this.extractVocabulary) yield {
//          counter.getCount(word)                          //word and count
//        }
//        )
//
//      for (individualClass <- this.possibleClasses) yield {
//        val conditionalProbs = this.vectorConditionalProbabilities.find(_._1 == individualClass)
//
//        val conditionalProbsCalc = for (i <- 0 to vector._2.length - 1) yield {
//          if (vector._2(i) != 0.0) {
//            conditionalProbs.get._3(i) * vector._2(i)
//          } else { 0 }
//        }
//
//        (
//          individualClass,                                    //the class
//          log(priors(individualClass)) +                      //log of the prior PLUS
//            sum(conditionalProbsCalc)                        //sum of the log of the conditional probabilities
//          )
//      }
//    }
//  }
//
//
//  def vectorArgMax = {
//    for (testDoc <- this.vectorTest) yield {
//      val sorted = testDoc.sortBy(_._2).reverse
//      sorted.head._1
//    }
//  }


  /*

  ///////////////////////////////without vectors///////////////////////////////////////

  //concatenate all documents for each class into one document
  def makeDocumentsConcatenized = {
    (for (possibleClass <- possibleClasses) yield {                                       //for each class
      possibleClass -> trainingData.filter(doc => doc.gradeLevel == possibleClass).map(     //take the documents of that class
        textDocument => textDocument.getWords.map(_.toLowerCase)).flatten.                  //get words, put to lowercase, and flatten
        diff(stopWords.map(_.toLowerCase))                                                 //filter out stop words
    }).toMap
  }

  //calculate *conditional probabilities*
  def conditionalProbabilities = {
    val vocabulary = this.extractVocabulary
    val docConcat = this.makeDocumentsConcatenized

    for (possibleClass <- possibleClasses) yield {
      val smoothingDenominator = docConcat(possibleClass).length.toDouble + vocabulary.length.toDouble
      (
        possibleClass,                                                                                                    //the class
        1d / smoothingDenominator, {                                                                                      //the smoothing value for that class
        vocabulary.map(word =>                                                                                            //for each word
          word -> (docConcat(possibleClass).count(item => item == word) + 1).toDouble / smoothingDenominator.toDouble           //the count in the class + 1 / smoothingDenominator
        )
      }.toMap
        )
    }
  }


  //calculate score of test document
  def testScores = {
    val priors = this.priorProbabilities
    val concatenizedDocs = this.makeDocumentsConcatenized
    val tokenizedTestDoc = this.testDocumentTokenize
    for (testDoc <- tokenizedTestDoc) yield {                                                     //for every test document
      for (individualClass <- this.possibleClasses) yield {                                         //for each class
      val conditionalProbs = this.conditionalProbabilities.find(_._1 == individualClass).get
        (
          individualClass,                                                                          //class name
          log(priors(individualClass)) + testDoc.map(word =>                                 //for each word
            conditionalProbs._3.getOrElse(word, conditionalProbs._2)).map(number =>                     //lookup conditional probability or use smoothing value
            log(number)).sum                                                                            //take the log and sum
          )
      }
    }
  }

  def argMax = {
    for (testDoc <- this.testScores) yield {
      val sorted = testDoc.sortBy(_._2).reverse
      sorted.head._1
    }
  }

  */


}
