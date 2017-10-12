import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.sql.Timestamp;
import java.util.Comparator;


import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class Coordinator extends Verticle {

	//Default mode: sharding. Possible string values are "replication" and "sharding"
	private static String storageType = "replication";

	/**
	 * TODO: Set the values of the following variables to the DNS names of your
	 * three dataCenter instances
	 */
	private static final String dataCenter1 = "ec2-54-173-11-182.compute-1.amazonaws.com";
	private static final String dataCenter2 = "ec2-54-210-54-172.compute-1.amazonaws.com";
	private static final String dataCenter3 = "ec2-54-236-208-196.compute-1.amazonaws.com";
	private HashMap<String, String> dataCenterMap = new HashMap<String, String>();
	private HashMap<String, PriorityQueue> keyMap = new HashMap<>();
	
	public class Item {
		String value;
		String timeStamp;
		String type;
		public Item (String value, String timeStamp, String type) {
			this.value = value;
			this.timeStamp = timeStamp;
			this.type = type;
			
		}
		public String getValue() {
			return value;
		}
		public String getTimestamp(){
			return timeStamp;
		}
		public String getType() {
			return type;
		}
	}
	

	@Override
	public void start() {
		//DO NOT MODIFY THIS
		KeyValueLib.dataCenters.put(dataCenter1, 1);
		KeyValueLib.dataCenters.put(dataCenter2, 2);
		KeyValueLib.dataCenters.put(dataCenter3, 3);
		dataCenterMap.put("1", dataCenter1);
		dataCenterMap.put("2", dataCenter2);
		dataCenterMap.put("3", dataCenter3);
	
		final RouteMatcher routeMatcher = new RouteMatcher();
		final HttpServer server = vertx.createHttpServer();
		server.setAcceptBacklog(32767);
		server.setUsePooledBuffers(true);
		server.setReceiveBufferSize(4 * 1024);

		routeMatcher.get("/put", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				MultiMap map = req.params();
				final String key = map.get("key");
				final String value = map.get("value");
				//You may use the following timestamp for ordering requests
                final String timestamp = new Timestamp(System.currentTimeMillis() 
                                                                + TimeZone.getTimeZone("EST").getRawOffset()).toString();
				Item item = new Item(value, timestamp, "put");
				
				if (keyMap.containsKey(key)) {
					keyMap.get(key).offer(item); 			
				} else {
					PriorityQueue<Item> keyQueue = new PriorityQueue<Item>(100, new Comparator<Item>() {
	                	@Override
	                	public int compare(Item o1, Item o2) {
	                		if (o1.getTimestamp().equals(o2.getTimestamp())) {
	                			return 0;
	                		}
	                		return Double.parseDouble(o1.getTimestamp()) > Double.parseDouble(o2.getTimestamp()) ? 1 : -1;
	                		
	                	}
	                });
					keyQueue.offer(item);
					keyMap.put(key,keyQueue);
				}
				
				PriorityQueue<Item> queue = keyMap.get(key);
				String recentValue = queue.peek().getValue();
				while (!queue.isEmpty() && queue.peek().getType().equals("put")) {
					Thread t = new Thread(new Runnable() {
						public void run() {
							//TODO: Write code for PUT operation here.
							//Each PUT operation is handled in a different thread.
							//Highly recommended that you make use of helper functions.
							
							synchronized(queue) {
								try {
									KeyValueLib.PUT(dataCenter1, key, recentValue);
									KeyValueLib.PUT(dataCenter2, key, recentValue);
									KeyValueLib.PUT(dataCenter3, key, recentValue);
									queue.poll();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							

						}
					});
					t.start();
					req.response().end(); //Do not remove this
				}
                
			}
		});

		routeMatcher.get("/get", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				MultiMap map = req.params();
				final String key = map.get("key");
				final String loc = map.get("loc");
				//You may use the following timestamp for ordering requests
				final String timestamp = new Timestamp(System.currentTimeMillis() 
								+ TimeZone.getTimeZone("EST").getRawOffset()).toString();
				
				Item item = new Item(loc, timestamp, "get");
				
				if (keyMap.containsKey(key)) {
					keyMap.get(key).offer(item); 			
				} else {
					PriorityQueue<Item> keyQueue = new PriorityQueue<Item>(100, new Comparator<Item>() {
	                	@Override
	                	public int compare(Item o1, Item o2) {
	                		if (o1.getTimestamp().equals(o2.getTimestamp())) {
	                			return 0;
	                		}
	                		return Double.parseDouble(o1.getTimestamp()) > Double.parseDouble(o2.getTimestamp()) ? 1 : -1;
	                		
	                	}
	                });
					keyQueue.offer(item);
					keyMap.put(key,keyQueue);
				}
				
				PriorityQueue<Item> queue = keyMap.get(key);
			
				while (!queue.isEmpty() && queue.peek().getType().equals("get")) {
					Thread t = new Thread(new Runnable() {
						public void run() {
							//TODO: Write code for GET operation here.
	                                                //Each GET operation is handled in a different thread.
	                                                //Highly recommended that you make use of helper functions.
							String returnValue = "0";
							synchronized(key) {
								try {
									returnValue = KeyValueLib.GET(dataCenterMap.get(loc), key);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								req.response().end(returnValue); //Default response = 0
							}
						}
					});
					t.start();
				}

				
				
			}
		});

		routeMatcher.get("/storage", new Handler<HttpServerRequest>() {
                        @Override
                        public void handle(final HttpServerRequest req) {
                                MultiMap map = req.params();
                                storageType = map.get("storage");
                                //This endpoint will be used by the auto-grader to set the 
				//consistency type that your key-value store has to support.
                                //You can initialize/re-initialize the required data structures here
                                req.response().end();
                        }
                });

		routeMatcher.noMatch(new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				req.response().putHeader("Content-Type", "text/html");
				String response = "Not found.";
				req.response().putHeader("Content-Length",
						String.valueOf(response.length()));
				req.response().end(response);
				req.response().close();
			}
		});
		server.requestHandler(routeMatcher);
		server.listen(8080);
	}
}
