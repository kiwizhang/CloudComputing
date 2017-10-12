package cc.cmu.edu.minisite;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.json.JSONArray;

public class HomepageServlet extends HttpServlet {
	
	MongoClient mongoClient;


    public HomepageServlet() {
        /*
            Your initialization code goes here
        */
    	initializeMongoDB();
    }
    
    public void initializeMongoDB() {
    	mongoClient = new MongoClient("ec2-52-90-129-242.compute-1.amazonaws.com");
    }
    


    @Override
    protected void doGet(final HttpServletRequest request, 
            final HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("id");
        
        MongoDatabase database = mongoClient.getDatabase("post");
        
//        MongoCollection<Document> coll = db.getCollection("post");
//        BasicDBObject query = new BasicDBObject("uid", Integer.parseInt(id));
//        
//        FindIterable<Document> cursor = coll.find(query);
//
//        try {
//            while (cursor.hasNext()) {
//                System.out.println(cursor.next());
//            }
//        } finally {
//            cursor.close();
//        }

        	
        	PriorityQueue<Document> queue = new PriorityQueue<Document> (10, new Comparator<Document>() {
        		@Override
        		public int compare(Document pair1, Document pair2) {
        		
        			return pair1.getString("timestamp").compareTo(pair2.getString("timestamp"));
        		}
         	});
    
        
        FindIterable<Document> iterable = database.getCollection("posts").find(
                new Document("uid", Integer.parseInt(id)));
        
        for (Document current : iterable) {
        	queue.add(current);
        }
        
        JSONObject finalresult = new JSONObject();
        JSONArray postArray = new JSONArray();
        
        while (!queue.isEmpty()) {
        	Document doc = queue.poll();
        	JSONObject singlePost = new JSONObject();
        	singlePost.put("content", doc.get("content"));
        	singlePost.put("timestamp", doc.get("timestamp"));
        	singlePost.put("uid", doc.get("uid"));
        	singlePost.put("name", doc.get("name"));
        	singlePost.put("image", doc.get("image"));
        	singlePost.put("pid", doc.get("pid"));
        	singlePost.put("comments", doc.get("comments"));
        	singlePost.put("profile", doc.get("profile"));
        	postArray.put(singlePost);
        	finalresult.put("posts", postArray);
        	
        }
        

        PrintWriter writer = response.getWriter();           
        writer.write(String.format("returnRes(%s)", finalresult.toString()));
        writer.close();
    }

    @Override
    protected void doPost(final HttpServletRequest request, 
            final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}

