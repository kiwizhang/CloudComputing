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

/**
 * The class for data query in HBase
 * 
 */

public class HBaseTasks {

    /**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.9.143";
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
     * the method to get data from Hbase. 
     */

    public static List<String> getTweet(String userid, String hashtag) throws IOException {
    	
    	if (tweetsTable == null || conn == null) {
    		initializeConnection();
        }
        return demo(userid, hashtag);
    }
    /**
     * This is a method to use "get" operation to query from HBase
     * It pass in the key and return the value from HBase
     * @throws IOException
     */
    private static List<String> demo(String userid, String hashtag) throws IOException {
    	
        List<String> tweets = new ArrayList<String>();
        String rowkey = userid + "&" + hashtag;
        Get get = new Get(rowkey.getBytes());
        Result rs = tweetsTable.get(get);
        for(KeyValue kv : rs.raw()){

	    tweets.add(Bytes.toString(kv.getValue()).replace("\\n", ";"));
        }
                
        return tweets;
    }

   
    

}
