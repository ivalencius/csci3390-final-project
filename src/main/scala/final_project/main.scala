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

    def maximumDegree(g: Graph[Int, Int]): Int = {
      return g.degrees.map(_._2).max
    }

    def permute(g: Graph[Int, Int]): Graph[Int, Double] = {
      // Adds a random Double to each edge attr
      val r = new scala.util.Random
      val added_attr = g.mapEdges(edge => r.nextDouble())
      return added_attr
    }

    def edgeSample(g: Graph[Int, Double], p: Double): Graph[Int, Double] = {
      /*
      Returns a graph with every edge randomly sampled.
      Cool trick: we can use the random Double we added in the permutation step
      */
      val sampled = g.subgraph(epred = e => e.attr < p)
      return sampled
    }

    // class chiFunction(k: Int){
    //   val r = new scala.util.Random

    //   def hash(v: Int): Int = {
    //     return r.nextInt(k)
    //   }
    // }

    def vertexPartition(g: Graph[Int, Double], k: Int): Graph[Int, Double] = {
      /*
      Store value of the partition in the vertex index
      */
      val partitioned = g.mapVertices((id, attr) => scala.util.Random.nextInt(k))
      return partitioned
    }

    def runPartitions(g: Graph[Int, Double], f: Graph[Int, Double] => Graph[Int, Double]): ??? = {
      /*
      Run the greedy maximal matching algorithm on each partition
      */
      val partitions = g.vertices.map(_._2).distinct.collect
      val partitionedMatching = partitions.map(p => {
        val partition = g.subgraph(vpred = (id, attr) => attr == p)
        f(partition)
      })
      return partitionedMatching
    }

    def GreedyMM(g: Graph[Int, Double]): Graph[Int, Double] = {
      /*
      We must have a way to merge the result of the partitions so just return
      the same graph but with a flag on if the edge should be dropped or not
      */
      // Sort the order of the edges (pi)
      val sorted_edges = g.edges.sortBy(e => e.attr)
      // Overwrite the edge attribute with a flag (true if in matching, false otherwise)
      val overwriten_attributes = sorted_edges.map({
        case Edge(src, dst, attr) => Edge(src, dst, false)
      })
      // #####################################################################
      // Up to here is working perfectly
      // #####################################################################
      // Now run greedy algorithm, if edge is in matching, set flag to true
      val matching = overwriten_attributes.map({
        case Edge(src, dst, attr) => {
          ???

          }
        }
      })
      return g
    }

    def maximalMatching(g: Graph[Int, Int]): Graph[Int, Int] = {
      var delta = maximumDegree(g)
      var matching = g
      var rounds = 0
      while(delta != 1){
        rounds += 1
        println("\tDelta: " + delta)
        val p = math.pow(delta, -0.77)
        val k = math.pow(delta, 0.12).ceil.toInt // Should we overestimate or underestimate k?
        /*
        1. Permutation
        Rather than have a separate ordering, we add a random Double to each edge
        as an attribute. This has the effect of randomly ordering the edges.
        There is a small issue in that the same Double may be generated twice,
        but hopefully this happens rarely enough that it doesn't matter (i.e. 
        we won't have the same Double on the same partition in the same iteration)
        */
        val pi = permute(g) // working
        // 2. Edge-sampling
        val GL = edgeSample(pi, p) // working
        /*
        3. Vertex partitioning
        Similar approach to permutation. We don't need to generate a new graph,
        we can simply store the value of the partition in the vertex attribute.
        */
        // val hashingFunction = new chiFunction(k)
        val partition = vertexPartition(GL, k)
        // #####################################################################
        // For testing (select first partition)
        val sel = partition.subgraph(vpred = (id, attr) => attr == 0)
        val greedy = GreedyMM(sel)
        // #####################################################################
        // 4. Run the greedy maximal matching algorithm on each partition
        val partitionedMatching = runPartitions(partition, GreedyMM)
        // 5. Combine the results of the partitions
        var matching = ...
        // 6. Update delta and remove all edges and nodes in the matching from the graph
        g = g.subgraph(epred = e => ...)
        g.cache()
        var delta = maximumDegree(g)
      }
      println("Rounds: " + rounds)
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
    val saveName = "./exports/"+file.split("/").last.replace(".csv", "_output.csv")
    // !!! TESTING !!!
    val file = "/Users/ilanvalencius/Documents/PhD-courses/2-Large-Scale-Data-Processing/csci3390-final-project/data/log_normal_100.csv"
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