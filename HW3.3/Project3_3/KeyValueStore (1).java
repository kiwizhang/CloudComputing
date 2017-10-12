import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;


public class KeyValueStore extends Verticle {
	
	private ConcurrentHashMap<String, Dataunit> dataStoreMap = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, PriorityQueue<Long>> timeStampMap = new ConcurrentHashMap<>();
	
	public class Dataunit {
		String value; 
		Long timestamp;
		
		
		public Dataunit (String value, Long timestamp){
			this.value = value;
			this.timestamp = timestamp;
				
		}
		public String getValue() {
			return value;
		}
		public Long getTimestamp() {
			return timestamp;
		}
	}
	//The method to acquire lock when the timestamp at the top of the queue does not equal the 
	//timestamp of the current request
	//only process request until they are equal
	public static void lockqueue (PriorityQueue<Long> timestampQueue, Long timestamp) {
		
		synchronized(timestampQueue) {
			while (timestampQueue.isEmpty() ||!timestamp.equals(timestampQueue.peek())) {
				try {
					System.out.println("waiting timestamp: " + timestamp );
					timestampQueue.wait();
				} catch (InterruptedException Exception) {
					Exception.printStackTrace();
				}
				
			}
		}
		
	}
	

	/* TODO: Add code to implement your backend storage */

	@Override
	public void start() {
		final KeyValueStore keyValueStore = new KeyValueStore();
		final RouteMatcher routeMatcher = new RouteMatcher();
		final HttpServer server = vertx.createHttpServer();
		server.setAcceptBacklog(32767);
		server.setUsePooledBuffers(true);
		server.setReceiveBufferSize(4 * 1024);
		routeMatcher.get("/put", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				System.out.println("put request comes");
				MultiMap map = req.params();
				String key = map.get("key");
				String value = map.get("value");
				String consistency = map.get("consistency");
				Integer region = Integer.parseInt(map.get("region"));

				Long timestamp = Long.parseLong(map.get("timestamp"));

				/* TODO: Add code here to handle the put request
					 Remember to use the explicit timestamp if needed! */
				Thread t = new Thread(new Runnable() {
					public void run() {
						if (consistency.equals("strong")) {
							//strong consistency test
							System.out.println("enter lockqueue");
							//only process the request when the timestamp at the top of the queue equals the 
							//timestamp of the current request. Otherwise the thread will wait until they are equal
							lockqueue(timeStampMap.get(key), timestamp);
							System.out.println("puting key:" + key + "with tmp: " + timestamp);
							//store the data into the ConcurrentHashMap
							dataStoreMap.put(key, new Dataunit(value, timestamp));
						} else {
							//eventual consistency test
							if (!dataStoreMap.containsKey(key) || timestamp > dataStoreMap.get(key).getTimestamp()) {
								dataStoreMap.put(key, new Dataunit(value, timestamp));
							}

							
						}
						
						
						String response = "stored";
						req.response().putHeader("Content-Type", "text/plain");
						req.response().putHeader("Content-Length",
								String.valueOf(response.length()));
						req.response().end(response);
						req.response().close();
					}
				});
				t.start();
			}
		});
		
		
			
		
		routeMatcher.get("/get", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				System.out.println("get request comes");
				MultiMap map = req.params();
				final String key = map.get("key");
				String consistency = map.get("consistency");
				final Long timestamp = Long.parseLong(map.get("timestamp"));

				/* TODO: Add code here to handle the get request
					 Remember that you may need to do some locking for this */
				Thread t = new Thread(new Runnable() {
					public void run() {
						String response = null;

						if (consistency.equals("strong")) {
							//strong consistency test:
							PriorityQueue<Long> timequeue = timeStampMap.get(key);						
							
							synchronized(timequeue) {
								//add the timestamp of the get request to the top of the queue
								timequeue.offer(timestamp);
								//only continue the thead when the timestamp at the top of the queue equals timestamp of the current request
								while (timequeue.isEmpty() ||!timestamp.equals(timequeue.peek())) {
									try {
										System.out.println("waiting get timestamp: " + timestamp + "key: " + key);
										timequeue.wait();
									} catch (InterruptedException Exception) {
										Exception.printStackTrace();
									}
		
								}
								response = dataStoreMap.get(key).getValue();
								//after the getting the value from the database, poll the timestamp out of the queue
								timequeue.poll();
								timequeue.notifyAll();
								System.out.println("key: " + key + " get notifyAll called!");
							}

						} else {
							//eventual consistency test
							if (dataStoreMap.get(key) != null) {
								response = dataStoreMap.get(key).getValue();
							} else {
								response = "";
							}
						}
			
						req.response().putHeader("Content-Type", "text/plain");
						if (response != null)
							req.response().putHeader("Content-Length",
									String.valueOf(response.length()));
						req.response().end(response);
						req.response().close();
					}
				});
				t.start();

			}
		});
		// Clears this stored keys. Do not change this
		routeMatcher.get("/reset", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				/* TODO: Add code to here to flush your datastore. This is MANDATORY */
				//create a new ConcurrentHashMap
				dataStoreMap = new ConcurrentHashMap<String, Dataunit>();
				req.response().putHeader("Content-Type", "text/plain");
				req.response().end();
				req.response().close();
			}
		});
		// Handler for when the AHEAD is called
		routeMatcher.get("/ahead", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				System.out.println("ahead request comes");

				MultiMap map = req.params();
				String key = map.get("key");
				final Long timestamp = Long.parseLong(map.get("timestamp"));
				//when ahead request is received, put the timestamp received to the queue
				synchronized(timeStampMap) {
					System.out.println("synchronizing timestampMap");
					if (!timeStampMap.containsKey(key)) {
						PriorityQueue<Long> timeStampQueue = new PriorityQueue<Long>(15, new Comparator<Long>() {
							@Override
							public int compare(Long o1, Long o2) {
								return o1.compareTo(o2);					
							}
						});
						timeStampMap.put(key, timeStampQueue);
					}
					
				}
				PriorityQueue<Long> timequeue = timeStampMap.get(key);
				synchronized(timequeue) {
					System.out.println("offering key " + key + "timestamp: " + timestamp);

					timequeue.offer(timestamp);
				}
				

				
				req.response().putHeader("Content-Type", "text/plain");
				req.response().end();
				req.response().close();
			}
		});
		// Handler for when the COMPLETE is called
		routeMatcher.get("/complete", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				System.out.println("complete request comes");

				MultiMap map = req.params();
				String key = map.get("key");
				final Long timestamp = Long.parseLong(map.get("timestamp"));
				/* TODO: Add code to handle the signal here if you wish */
				//when complete request is received, poll the timestamp out of the queue
				PriorityQueue<Long> timequeue = timeStampMap.get(key);
				synchronized(timequeue) {
					System.out.println("popping key: " + key + "with tmp:" + timestamp);
					if (!timequeue.isEmpty()) {
						timequeue.poll();
						timequeue.notifyAll();
						System.out.println("notifyAll called!");

					}
					
				}
				req.response().putHeader("Content-Type", "text/plain");
				req.response().end();
				req.response().close();
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
	
