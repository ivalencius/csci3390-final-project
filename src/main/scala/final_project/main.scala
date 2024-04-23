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

    def maximumDegree(g: Graph[Int, String]): Int = {
      return g.degrees.map(_._2).max
    }

    def permute(g: Graph[Int, String]): Graph[Int, Double] = {
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

    def runPartitions(g: Graph[Int, Double], f: Graph[Int, Double] => RDD[Edge[Boolean]]): Array[RDD[Edge[Boolean]]]= {
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

    def GreedyMM(g: Graph[Int, Double]): RDD[Edge[Boolean]] = {
      /*
      We must have a way to merge the result of the partitions so just return
      the same graph but with a flag on if the edge should be dropped or not
      */
      // Sort the order of the edges (pi)
      val sorted_edges = g.edges.sortBy(e => e.attr)
      // Overwrite the edge attribute with a flag (true if in matching, false otherwise)
      var overwriten_attributes = sorted_edges.map({
        case Edge(src, dst, attr) => Edge(src, dst, false)
      })
      // #####################################################################
      // Up to here is working perfectly
      // #####################################################################
      // Now run greedy algorithm, if edge is in matching, set flag to true
      for (edge <- overwriten_attributes.collect()) {
        val src = edge.srcId
        val dst = edge.dstId
        val attr = edge.attr
        if (attr == false) {
          if (!overwriten_attributes.filter(e => e.srcId == src || e.dstId == src || e.srcId == dst || e.dstId == dst).map(e => e.attr).reduce(_ || _)) {
            // Not matched and none of the neighbors are matched!
            overwriten_attributes = overwriten_attributes.map(e => if (e.srcId == src && e.dstId == dst) Edge(src, dst, true) else e)
          }
        }
      }
      // THIS DOESN'T WORK BECAUSE WE NEED TO UPDATE THE GRAPH INSIDE MAP
      // val matching = overwriten_attributes.map({
      //   case Edge(src, dst, attr) => {
      //     if (attr == true){
      //       // Edge is already in matching
      //       Edge(src, dst, true)
      //     } else if (!overwriten_attributes.filter(e => e.srcId == src || e.dstId == src || e.srcId == dst || e.dstId == dst).map(e => e.attr).reduce(_ || _)) {
      //       // Edge is not in matching neither are any of it's neighbors
      //       Edge(src, dst, true)
      //     } else { 
      //       Edge(src, dst, false)
      //     }
      //     }
      // })
      // Get the vertices that are in the matching
      // val in_matching = overwriten_attributes.filter(e => e.attr == true)
      return overwriten_attributes
    }

    def maximalMatching(g: Graph[Int, Int]): Graph[Int, Int] = {
      var delta = 2 // random starting value
      var matching = g.mapEdges(e => "unmatched")
      var rounds = 0
      while(delta != 1){
        val unmatched = matching.subgraph(epred = e => e.attr == "unmatched")
        delta = maximumDegree(unmatched)
        rounds += 1
        println("\tDelta: " + delta) // random starting value
        val p = math.pow(delta, -0.77)
        val k = math.pow(delta, 0.12).ceil.toInt // Should we overestimate or underestimate k?
        /*
        We get all the unmatched edges so we don't iterate over the same edges,
        and that way we don't need to worry about overwriting the matching graph.
        */
        /*
        1. Permutation
        Rather than have a separate ordering, we add a random Double to each edge
        as an attribute. This has the effect of randomly ordering the edges.
        There is a small issue in that the same Double may be generated twice,
        but hopefully this happens rarely enough that it doesn't matter (i.e. 
        we won't have the same Double on the same partition in the same iteration)
        */
        
        val pi = permute(unmatched) // working
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
        val greedy_edges = GreedyMM(sel) // Output has a true or false on whether it is in matching
        for (edge <- greedy_edges.collect()) {
          val src = edge.srcId
          val dst = edge.dstId
          val attr = edge.attr
          if (attr == true) {
            matching = matching.mapEdges(e => if ((e.srcId == src && e.dstId == dst) || (e.dstId == src && e.srcId == dst)) "matched" else e.attr)
          }
        }
        // matching.subgraph(epred = e => e.attr == "matched").numEdges
        // #####################################################################
        // 4. Run the greedy maximal matching algorithm on each partition
        val partitionedMatching = runPartitions(partition, GreedyMM)
        // 5. Combine the results of the partitions
        val all_partitioned_edges = partitionedMatching.reduce(_ union _)
        // Update the matching
        for (edge <- all_partitioned_edges.collect()) {
          val src = edge.srcId
          val dst = edge.dstId
          val attr = edge.attr
          if (attr == true) {
            matching = matching.mapEdges(e => if ((e.srcId == src && e.dstId == dst) || (e.dstId == src && e.srcId == dst)) "matched" else e.attr)
          }
        }
        // 6. Update delta and remove all edges and nodes in the matching from the graph
        matching.cache()
      }
      println("Rounds: " + rounds)
      return matching.subgraph(epred = e => e.attr == "matched")
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
    // val file = "/Users/ilanvalencius/Documents/PhD-courses/2-Large-Scale-Data-Processing/csci3390-final-project/data/log_normal_100.csv"
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