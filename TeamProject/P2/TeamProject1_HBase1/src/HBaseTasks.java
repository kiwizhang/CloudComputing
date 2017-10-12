import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.BigDecimalColumnInterpreter;
import org.apache.hadoop.hbase.coprocessor.ColumnInterpreter;
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
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class for data query in HBase
 * 
 */

public class HBaseTasks {

    /**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.1.229"; 
    /**
     * The name of your HBase table.
     */
    private static String tableName = "hbaseschema";
    
    private static final byte[] TEST_TABLE = Bytes.toBytes(tableName); 
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
    private final static Configuration conf = HBaseConfiguration.create();



    /**
     * Initialize HBase connection.
     * @throws IOException
     */
    private static void initializeConnection() throws IOException {
        // Remember to set correct log level to avoid unnecessary output.
        System.out.println("before set up connecion:" + System.currentTimeMillis());
        logger.setLevel(Level.ERROR);
        conf.set("hbase.master", zkAddr + ":60000");
        System.out.println("finish step 1");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        System.out.println("finish step 2");

        conf.set("hbase.zookeeper.property.clientport", "2181");
        System.out.println("finish step 3");

        if (!zkAddr.matches("\\d+.\\d+.\\d+.\\d+")) {
            System.out.print("HBase not configured!");
            return;
        }
        System.out.println("finish step 4");

        conn = HConnectionManager.createConnection(conf);
        System.out.println("finish step 5");
        System.out.println(conn.toString());

        tweetsTable = conn.getTable(TEST_TABLE);
        System.out.println("finish step 6");

        System.out.println(tweetsTable);
        System.out.println("after set up connecion:" + System.currentTimeMillis());

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

    public static String getTweet(String startId, String stopId, String startDate, String stopDate, String[] wordList) throws Exception {
        
        if (tweetsTable == null || conn == null) {
            initializeConnection();
        }
        return query(startId, stopId, startDate, stopDate, wordList);
    }
    /**
     * This is a method to use "get" operation to query from HBase
     * It pass in the key and return the value from HBase
     * @throws IOException
     */
    
    public static String query(String startId, String stopId, String startDate, String stopDate, String[] wordList) throws Exception {
//        AggregationClient aClient = new AggregationClient(conf);
        Map<String, Integer> wordmap = new HashMap<String, Integer>();
        //System.out.println("old strop id: " + stopId);
        stopId = ((Long)(Long.parseLong(stopId) + 1)).toString();
        //System.out.println("new strop id: " + stopId);
        startId = convertId(startId);
        stopId = convertId(stopId);

        //System.out.println("start id, " + startId);
        //System.out.println("stop id, " + stopId);

        wordmap.put(wordList[0], 0);
        wordmap.put(wordList[1], 0);
        wordmap.put(wordList[2], 0);
        // System.out.println(wordList[0]);
        // System.out.println(wordList[1]);
        // System.out.println(wordList[2]);

        String startRowString = startId;
        String stopRowString = stopId;
        byte[] startRow = Bytes.toBytes(startRowString);
        byte[] stopRow = Bytes.toBytes(stopRowString);
        Scan scan = new Scan();
        
        scan.setStartRow(startRow);
        scan.setStopRow(stopRow);  
        //System.out.println(startRow.toString());
        //System.out.println(stopRow.toString());

        //System.out.println("finish setting start and stop row");

        byte[] bCol1 = Bytes.toBytes("t");
        
        //System.out.println("finish setting bCol");


        scan.addColumn(bColFamily, bCol1);
        
        ResultScanner rs = tweetsTable.getScanner(scan);

        for (Result r = rs.next(); r != null; r = rs.next()) {
            String str = Bytes.toString(r.getValue(bColFamily, bCol1));
            //System.out.println("one user result: " + str);
            String[] dateandwc = str.split("\t");
            //System.out.println("one user result: ");
            if (dateandwc[0].split("#")[0].compareTo(stopDate) > 0) continue;
            if (dateandwc[dateandwc.length-1].split("#")[0].compareTo(startDate) < 0) continue;
            for (String s : dateandwc) {
                //System.out.println(s);
                String[] onedaywc = s.split("#");
                // System.out.println(onedaywc[0]);
                // System.out.println(onedaywc[0].compareTo(startDate));
                // System.out.println(onedaywc[0].compareTo(stopDate));

                if (onedaywc[0].compareTo(startDate) >= 0 && onedaywc[0].compareTo(stopDate) <= 0) {
                    //System.out.println("this is a right date:" + onedaywc[0]);
                    String[] wordlist = onedaywc[1].split(" +");
                    for (String wordc : wordlist) {
                        //System.out.println(word);
                        String[] wandc = wordc.split(":");
                        for (String searchword : wordList) {
                            if (wandc[0].equals(searchword)) {
                                //System.out.println("get one: " + searchword);
                                wordmap.put(searchword, wordmap.get(searchword) + Integer.parseInt(wandc[1]));
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("finish rs");

        StringBuilder sb = new StringBuilder();
        sb.append(wordList[0] + ":" + wordmap.get(wordList[0]) + "\n").append(wordList[1] + ":" + wordmap.get(wordList[1]) + "\n").append(wordList[2] + ":" + wordmap.get(wordList[2]) + "\n");
        rs.close();
        return sb.toString();
    }

    public static String convertId(String id) {
        int size = id.length();
        if (size < 10) {
            for (int i = 0; i < 10-size; ++i) {
                id = "0" + id;
            }
        }
        return id;
    }
}
