import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
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

public class HBaseTasks {

    /**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.0.120";
    /**
     * The name of your HBase table.
     */
    private static String tableName = "songdata";
    /**
     * HTable handler.
     */
    private static HTableInterface songsTable;
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
        songsTable = conn.getTable(Bytes.toBytes(tableName));
    }

    /**
     * Clean up resources.
     * @throws IOException
     */
    private static void cleanup() throws IOException {
        if (songsTable != null) {
            songsTable.close();
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
    public static void main(String[] args) throws IOException {
        initializeConnection();
        switch (args[0]) {
            case "demo":
                demo();
                break;
            case "q17":
                q17();
                break;
            case "q18":
                q18();
                break;
            case "q19":
                q19();
                break;
            case "q20":
                q20();
                break;
            case "q21":
                q21();
        }
        cleanup();
    }

    /**
     * This is a demo of how to use HBase Java API. It will print all the artist_names starting with "The Beatles".
     * @throws IOException
     */
    private static void demo() throws IOException {
        Scan scan = new Scan();
        byte[] bCol = Bytes.toBytes("artist_name");
        scan.addColumn(bColFamily, bCol);
        RegexStringComparator comp = new RegexStringComparator("^The Beatles.*");
        Filter filter = new SingleColumnValueFilter(bColFamily, bCol, CompareFilter.CompareOp.EQUAL, comp);
        scan.setFilter(filter);
        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);
        int count = 0;
        for (Result r = rs.next(); r != null; r = rs.next()) {
            count ++;
            System.out.println(Bytes.toString(r.getValue(bColFamily, bCol)));
        }
        System.out.println("Scan finished. " + count + " match(es) found.");
        rs.close();
    }

    /**
     * Question 17.
     *
     * What was that song whose name started with "Total" and ended with "Water"?
     * Write an HBase query that finds the track that the person is looking for.
     * The title starts with "Total" and ends with "Water", both are case sensitive.
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q17() throws IOException{
    	Scan scan = new Scan();
        byte[] bCol = Bytes.toBytes("title");
        scan.addColumn(bColFamily, bCol);
        RegexStringComparator comp = new RegexStringComparator("^Total.*Water$");
        Filter filter = new SingleColumnValueFilter(bColFamily, bCol, CompareFilter.CompareOp.EQUAL, comp);
        scan.setFilter(filter);
        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);
        int count = 0;
        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily, bCol)));
        }
//        System.out.println("Scan finished. " + count + " match(es) found.");
        rs.close();

    }

    /**
     * Question 18.
     *
     * I don't remember the exact title, it was that song by "Kanye West", and the
     * title started with either "Apologies" or "Confessions". Not sure which...
     * Write an HBase query that finds the track that the person is looking for.
     * The artist_name contains "Kanye West", and the title starts with either
     * "Apologies" or "Confessions" (Case sensitive).
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q18() throws IOException{
    	Scan scan = new Scan();
        byte[] bCol1 = Bytes.toBytes("title");
        byte[] bCol2 = Bytes.toBytes("artist_name");

        scan.addColumn(bColFamily, bCol1);
        scan.addColumn(bColFamily, bCol2);
    	RegexStringComparator comp2 = new RegexStringComparator(".*Kanye West.*"); 
    	RegexStringComparator comp1 = new RegexStringComparator("^(Apologies|Confessions).*"); 
    
        Filter filter1 = new SingleColumnValueFilter(bColFamily, bCol1, CompareFilter.CompareOp.EQUAL, comp1);
        Filter filter2 = new SingleColumnValueFilter(bColFamily, bCol2, CompareFilter.CompareOp.EQUAL, comp2);
        List<Filter> listOfFilter = new ArrayList<>();
        listOfFilter.add(filter1);
        listOfFilter.add(filter2);
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL, listOfFilter);
        scan.setFilter(filterList);
        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);
        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily, bCol1)));
        }
//        System.out.println("Scan finished. " + count + " match(es) found.");
        rs.close();

    }

    /**
     * Question 19.
     *
     * There was that new track by "Bob Marley" that was really long. Do you know?
     * Write an HBase query that finds the track the person is looking for.
     * The artist_name has a prefix of "Bob Marley", duration no less than 400,
     * and year 2000 and onwards (Case sensitive).
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q19() throws IOException{
    	Scan scan = new Scan();
        byte[] bCol1 = Bytes.toBytes("title");
        byte[] bCol2 = Bytes.toBytes("artist_name");
        byte[] bCol3 = Bytes.toBytes("duration");
        byte[] bCol4 = Bytes.toBytes("year");


        scan.addColumn(bColFamily, bCol1);
        scan.addColumn(bColFamily, bCol2);
        scan.addColumn(bColFamily, bCol3);
        scan.addColumn(bColFamily, bCol4);
    	RegexStringComparator comp2 = new RegexStringComparator("^Bob Marley.*"); 
    	BinaryComparator comp3 = new BinaryComparator(Bytes.toBytes("400")); 
    	BinaryComparator comp4 = new BinaryComparator(Bytes.toBytes("2000")); 

    
        Filter filter2 = new SingleColumnValueFilter(bColFamily, bCol2, CompareFilter.CompareOp.EQUAL, comp2);
        Filter filter3 = new SingleColumnValueFilter(bColFamily, bCol3, CompareFilter.CompareOp.GREATER_OR_EQUAL, comp3);
        Filter filter4 = new SingleColumnValueFilter(bColFamily, bCol4, CompareFilter.CompareOp.GREATER_OR_EQUAL, comp4);
        List<Filter> listOfFilter = new ArrayList<>();
        listOfFilter.add(filter2);
        listOfFilter.add(filter3);
        listOfFilter.add(filter4);
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL, listOfFilter);


        scan.setFilter(filterList);
        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);
        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily, bCol1)));
        }
//        System.out.println("Scan finished. " + count + " match(es) found.");
        rs.close();
    }

    /**
     * Question 20.
     *
     * I heard a really great song about "Family" by this really cute singer,
     * I think his name was "Consequence" or something...
     * Write an HBase query that finds the track the person is looking for.
     * The track has an artist_hotttnesss of at least 1, and the artist_name
     * contains "Consequence". Also, the title contains "Family" (Case sensitive).
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q20() throws IOException{
    	Scan scan = new Scan();
        byte[] bCol1 = Bytes.toBytes("title");
        byte[] bCol2 = Bytes.toBytes("artist_name");
        byte[] bCol3 = Bytes.toBytes("artist_hotttnesss");


        scan.addColumn(bColFamily, bCol1);
        scan.addColumn(bColFamily, bCol2);
        scan.addColumn(bColFamily, bCol3);
    	RegexStringComparator comp1 = new RegexStringComparator(".*Family.*"); 
    	RegexStringComparator comp2 = new RegexStringComparator(".*Consequence.*"); 
    	BinaryComparator comp3 = new BinaryComparator(Bytes.toBytes("1")); 

    
        Filter filter1 = new SingleColumnValueFilter(bColFamily, bCol1, CompareFilter.CompareOp.EQUAL, comp1);
        Filter filter2 = new SingleColumnValueFilter(bColFamily, bCol2, CompareFilter.CompareOp.EQUAL, comp2);
        Filter filter3 = new SingleColumnValueFilter(bColFamily, bCol3, CompareFilter.CompareOp.GREATER_OR_EQUAL, comp3);
        List<Filter> listOfFilter = new ArrayList<>();
        listOfFilter.add(filter1);
        listOfFilter.add(filter2);
        listOfFilter.add(filter3);
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL, listOfFilter);

        scan.setFilter(filterList);
        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);
        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily, bCol1)));
        }
//        System.out.println("Scan finished. " + count + " match(es) found.");
        rs.close();

    }

    /**
     * Question 21.
     *
     * Hey what was that "Love" song that "Gwen Guthrie" came out with in 1990?
     * No, no, it wasn't the sad one, nothing "Bitter" or "Never"...
     * Write an HBase query that finds the track the person is looking for.
     * The track has an artist_name prefix of "Gwen Guthrie", the title contains "Love"
     * but does NOT contain "Bitter" or "Never", the year equals to 1990.
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q21() throws IOException{
    	Scan scan = new Scan();
        byte[] bCol1 = Bytes.toBytes("title");
        byte[] bCol2 = Bytes.toBytes("artist_name");
        byte[] bCol3 = Bytes.toBytes("year");


        scan.addColumn(bColFamily, bCol1);
        scan.addColumn(bColFamily, bCol2);
        scan.addColumn(bColFamily, bCol3);
        

    	RegexStringComparator comp1 = new RegexStringComparator(".*Love.*");
    	RegexStringComparator comp4 = new RegexStringComparator("^((?!Bitter).)*$"); 
    	RegexStringComparator comp5 = new RegexStringComparator("^((?!Never).)*$"); 

    	RegexStringComparator comp2 = new RegexStringComparator("^Gwen Guthrie.*"); 
    	BinaryComparator comp3 = new BinaryComparator(Bytes.toBytes("1990")); 

        Filter filter1 = new SingleColumnValueFilter(bColFamily, bCol1, CompareFilter.CompareOp.EQUAL, comp1);
        Filter filter4 = new SingleColumnValueFilter(bColFamily, bCol1, CompareFilter.CompareOp.EQUAL, comp4);
        Filter filter5 = new SingleColumnValueFilter(bColFamily, bCol1, CompareFilter.CompareOp.EQUAL, comp5);
        Filter filter2 = new SingleColumnValueFilter(bColFamily, bCol2, CompareFilter.CompareOp.EQUAL, comp2);
        Filter filter3 = new SingleColumnValueFilter(bColFamily, bCol3, CompareFilter.CompareOp.EQUAL, comp3);
        List<Filter> listOfFilter = new ArrayList<>();
        listOfFilter.add(filter1);
        listOfFilter.add(filter2);
        listOfFilter.add(filter3);
        listOfFilter.add(filter4);
        listOfFilter.add(filter5);
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL, listOfFilter);
        

        scan.setFilter(filterList);
        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);
        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily, bCol1)));
        }
//        System.out.println("Scan finished. " + count + " match(es) found.");
        rs.close();
    }

}
