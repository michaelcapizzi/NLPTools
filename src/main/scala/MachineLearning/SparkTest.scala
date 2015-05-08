package MachineLearning

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.log4j.Logger
import org.apache.log4j.Level

/**
 * Created by mcapizzi on 5/6/15.
 */
object SparkTest {

  //turns of messages -- not stopping all messages
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  //build a SparkConf and SparkContext
  val conf = new SparkConf().setAppName("test").setMaster("local")
  val sc = new SparkContext(conf)

  val data = 1 to 10000

  val distData = sc.parallelize(data)
  distData.filter(_ < 10).collect

  //import text
  val lines = sc.textFile("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_Garden.txt")

  //to use a variable after computation, use [variable].persist

}
