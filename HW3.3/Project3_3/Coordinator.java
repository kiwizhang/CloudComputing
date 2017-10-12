import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;


public class Coordinator extends Verticle {

	// This integer variable tells you what region you are in
	// 1 for US-E, 2 for US-W, 3 for Singapore
	private static int region = KeyValueLib.region;

	// Default mode: Strongly consistent
	// Options: strong, eventual
	private static String consistencyType = "strong";

	/**
	 * TODO: Set the values of the following variables to the DNS names of your
	 * three dataCenter instances. Be sure to match the regions with their DNS!
	 * Do the same for the 3 Coordinators as well.
	 */
	private static final String dataCenterUSE = "ec2-54-175-28-78.compute-1.amazonaws.com";
	private static final String dataCenterUSW = "ec2-54-175-33-227.compute-1.amazonaws.com";
	private static final String dataCenterSING = "ec2-54-175-33-203.compute-1.amazonaws.com";

	private static final String coordinatorUSE = "ec2-54-175-33-12.compute-1.amazonaws.com";
	private static final String coordinatorUSW = "ec2-54-174-234-90.compute-1.amazonaws.com";
	private static final String coordinatorSING = "ec2-52-207-250-191.compute-1.amazonaws.com";
	private static String thisCoordinator;
	private static String thisDataCenter;


	
	
	public class Item {
		String value;
		Timestamp timestamp;
		String type;

		public Item(String value, Timestamp timestamp, String type) {
			this.value = value;
			this.timestamp = timestamp;
			this.type = type;

		}

		public String getValue() {
			return value;
		}

		public Timestamp getTimestamp() {
			return timestamp;
		}

		public String getType() {
			return type;
		}
	}
	
	//hash function to hash the key to a datastore
		private String hashFunction(String key) {
			if (key.equals("a") || key.matches(".*[A-Id-i1-3]$")) {
				return coordinatorUSE;
			}
			else if (key.equals("b") || key.matches(".*[J-Qj-q4-7]$")) {
				return coordinatorUSW;
			}
			return coordinatorSING;
		}

	@Override
	public void start() {
		KeyValueLib.dataCenters.put(dataCenterUSE, 1);
		KeyValueLib.dataCenters.put(dataCenterUSW, 2);
		KeyValueLib.dataCenters.put(dataCenterSING, 3);
		KeyValueLib.coordinators.put(coordinatorUSE, 1);
		KeyValueLib.coordinators.put(coordinatorUSW, 2);
		KeyValueLib.coordinators.put(coordinatorSING, 3);
		final RouteMatcher routeMatcher = new RouteMatcher();
		final HttpServer server = vertx.createHttpServer();
		server.setAcceptBacklog(32767);
		server.setUsePooledBuffers(true);
		server.setReceiveBufferSize(4 * 1024);
		if (region == 1) {
			thisCoordinator = coordinatorUSE;
			thisDataCenter = dataCenterUSE;
		} else if (region == 2) {
			thisCoordinator = coordinatorUSW;
			thisDataCenter = dataCenterUSW;
		} else {
			thisCoordinator = coordinatorSING;
			thisDataCenter = dataCenterSING;
		}

		routeMatcher.get("/put", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				MultiMap map = req.params();
				final String key = map.get("key");
				final String value = map.get("value");
				final Long timestamp = Long.parseLong(map.get("timestamp"));
				final String forwarded = map.get("forward");
				final String forwardedRegion = map.get("region");
				

				Thread t = new Thread(new Runnable() {
					public void run() {
					/* Handle PUT request
					 * Each operation is handled in a new thread.
					 * If the request was forwarded from another coordinator
					 * or if the hash value of the key equals this coordinator
					 * Send the put request to all datastores from this coordinator */
						try {
							if (consistencyType.equals("strong")) {
								//only if the request was not forwarded from other coordinators
								//send the ahead request
								if (forwarded == null) {
									System.out.println("aheading:" + key);
									KeyValueLib.AHEAD(key, timestamp.toString());
									
								}
								//if the request belongs to this coordinator, put the data to datastores
								if (hashFunction(key).equals(thisCoordinator)) {
									System.out.println("putting:" + key);
									KeyValueLib.PUT(dataCenterUSE, key, value, timestamp.toString(), consistencyType); 
									KeyValueLib.PUT(dataCenterUSW, key, value, timestamp.toString(), consistencyType);
									KeyValueLib.PUT(dataCenterSING, key, value, timestamp.toString(), consistencyType);

									KeyValueLib.COMPLETE(key, timestamp.toString());
									System.out.println("complete update for key: " + key);

								} else {
									//otherwise, forward the request to other coordinator based on hashfunction
									KeyValueLib.FORWARD(hashFunction(key), key, value, timestamp.toString());
									System.out.println("Forwarded key: " + key + "for coordinator: " + hashFunction(key));
								}
							} else {
								//eventual consistency test:
								//if the request belongs to this coordinator, put the data to datastores
								if (hashFunction(key).equals(thisCoordinator)) {
									System.out.println("putting:" + key);
									KeyValueLib.PUT(dataCenterUSE, key, value, timestamp.toString(), consistencyType); 
									KeyValueLib.PUT(dataCenterUSW, key, value, timestamp.toString(), consistencyType);
									KeyValueLib.PUT(dataCenterSING, key, value, timestamp.toString(), consistencyType);


								} else {
									//otherwise forward to other coordinator based on hashfunction
									KeyValueLib.FORWARD(hashFunction(key), key, value, timestamp.toString());
									System.out.println("Forwarded key: " + key + "for coordinator: " + hashFunction(key));
								}
							}

						} catch (IOException e) {
							e.printStackTrace();
						}
							
						}
			
				});
				t.start();
				req.response().end(); // Do not remove this
			}
		});

		routeMatcher.get("/get", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				MultiMap map = req.params();
				final String key = map.get("key");
				final Long timestamp = Long.parseLong(map.get("timestamp"));
				Thread t = new Thread(new Runnable() {
					public void run() {
					// For both strong and eventual test, it will get the data from local datastore
					 
						try {
							String response = KeyValueLib.GET(thisDataCenter, key, timestamp.toString(), consistencyType);
							req.response().end(response);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
				});
				t.start();
			}
		});
		/* This endpoint is used by the grader to change the consistency level */
		routeMatcher.get("/consistency", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				MultiMap map = req.params();
				consistencyType = map.get("consistency");
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
