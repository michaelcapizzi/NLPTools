package Evaluation

/**
 * Created by mcapizzi on 5/16/15.
 */
class Metrics(
               fullScoreList: Vector[(String, Vector[(String, String, String)])]    //Vector[(machineLearningAlgorithm, Vector[(title, mlScore, actualScore)])]
             ) {

  //scoreList.map(ml => (ml._1 -> eval.[method](ml._2)))

  val possibleLabels = fullScoreList.head._2.map(_._3).distinct

  //accuracy metric
  def accuracy(mlScoreList: Vector[(String, String, String)]) = {
    def isAccurate(mlScore: String, actualScore: String): Int = {
      if (mlScore == actualScore) 1 else 0
    }

    (mlScoreList.map(item => isAccurate(item._2, item._3)).sum.toDouble / mlScoreList.length.toDouble) * 100          //sum up correct and divide by total number of items then multiply by 100
  }

  //TODO rewrite to handle any labeling
  def distanceAccuracy6(mlScoreList: Vector[(String, String, String)]) = {
    def convertLabel(label: String): Int = {
      label match {
        case "0001" => 0
        case "0203" => 1
        case "0405" => 2
        case "0608" => 3
        case "0910" => 4
        case "1112" => 5
      }
    }

    def calculateDistance(mlScore: Int, actualScore: Int): Int = {
      mlScore - actualScore
    }

    for (score <- mlScoreList) yield {
      score._1 -> calculateDistance(convertLabel(score._2), convertLabel(score._3))
    }
  }


  //TODO fix to handle rebuilt distanceAccuracy above
  /*
  //histogram of distance accuracy
  def distanceAccuracyTotalHistogram(mlScoreList: Vector[(String, String, String)], numberOfClasses: Int) = {
    val distanceAccuracyScores = if (numberOfClasses == 6) this.distanceAccuracy6(mlScoreList) else this.distanceAccuracy3(mlScoreList)

    for (distance <- distanceAccuracyScores.map(_._2).distinct.sorted) yield {
      distance -> distanceAccuracyScores.count(_._2 == distance)
    }
  }
  */


  //determing relevance labels for each instance
  def relevanceLabels(mlScoreList: Vector[(String, String, String)]): Map[String, Vector[String]] = {
    def determineRelevanceLabels(relevantClass: String, mlScore: String, actualScore: String): String = {
      if (relevantClass == actualScore & relevantClass == mlScore) "truePositive"           //it was relevant, and it was correctly scored as relevant
      else if (relevantClass != actualScore & relevantClass == mlScore) "falsePositive"     //it was irrelevant, but it was incorrectly scored as relevant
      else if (relevantClass == actualScore & relevantClass != mlScore) "falseNegative"     //it was relevant, but it was incorrectly scored as irrelevant
      else "trueNegative"
    }

    (for (label <- possibleLabels) yield {                                                      //for every possible label
      label -> mlScoreList.map(score => determineRelevanceLabels(label, score._2, score._3))      //generate relevance tags for each item
    }).toMap
  }

  //calculates recall
  def recall(mlScoreList: Vector[(String, String, String)]) = {
    def calculateRecall(truePositive:Double, falseNegative: Double): Double = {
      if ((truePositive + falseNegative) == 0) 0                                            //in case denominator is 0
      else truePositive / (truePositive + falseNegative)                                    //otherwise calculate recall
    }

    val relevanceLabelsMap = relevanceLabels(mlScoreList)
    (for (relevance <- relevanceLabelsMap.keySet.toList) yield {
      relevance -> calculateRecall(relevanceLabelsMap(relevance).count(_.matches("truePositive")).toDouble, relevanceLabelsMap(relevance).count(_.matches("falseNegative")).toDouble)
    }).toMap
  }

  //calculates precision
  def precision(mlScoreList: Vector[(String, String, String)]) = {
    def calculatePrecision(truePositive: Double, falsePositive: Double): Double = {
      if ((truePositive + falsePositive) == 0) 0 //in case denominator is 0
      else truePositive / (truePositive + falsePositive) //otherwise calculate recall
    }

    val relevanceLabelsMap = relevanceLabels(mlScoreList)
    (for (relevance <- relevanceLabelsMap.keySet.toList) yield {
      relevance -> calculatePrecision(relevanceLabelsMap(relevance).count(_.matches("truePositive")).toDouble, relevanceLabelsMap(relevance).count(_.matches("falsePositive")).toDouble)
    }).toMap
  }

  //calculates F1
  def calculateF1(precisionScore: Double, recallScore: Double): Double = {
    if ((precisionScore + recallScore) == 0) 0                                            //in case denominator is 0
    else (2 * precisionScore * recallScore) / (precisionScore + recallScore)              //otherwise calculate recall
  }

  def f1(mlScoreList: Vector[(String, String, String)]) = {
    val relevanceLabelsMap = relevanceLabels(mlScoreList)
    (for (relevance <- relevanceLabelsMap.keySet.toList) yield {
      val precisionScore = precision(mlScoreList)(relevance)
      val recallScore = recall(mlScoreList)(relevance)
      relevance -> calculateF1(precisionScore, recallScore)
    }).toMap
  }

  //builds map of macros scores
  def macroScores(mlScoreList: Vector[(String, String, String)]) = {
    val macroPrecision = precision(mlScoreList: Vector[(String, String, String)]).values.toList.sum / possibleLabels.length
    val macroRecall = recall(mlScoreList: Vector[(String, String, String)]).values.toList.sum / possibleLabels.length
    Map(
      "macroPrecision" -> macroPrecision,
      "macroRecall" -> macroRecall,
      "macroF1" -> calculateF1(macroPrecision, macroRecall)
    )
  }

}
