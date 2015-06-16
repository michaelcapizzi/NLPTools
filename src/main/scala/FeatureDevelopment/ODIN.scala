package FeatureDevelopment

import edu.arizona.sista.odin._
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor

/**
 * Created by mcapizzi on 6/16/15.
 */

class ODIN(
            sistaDoc: edu.arizona.sista.processors.Document,
            processor: CoreNLPProcessor,
            ruleFilePath: String
          ) {

  def annotate: Unit = processor.annotate(sistaDoc)

  def showText: String = {
    sistaDoc.sentences.map(_.words.toVector).flatten.mkString(" ")
  }

  def rulesFromFile: String = {
    scala.io.Source.fromFile(ruleFilePath).mkString
  }

  def rulesFromURL: String = {
    scala.io.Source.fromURL(ruleFilePath).mkString
  }

  //should this be [Nothing]?
  def createExtractor(source: String): ExtractorEngine[Actions] = {
    if (source == "URL") new ExtractorEngine[Actions](this.rulesFromURL)
    else new ExtractorEngine[Actions](this.rulesFromFile)
  }

  def getMentions(extractor: ExtractorEngine[Actions]): Vector[Mention] = {
    extractor.extractFrom(sistaDoc).sortBy(m => (m.sentence, m.getClass.getSimpleName)).toVector
  }

  def showMentions(mentions: Vector[Mention]): Unit = {
    for (m <- mentions) {
      m match {
        case m: TextBoundMention =>
          println("TextBoundMention")
          println(s"Rule = ${m.foundBy}")
          println(s"Label = ${m.label}")
          println(s"string = ${m.text}")
          println("=" * 72)
        case m: EventMention =>
          println("EventMention")
          println(s"Rule = ${m.foundBy}")
          println(s"Label = ${m.label}")
          println(s"Text span = ${m.text}")
          println(s"trigger = ${m.trigger.text}")
          m.arguments foreach {
            case (k, vs) => for (v <- vs) println(s"$k = ${v.text}")
          }
          println("=" * 72)
        case m: RelationMention =>
          println("RelationMention")
          println(s"Rule = ${m.foundBy}")
          println(s"Label = ${m.label}")
          println(s"Text span = ${m.text}")
          m.arguments foreach {
            case (k, vs) => for (v <- vs) println(s"$k = ${v.text}")
          }
          println("=" * 72)
        case _ => ()
      }
    }
  }



}
