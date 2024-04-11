package final_project

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.graphx._
import org.apache.spark.storage.StorageLevel
import org.apache.log4j.{Level, Logger}
// main function
object maximal{
  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  Logger.getLogger("org.spark-project").setLevel(Level.WARN)
  def maximalMatching(g: Graph[Int, Int]): Graph[Int, Int] = {
    return g
    }   

    def main(args: Array[String]) {


    val conf = new SparkConf().setAppName("maximal")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder.config(conf).getOrCreate()
    /* hide INFO logs */
    spark.sparkContext.setLogLevel("ERROR")
    /* You can either use sc or spark */
    println("\n=====================================")
    println("Spark UI:")
    println("\t"+sc.uiWebUrl.get)
    println("=====================================")

    if(args.length != 1) {
      println("Usage: maximal graph_path")
      sys.exit(1)
    }
    val file = args(0)
    // Get file name for output
    val saveName = "./exports/"+file.split("/").last.replace(".csv", "_matching.csv")
      
    val edges = sc.textFile(file).map(line => {val x = line.split(","); Edge(x(0).toLong, x(1).toLong , 1)} )
    val g = Graph.fromEdges[Int, Int](edges, 0, edgeStorageLevel = StorageLevel.MEMORY_AND_DISK, vertexStorageLevel = StorageLevel.MEMORY_AND_DISK)
    println("Starting to find the maximal matching")
    val startTimeMillis = System.currentTimeMillis()
    val g2 = maximalMatching(g)

    val endTimeMillis = System.currentTimeMillis()
    val durationSeconds = (endTimeMillis - startTimeMillis) / 1000
    println("\n=====================================")
    println("Maximal matching completed in " + durationSeconds + "s.")
    println("=====================================")

    val g2df = spark.createDataFrame(g2.edges)
    g2df.coalesce(1).write.format("csv").mode("overwrite").save(saveName)
    }
}