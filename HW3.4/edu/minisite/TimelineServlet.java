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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import cc.cmu.edu.minisite.ProfileServlet;
public class TimelineServlet extends HttpServlet {
	
	
	/**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.38.227";
    /**
     * The name of your HBase table.
     */
    private static String follower_tableName = "firstfinallinks";
    private static String followee_tableName = "finalreverselinks";
    

    /**
     * HTable handler.
     */
    private static HTableInterface followerTable;
    private static HTableInterface followeeTable;

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
	MongoClient mongoClient;
	static MongoDatabase database;


    public TimelineServlet() throws Exception {
        /*
            Your initialization code goes here
        */
    	innitializeMySQLConnection();
    	initializeHBaseConnection();
    	mongoClient = new MongoClient("ec2-52-90-129-242.compute-1.amazonaws.com");
        database = mongoClient.getDatabase("post");

    	
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
      
        followerTable = hconn.getTable(Bytes.toBytes(follower_tableName));
        followeeTable = hconn.getTable(Bytes.toBytes(followee_tableName));

        } catch (ZooKeeperConnectionException e) {
        	e.printStackTrace();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }

    @Override
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException {

        JSONObject result = new JSONObject();
        String id = request.getParameter("id");

        /*
            Task 4 (1):
            Get the name and profile of the user as you did in Task 1
            Put them as fields in the result JSON object
        */
        
        result.put("name", getUserNameandUrl(id)[0]);
        result.put("profile", getUserNameandUrl(id)[1]);

        /*
            Task 4 (2);
            Get the follower name and profiles as you did in Task 2
            Put them in the result JSON object as one array
        */
        try {
        	result.put("followers", query(id));
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        /*
            Task 4 (3):
            Get the 30 LATEST followee posts and put them in the
            result JSON object as one array.

            The posts should be sorted:
            First in ascending timestamp order
            Then numerically in ascending order by their PID (PostID) 
	    if there is a tie on timestamp
        */
        PriorityQueue<JSONObject> postqueue = new PriorityQueue<JSONObject> (10, new Comparator<JSONObject>() {
    		@Override
    		public int compare(JSONObject pair1, JSONObject pair2) {
    		
    			return pair2.getString("timestamp").compareTo(pair1.getString("timestamp"));
    		}
     	});
        
        PriorityQueue<JSONObject> finalpostqueue = new PriorityQueue<JSONObject> (10, new Comparator<JSONObject>() {
    		@Override
    		public int compare(JSONObject pair1, JSONObject pair2) {
    		
    			return pair2.getString("timestamp").compareTo(pair1.getString("timestamp"));
    		}
     	});
        try {
        	List<String> followeeList = getFollowees(id);
            for (String followeeid: followeeList) {
            	List<JSONObject> followeePosts = getPost(followeeid);
            	for (JSONObject followeePost: followeePosts) {
            		postqueue.add(followeePost);
            	}
            }    
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        while (!postqueue.isEmpty()) {
        	finalpostqueue.add(postqueue.poll());
        }

    	JSONArray followeePost = new JSONArray();
        for (int i = 0; i < 30; i++) {
        	followeePost.put(finalpostqueue.poll());
        }
        result.put("posts", followeePost);
        
        System.out.println("got all posts");
        System.out.println("result:" + result);

        
        
        
        
        PrintWriter out = response.getWriter();
        out.print(String.format("returnRes(%s)", result.toString()));
        out.close();
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
    
    private static String[] getUserNameandUrl(String userid) {
   	 Statement st1 = null;
        String sql1 = null;
        ResultSet rs1 = null;
        String[] returnArray = new String[2];
        
        try {
        	System.out.println("in mysql");
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
    
	
	public static JSONArray query(String userid) throws Exception{
		
		PriorityQueue<JSONObject> queue = new PriorityQueue<JSONObject> (10, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject pair1, JSONObject pair2) {
				if (pair1.getString("name").equals(pair2.getString("name"))) {
					return pair1.getString("profile").compareTo(pair2.getString("profile"));
				}
				return pair1.getString("name").compareTo(pair2.getString("name"));
			}
	 	});
		String followerstring = null;
		JSONArray followers = new JSONArray();
		
    	System.out.println("scan hbase");

		Scan scan = new Scan();
	    byte[] bCol1 = Bytes.toBytes("userid");
	    byte[] bCol2 = Bytes.toBytes("follower");
	
	    scan.addColumn(bColFamily, bCol1);
	    scan.addColumn(bColFamily, bCol2);
	   
	

	    
	    Get get = new Get(userid.getBytes());
        Result rs = followerTable.get(get);
        for(KeyValue kv : rs.raw()){
            followerstring = new String(kv.getValue());
        }
        String[] tempList = followerstring.split(",");
        for (String followerid: tempList) {
        	
	    	
	    	System.out.println("hbase rs loop");

	    	String followername = getUserNameandUrl(followerid)[0];
	    	String followerurl = getUserNameandUrl(followerid)[1];
	    
	    	JSONObject follower = new JSONObject();
	    	follower.put("name", followername);
	    	follower.put("profile", followerurl);
	    	

	
	    	queue.add(follower);

	    
	    
	    }
	    while (!queue.isEmpty()) {
	    	followers.put(queue.poll());
	    }
    	System.out.println("got follower");

	    return followers;

	}
	
public static List<String> getFollowees(String userid) throws Exception{
    	
		List<String> followeeList = new ArrayList<String>();
		String followees = null;
    	Scan scan = new Scan();
        byte[] bCol1 = Bytes.toBytes("userid");
        byte[] bCol2 = Bytes.toBytes("followee");

        scan.addColumn(bColFamily, bCol1);
        scan.addColumn(bColFamily, bCol2);
       
    
        Filter filter = new SingleColumnValueFilter(bColFamily, bCol1, CompareFilter.CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes(userid)));
        
        Get get = new Get(userid.getBytes());
        Result rs = followeeTable.get(get);
        for(KeyValue kv : rs.raw()){
            followees = new String(kv.getValue());
        }
        String[] tempList = followees.split(",");
        for (String followee: tempList) {
        	followeeList.add(followee);
        }
    	System.out.println("got followees");

        return followeeList;
    }

public static List<JSONObject> getPost(String userid) {



    List<JSONObject> postList = new ArrayList<JSONObject>();
    FindIterable<Document> iterable = database.getCollection("posts").find(
            new Document("uid", Integer.parseInt(userid)));
    
    for (Document doc : iterable) {
    	JSONObject singlePost = new JSONObject();
    	singlePost.put("content", doc.get("content"));
    	singlePost.put("timestamp", doc.get("timestamp"));
    	singlePost.put("uid", doc.get("uid"));
    	singlePost.put("name", doc.get("name"));
    	singlePost.put("image", doc.get("image"));
    	singlePost.put("pid", doc.get("pid"));
    	singlePost.put("comments", doc.get("comments"));
    	singlePost.put("profile", doc.get("profile"));
    	postList.add(singlePost);
    	
    }
	System.out.println("get postList ");

    return postList;
}
}

