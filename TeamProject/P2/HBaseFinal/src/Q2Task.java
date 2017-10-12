import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Q2Task {

    /**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.1.229";
    /**
     * The name of your HBase table.
     */
    private static String tableName = "hbase_input2";
    /**
     * HTable handler.
     */
    private static HTableInterface tweetsTable;
    /**
     * HBase connection.
     */
    private static HConnection conn;
    /**
     * Byte representation of column family.
     */
    private static byte[] bColFamily = Bytes.toBytes("data");
    /**
     * Logger.
     */
    private final static Logger logger = Logger.getRootLogger();


    /**
     * Initialize HBase connection.
     * @throws IOException
     */
    private static void initializeConnection() throws IOException {
        // Remember to set correct log level to avoid unnecessary output.
        logger.setLevel(Level.ERROR);
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.master", zkAddr + ":60000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");
	    if (!zkAddr.matches("\\d+.\\d+.\\d+.\\d+")) {
		    System.out.print("HBase not configured!");
		    return;
	    }
        conn = HConnectionManager.createConnection(conf);
        tweetsTable = conn.getTable(Bytes.toBytes(tableName));
    }

    /**
     * Clean up resources.
     * @throws IOException
     */
    private static void cleanup() throws IOException {
        if (tweetsTable != null) {
            tweetsTable.close();
        }
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * You should complete the missing parts in the following method. Feel free to add helper functions if necessary.
     *
     * For all questions, output your answer in ONE single line, i.e. use System.out.print().
     *
     * @param args The arguments for main method.
     */

    public static List<String> getTweet(String userid, String hashtag) throws IOException {
    	
    	if (tweetsTable == null || conn == null) {
    		initializeConnection();
        }
       System.out.println("innitianized:");

        return demo(userid, hashtag);
    }
    /**
     * This is a demo of how to use HBase Java API. It will print all the artist_names starting with "The Beatles".
     * @throws IOException
     */
    private static List<String> demo(String userid, String hashtag) throws IOException {
    	
        List<String> tweets = new ArrayList<String>();
        String rowkey = userid + "&" + hashtag;
        System.out.println("rowkey: " + rowkey);
        Get get = new Get(rowkey.getBytes());
        System.out.println("after get");
        Result rs = tweetsTable.get(get);
        System.out.println("after tweettable get");

        for(KeyValue kv : rs.raw()){
	    //System.out.println("binary" + kv.getValue());
            //String tmp = Bytes.toString(kv.getValue());
            //System.out.println(tmp);
	    //String[] list = tmp.split("\\n");
	    //StringBuilder sb = new StringBuilder();
	    //for (String s: list) {
	    //	sb.append(s);
//		sb.append("\n");
//   	    }
//	    tweets.add(sb.toString());
        System.out.println("in for loop");

	    tweets.add(Bytes.toString(kv.getValue()).replace("\\n", ";"));
        }
        System.out.println("tweets: " + tweets);

        return tweets;
    }

   
    

}
