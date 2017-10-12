package cc.cmu.edu.minisite;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;


public class FollowerServlet extends HttpServlet {
	
	/**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.38.227";
    /**
     * The name of your HBase table.
     */
    private static String tableName = "newlinks";
    

    /**
     * HTable handler.
     */
    private static HTableInterface followerTable;
    /**
     * HBase connection.
     */
    private static HConnection hconn;
    /**
     * Byte representation of column family.
     */
    private static byte[] bColFamily = Bytes.toBytes("data");
    /**
     * Logger.
     */
    private final static Logger logger = Logger.getRootLogger();
    private final static Configuration conf = HBaseConfiguration.create();

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_NAME = "flykiwi";
    private static final String URL = "jdbc:mysql://flykiwi.clnmtgvkqmer.us-east-1.rds.amazonaws.com/" + DB_NAME;

    private static final String DB_USER = "flykiwi";
    private static final String DB_PWD = "painting";
    private static Connection conn = null;


    public class Pair {
    	private String username;
    	private String url;
    	public Pair(String username, String url) {
    		this.username = username;
    		this.url = url;
    	}
    	public String getUsername() {
    		return username;
    	}
    	public String getUrl() {
    		return url;
    	}
    }


    public FollowerServlet() {
        /*
            Your initialization code goes here
        */
    	initializeHBaseConnection();
    	innitializeMySQLConnection();
    	
    	
    }
    
    private static void innitializeMySQLConnection() {
    	try {
        	Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
    	} catch (ClassNotFoundException e) { 
         	e.printStackTrace();
         } catch (SQLException e) {
        	 e.printStackTrace();
         }

    }
    
    /**
     * Initialize HBase connection.
     */
    private static void initializeHBaseConnection() {
    try {
        logger.setLevel(Level.ERROR);
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.master", zkAddr + ":60000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");
	    if (!zkAddr.matches("\\d+.\\d+.\\d+.\\d+")) {
		    System.out.print("HBase not configured!");
		    return;
	    }
        hconn = HConnectionManager.createConnection(conf);
      
        followerTable = hconn.getTable(Bytes.toBytes(tableName));
        } catch (ZooKeeperConnectionException e) {
        	e.printStackTrace();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }

    	
    	
    	
    public static String query(String userid) throws Exception{
    	
    	PriorityQueue<JSONObject> queue = new PriorityQueue<JSONObject> (10, new Comparator<JSONObject>() {
    		@Override
    		public int compare(JSONObject pair1, JSONObject pair2) {
    			if (pair1.getString("name").equals(pair2.getString("name"))) {
    				return pair1.getString("profile").compareTo(pair2.getString("profile"));
    			}
    			return pair1.getString("name").compareTo(pair2.getString("name"));
    		}
     	});
    	
    	JSONObject finalresult = new JSONObject();

    	JSONArray followers = new JSONArray();
    	
    	ResultScanner rs = null;
    	
    	Scan scan = new Scan();
        byte[] bCol1 = Bytes.toBytes("userid");
        byte[] bCol2 = Bytes.toBytes("follower");

        scan.addColumn(bColFamily, bCol1);
        scan.addColumn(bColFamily, bCol2);
       
    
        Filter filter = new SingleColumnValueFilter(bColFamily, bCol1, CompareFilter.CompareOp.EQUAL, Bytes.toBytes(userid));
        
        scan.setFilter(filter);
        rs = followerTable.getScanner(scan);
        for (Result r = rs.next(); r != null; r = rs.next()) {
        	
        	String followerid = Bytes.toString(r.getValue(bColFamily, bCol2));
        	String followername = getUserNameandUrl(followerid)[0];
        	String followerurl = getUserNameandUrl(followerid)[1];
        
        	JSONObject follower = new JSONObject();
        	follower.put("name", followername);
        	follower.put("profile", followerurl);
            System.out.println("follower: " + follower.toString());

        	queue.add(follower);
//        	followers.put(follower);
            System.out.println("followername: " + followername);
            System.out.println("profile: " + followerurl);

        }
//        System.out.println("Scan finished. " + count + " match(es) found.");
        while (!queue.isEmpty()) {
        	System.out.println("follower " + queue.poll());
        	followers.put(queue.poll());
        }
        finalresult.put("followers", followers);
        rs.close();
        return finalresult.toString();
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");
       

        /*
            Task 2:
            Implement your logic to retrive the followers of this user. 
            You need to send back the Name and Profile Image URL of his/her Followers.

            You should sort the followers alphabetically in ascending order by Name. 
            If there is a tie in the followers name, 
	    sort alphabetically by their Profile Image URL in ascending order. 
        */
        
      

        PrintWriter writer = response.getWriter();
        try {
        	System.out.println(query(id));
            writer.write(String.format("returnRes(%s)", query(id)));
        } catch (Exception e) {
        	e.printStackTrace();
        }
        writer.close();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    } 
    
    private static String[] getUserNameandUrl(String userid) {
    	 Statement st1 = null;
         String sql1 = null;
         ResultSet rs1 = null;
         String[] returnArray = new String[2];
         
         try {
         	 Class.forName(JDBC_DRIVER);
              conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
             
             if (userid != null) {
             	st1 = conn.createStatement();
                 sql1 =  "Select * from userprofile Where UserId='" + userid + "'";
                 rs1 = st1.executeQuery(sql1);
                 while (rs1.next()) {
                     String username = rs1.getString("UserName");
                     String url = rs1.getString("url");
                     returnArray[0] = username;
                     returnArray[1] = url;
                 }
                     
                 	
              } else {
                     System.out.println("This user does not exist in userprofile table.");
              }
         }catch (SQLException e) {
             e.printStackTrace();
         }catch (ClassNotFoundException e) { 
           	e.printStackTrace();
          } finally {
             if (st1 != null) {
                 try {
                     st1.close();
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
             if (conn != null) {
                 try {
                     conn.close();
                 } catch (SQLException e) { /* ignored */}
             }
             
         }
         return returnArray;
    }
    
}


