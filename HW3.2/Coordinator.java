import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
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
import java.util.Date;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class Coordinator extends Verticle {

	// Default mode: sharding. Possible string values are "replication" and
	// "sharding"
	private static String storageType = "replication";

	/**
	 * TODO: Set the values of the following variables to the DNS names of your
	 * three dataCenter instances
	 */
	private static final String dataCenter1 = "ec2-52-91-246-250.compute-1.amazonaws.com";
	private static final String dataCenter2 = "ec2-54-88-82-228.compute-1.amazonaws.com";
	private static final String dataCenter3 = "ec2-52-91-159-103.compute-1.amazonaws.com";
	private HashMap<String, String> dataCenterMap = new HashMap<String, String>();
	private HashMap<String, PriorityQueue<Item>> keyMap = new HashMap<>();

	// create item class to store request parameters
	// date is the timestamp of request, type is the request type
	// for put request, value is the udpate data value; for get request, value
	// is the location
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

	// the method to process each upcoming request
	public String processItem(String key, Item item) {
		// if keyMap has the key, add the item to the corresponding heap
		System.out.println(key + "\t" + item.type + "\t" + item.value);

		// synchronized keyMap to avoid generating duplicated queues
		synchronized (keyMap) {
			if (!keyMap.containsKey(key)) {
				// create a new minHeap: item with earliest timestamp is at the
				// top
				PriorityQueue<Item> keyQueue = new PriorityQueue<Item>(15, new Comparator<Item>() {
					@Override
					public int compare(Item o1, Item o2) {
						return o1.getTimestamp().compareTo(o2.getTimestamp());					
					}
				});
				keyMap.put(key, keyQueue);

			}
		}
		// add the item into the minHeap and put the minHeap to the keyMap
		keyMap.get(key).offer(item);

		PriorityQueue<Item> queue = keyMap.get(key);

		synchronized (queue) {
			if (item.getType().equals("put")) {
				while (!queue.isEmpty()) {
					if (queue.peek().getType().equals("put")) {
						Item oneItem = queue.poll();
						try {
							if (storageType.equals("replication")) {
								System.out.println("replication put value: " + oneItem.getValue());
								// if the item at the top is put request, update
								// the data to all the data centers
								KeyValueLib.PUT(dataCenter1, key, oneItem.getValue());
								KeyValueLib.PUT(dataCenter2, key, oneItem.getValue());
								KeyValueLib.PUT(dataCenter3, key, oneItem.getValue());
							} else {
								System.out.println("sharding put: " + key + "\t" + oneItem.getValue());

								// if storageType is sharding, update the data
								// in the datacenter returned by hash function
								KeyValueLib.PUT(hashFunction(key), key, oneItem.getValue());
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						break;
					}
				}
			} else {
				// if the current request is get
				if (!queue.isEmpty()) {
					Item oneItem = queue.poll();
					try {
						//replication
						String dc;
						if (storageType.equals("replication")) {
							System.out.println("get value: " + oneItem.getValue());
						    dc = dataCenterMap.get(oneItem.getValue());

						} else {
							System.out.println("sharding get: " + key + "\t" + oneItem.getValue());

							//sharding
							if (oneItem.getValue() != null) {
								// if the request loc is not 1, 2 or 3, return 0
								if (!oneItem.getValue().equals("1") && !oneItem.getValue().equals("2")
										&& !oneItem.getValue().equals("3")) {
									return "0";
									// if the request loc's corresponding
									// datacenter is different from hashed value
									// of the key, return 0
								} if (oneItem.value.equals("1") && !hashFunction(key).equals(dataCenter1)) {
									return "0";
								} else if (oneItem.value.equals("2") && !hashFunction(key).equals(dataCenter2)) {
									return "0";
								} else if (oneItem.value.equals("3") && !hashFunction(key).equals(dataCenter3)) {
									return "0";
								}
							}
							dc = dataCenterMap.get(oneItem.getValue());

						}	
						String returnValue = KeyValueLib.GET(dc,key);

						if (returnValue == null) {
							return "0";
						} else {
							return returnValue;
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}

		return "0";

	}
	//hash function to hash the key to a datastore
	private String hashFunction(String key) {
		if (key.equals("a") || key.matches(".*[A-Id-i1-3]$")) {
			return dataCenter1;
		}
		else if (key.equals("b") || key.matches(".*[J-Qj-q4-7]$")) {
			return dataCenter2;
		}
		    return dataCenter3;
	}
	@Override
	public void start() {
		// DO NOT MODIFY THIS
		KeyValueLib.dataCenters.put(dataCenter1, 1);
		KeyValueLib.dataCenters.put(dataCenter2, 2);
		KeyValueLib.dataCenters.put(dataCenter3, 3);

		dataCenterMap.put("1", dataCenter1);
		dataCenterMap.put("2", dataCenter1);
		dataCenterMap.put("3", dataCenter1);

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
				final Timestamp timestamp = new Timestamp(
						System.currentTimeMillis() + TimeZone.getTimeZone("EST").getRawOffset());

				// start a new thread everytime a request comes in
				Thread t = new Thread(new Runnable() {
					public void run() {
						// TODO: Write code for PUT operation here.
						// Each PUT operation is handled in a different thread.
						// Highly recommended that you make use of helper
						// functions.
						Item item = new Item(value, timestamp, "put");

						processItem(key, item);

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
				final String loc = map.get("loc");
				// You may use the following timestamp for ordering requests
				final Timestamp timestamp = new Timestamp(
						System.currentTimeMillis() + TimeZone.getTimeZone("EST").getRawOffset());

				Item item = new Item(loc, timestamp, "get");

				Thread t = new Thread(new Runnable() {
					public void run() {

						req.response().end(processItem(key, item)); // Default
																	// response
																	// = 0

					}

				});
				t.start();

			}

		});

		routeMatcher.get("/storage", new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				MultiMap map = req.params();
				storageType = map.get("storage");
				// This endpoint will be used by the auto-grader to set the
				// consistency type that your key-value store has to support.
				// You can initialize/re-initialize the required data structures
				// here
				keyMap = new HashMap<String, PriorityQueue<Item>>();
				req.response().end();
			}
		});

		routeMatcher.noMatch(new Handler<HttpServerRequest>() {
			@Override
			public void handle(final HttpServerRequest req) {
				req.response().putHeader("Content-Type", "text/html");
				String response = "Not found.";
				req.response().putHeader("Content-Length", String.valueOf(response.length()));
				req.response().end(response);
				req.response().close();
			}
		});
		server.requestHandler(routeMatcher);
		server.listen(8080);
	}
}
