
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

/**
 Author: Jiwei Zhang  
 Andrew ID : jiweiz
 */
 object PageRank {


  def main(args: Array[String]) {   

    val sparkConf = new SparkConf().setAppName("pagerank")
    val n = 10
    val sc = new SparkContext(sparkConf)
    //load file
    val lines = sc.textFile("hdfs:///input")
    // get all the distinct keys
    val allkeys = lines.flatMap(line => line.split("\t")).distinct
    // get the map with all the nodes
    val totalNodesLinks = lines.flatMap(line => line.split("\t")).map(word => (word, 1)).reduceByKey(_ + _)
    // get the number of nodes
    val nodeNum = totalNodesLinks.count()
    // get follow list like (1,(2,3))
    val followLinks = lines.map(word =>(word.split("\t")(0), word.split("\t")(1))).distinct().groupByKey().cache()
    // get all nodes in the follow list, the dangling nodes is not included
    val followNodes = followLinks.keys
    // get all the dangling nodes
    val DNodes = allkeys.subtract(followNodes).collect()
    // store the dangling nodes in the list, with empty value for each node
    var DNodesList = for (i <- DNodes) yield (i, List.empty)
    // add dangling nodes to the follow list
    val allLinks = followLinks ++ sc.parallelize(DNodesList)
    // set all the node inital rank as one
    var ranks = allLinks.mapValues(v => 1.0)

  // calculate ranks for each nodes and iterate for 10 times
  for (i <- 1 to 10) {
    // danglingNode is to store the total rank value of all dangling nodes after each iteration
    val danglingNode = sc.accumulator(0.0)
      // calculate the rank value for each node before taking into account of dangling nodes  
      val contribution = allLinks.join(ranks).values.flatMap{ case (follows, rank) =>
        val size = follows.size
        // if the node is dangling node, calculate culmulative rank
        // if the node is not dangling node, calculate rank for each node in the follows map
        if (size == 0) {  
          danglingNode += rank  
          List() 
          } else {
            follows.map(follows => (follows, rank / size))
          }
        }
        contribution.count()
      // get the value of total rank of all Dangling nodes
      val totalDangling = danglingNode.value
      // add the total dangling ranks to the previous contribution map
      ranks = contribution.reduceByKey(_ + _).mapValues[Double](value =>
        0.15 + 0.85 * (totalDangling/ nodeNum + value)
        )
    }
    // reformat the output
    val output = ranks.map(word => (word._1 + "\t" + word._2))
    output.saveAsTextFile("hdfs:///pagerank-output")
  }
}
// scalastyle:on println
