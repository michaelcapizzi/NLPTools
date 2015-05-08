name := "NLPTools"

version := "1.0"

scalaVersion := "2.11.6"

javaOptions += "-Xmx7G"

libraryDependencies ++= Seq(
  "edu.arizona.sista" % "processors_2.11" % "5.2",
  "edu.arizona.sista" % "processors_2.11" % "5.2" classifier "models",
  "org.apache.commons" % "commons-math3" % "3.3",
  "org.apache.commons" % "commons-compress" % "1.9",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.elasticsearch" % "elasticsearch" % "1.5.2",
  "com.quantifind" % "wisp_2.11" % "0.0.4",
  "org.scalanlp" % "breeze-natives_2.11" % "0.11.2",
  "org.scalanlp" % "breeze_2.11" % "0.11.2",
  "edu.cmu.cs" % "ark-tweet-nlp" % "0.3.2",
  "org.twitter4j" % "twitter4j" % "4.0.3",
  "org.facebook4j" % "facebook4j-core" % "2.2.2",
  "org.scala-tools" % "maven-scala-plugin" % "2.15.2",
  "com.github.fommil.netlib" % "all" % "1.1.2",
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "org.apache.spark" % "spark-core_2.11" % "1.3.1",
  "org.apache.spark" % "spark-streaming_2.11" % "1.3.1"
  //"edu.stanford.nlp" % "stanford-corenlp" % "3.5.0",
)

resolvers ++= Seq(
//  "Akka Repository" at "http://repo.akka.io/releases/",
//  "Spray Repository" at "http://repo.spray.cc/",
//  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)
