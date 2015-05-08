package MachineLearning

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

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

  //to use a variable after computation, use [variable].cache
    //[variable].persist will allow you to set the storage level
        //MEMORY_ONLY -- default
        //MEMORY_AND_DISK
        //MEMORY_ONLY_SER
        //MEMORY_AND_DISK_SER
        //DISK_ONLY

  //sc.broadcast([variable]).value
    //makes the variable available across all nodes

  //accumulators == variables that can only be "added to"
    //best used for counters and sums
      //only the driver can read the value of an accumulator
    //sc.accumulator[init value]
      //create a function that "adds to" the accumulator in certain conditions

  //to see the map of transformations and actions
    lines.toDebugString

  //transformations --> will create a new dataset from an existing one
    //https://spark.apache.org/docs/latest/programming-guide.html#transformations

  //actions --> kickstarts the transformations and return true values
    //https://spark.apache.org/docs/latest/programming-guide.html#actions

  //labeled point
      //label and features
        //val neg = LabeledPoint(0.0, Vectors.sparse(3, Array(0, 2), Array(1.0, 3.0)))
        //val pos = LabeledPoint(1.0, Vectors.dense(1.0, 0.0, 3.0))

  //load from SVM
      //MLUtils.loadLibSVMFile(sc, [path])
        //can't handle #comments
      val test = MLUtils.loadLibSVMFile(sc, "/home/mcapizzi/Desktop/6-lexical-1/1.test")
      val train = MLUtils.loadLibSVMFile(sc, "/home/mcapizzi/Desktop/6-lexical-1/1.train")

}
