package final_project

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import scala.util.control.Breaks._
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
      if (g.numEdges == 0) {
        return 0
      }
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
        f(partition) // Ensure partition is stored all on one machine
      })
      return partitionedMatching
    }

    def GreedyMM(g: Graph[Int, Double]): RDD[Edge[Boolean]] = {
      /*
      We must have a way to merge the result of the partitions so just return
      the same graph but with a flag on if the edge should be dropped or not
      */
      // Sort the order of the edges (pi)
      val sorted_edges = g.edges.sortBy(e => e.attr)                            // OVERFLOW ERROR HERE ?
      // Overwrite the edge attribute with a flag (true if in matching, false otherwise)
      var overwriten_attributes = sorted_edges.map({
        case Edge(src, dst, attr) => Edge(src, dst, false)
      })
      // #####################################################################
      // Make this a concrete object and do an O(1) replace
      // #####################################################################
      // Now run greedy algorithm, if edge is in matching, set flag to true
      for (edge <- overwriten_attributes.collect) {
        val src = edge.srcId
        val dst = edge.dstId
        val attr = edge.attr
        if (attr == false) {
          if (!overwriten_attributes.filter(e => e.srcId == src || e.dstId == src || e.srcId == dst || e.dstId == dst).map(e => e.attr).reduce(_ || _)) {
            // Not matched and none of the neighbors are matched!
            overwriten_attributes = overwriten_attributes.map(e => if ((e.srcId == src && e.dstId == dst) || (e.srcId == dst && e.dstId == src)) Edge(e.srcId, e.dstId, true) else e)
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
      var matching = g.mapEdges(e => "untouched")//.mapVertices((id, attr) => (id, "untouched"))
      var rounds = 0
      var untouched = matching.subgraph(epred = e => e.attr == "untouched")
      var delta = maximumDegree(untouched)
      while(delta > 1){
        println("Round: " + rounds)
        /*
        We get all the unmatched edges so we don't iterate over the same edges,
        and that way we don't need to worry about overwriting the matching graph.
        */
        // #####################################################################
        // ISSUE: subgraph doesn't filter out edges AND vertices
        // #####################################################################
        println("\t# of untouched vertices: " + untouched.numVertices)
        println("\t# of untouched edges: " + untouched.numEdges)
        rounds += 1
        println("\tDelta: " + delta)
        val p = math.pow(delta, -0.77)
        val k = math.pow(delta, 0.12).floor.toInt // Should we overestimate or underestimate k?
        println("\tP: " + p)
        println("\tK: " + k)
        /*
        1. Permutation
        Rather than have a separate ordering, we add a random Double to each edge
        as an attribute. This has the effect of randomly ordering the edges.
        There is a small issue in that the same Double may be generated twice,
        but hopefully this happens rarely enough that it doesn't matter (i.e. 
        we won't have the same Double on the same partition in the same iteration)
        */
        
        val pi = permute(untouched) // working
        // 2. Edge-sampling
        val GL = edgeSample(pi, p) // working
        /*
        3. Vertex partitioning
        Similar approach to permutation. We don't need to generate a new graph,
        we can simply store the value of the partition in the vertex attribute.
        */
        var partition = vertexPartition(GL, k)
        while (partition.numVertices == 0) {
          partition = vertexPartition(GL, k)
        }
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
            /*
            Edge is in matching
            Note: another edge may have already a vertex touching this edge so
            we must ensure the edge has not been touched yet
            !!!!!!!!!!!! THIS ISN'T WORKING PROPERLY !!!!!!!!!!!!!
            Incident edges are being marked as matched even though only one can be matched
            */
            matching = matching.mapEdges(e => if (e.attr == "untouched" && ((e.srcId == src && e.dstId == dst) || (e.dstId == src && e.srcId == dst))) "matched" else e.attr)
            // Deactivate the edges that are incident to the matched edge
            matching = matching.mapEdges(e => if ((e.attr == "untouched") && (e.srcId == src || e.dstId == src || e.srcId == dst || e.dstId == dst)) "unmatched" else e.attr)
          } else if (attr == false) {
            // Edge is not in matching
            matching = matching.mapEdges(e => if (e.attr == "untouched" && ((e.srcId == src && e.dstId == dst) || (e.dstId == src && e.srcId == dst))) "unmatched" else e.attr)
          }
        }
        untouched = matching.subgraph(epred = e => e.attr == "untouched")
        // check that untouched has elements:
        delta = maximumDegree(untouched)
        // 6. Cache the matching graph for the next iteration
        untouched.cache()
        matching.cache()
      }
      println("Rounds: " + rounds)
      // Filter and drop attributes (coerce back to int to satisfy compiler)
      val matched = matching.subgraph(epred = e => e.attr == "matched").mapEdges(e => 1)
      println("Number of edges in maximal matching: " + matched.numEdges)
      return matched
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