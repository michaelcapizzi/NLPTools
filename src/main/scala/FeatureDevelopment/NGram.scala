package FeatureDevelopment

/**
 * Created by mcapizzi on 6/19/15.
 */
class NGram(
              val textDoc: edu.arizona.sista.processors.Document,
              processor: edu.arizona.sista.processors.corenlp.CoreNLPProcessor
           ) {

  def annotate: Unit = {
    processor.annotate(textDoc)
  }

  //TODO - figure out add Padding option
  def getNGrams(gram: String, n: Int, punctuation: Boolean, withPadding: Boolean): Vector[String] = {
    /*if (withPadding) {
      if (gram == "word") {
        if (punctuation == false) {
          val words = this.textDoc.sentences.map(_.words) //get all words
            .flatten //remove sentence barriers
            .filter(word => word.matches("[A-Za-z0-9]+")) //remove punctuation
            .toVector
          words.iterator.sliding(n) //get window of n
            .withPadding("#") //pads with #
            .toVector
            .map(ngram => ngram.mkString(" ")) //convert to string
        } else {
          val words = this.textDoc.sentences.map(_.words) //get all words
            .flatten //remove sentence barriers
            .toVector
          words.iterator.sliding(n) //get window of n
            .withPadding("#") //pads with #
            .toVector
            .map(ngram => ngram.mkString(" ")) //convert to string
        }
      } else {
        if (punctuation == false) {
          val lemmas = this.textDoc.sentences.map(_.lemmas.get.toArray) //get all lemmas
            .flatten //remove sentence barriers
            .filter(lemma => lemma.matches("[A-Za-z0-9]+")) //remove punctuation
            .toVector
          lemmas.iterator.sliding(n) //get window of n
            .withPadding("#") //pads with #
            .toVector
            .map(ngram => ngram.mkString(" ")) //convert to string
        } else {
          val lemmas = this.textDoc.sentences.map(_.lemmas.get.toArray) //get all lemmas
            .flatten //remove sentence barriers
            .toVector
          lemmas.iterator.sliding(n) //get window of n
            .withPadding("#") //pads with #
            .toVector
            .map(ngram => ngram.mkString(" ")) //convert to string        }
        }
      }
    } else {*/
      if (gram == "word") {
        if (punctuation == false) {
          val words = this.textDoc.sentences.map(_.words)       //get all words
            .flatten                                            //remove sentence barriers
            .filter(word => word.matches("[A-Za-z0-9]+"))       //remove punctuation
            .toVector
          words.iterator.sliding(n).toVector                    //get window of n
            .map(ngram => ngram.mkString(" "))                  //convert to string
        } else {
          val words = this.textDoc.sentences.map(_.words)       //get all words
            .flatten                                            //remove sentence barriers
            .toVector
          words.iterator.sliding(n).toVector                    //get window of n
            .map(ngram => ngram.mkString(" "))                  //convert to string
        }
      } else {
        if (punctuation == false) {
          val lemmas = this.textDoc.sentences.map(_.lemmas.get.toArray)   //get all lemmas
            .flatten                                                      //remove sentence barriers
            .filter(lemma => lemma.matches("[A-Za-z0-9]+"))               //remove punctuation
            .toVector
          lemmas.iterator.sliding(n).toVector                   //get window of n
            .map(ngram => ngram.mkString(" "))                  //convert to string
        } else {
          val lemmas = this.textDoc.sentences.map(_.lemmas.get.toArray)   //get all lemmas
            .flatten                                                      //remove sentence barriers
            .toVector
          lemmas.iterator.sliding(n).toVector                   //get window of n
            .map(ngram => ngram.mkString(" "))                  //convert to string
        }
      }
    //}
  }



}
