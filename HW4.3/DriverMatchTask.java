package com.cloudcomputing.samza.pitt_cabs;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;

import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.KeyValueIterator;
import org.apache.samza.storage.kv.Entry;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.WindowableTask;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

/**
 * Consumes the stream of driver location updates and rider cab requests.
 * Outputs a stream which joins these 2 streams and gives a stream of rider
 * to driver matches.
 * @author Jiwei  andrewid: jiweiz
 *
 */
public class DriverMatchTask implements StreamTask, InitableTask, WindowableTask {

  /* Define per task state here. (kv stores etc) */
  private KeyValueStore<String, Map<String, Object>> driverLocation;
  private Map<String, Object> result;
  private Map<String, Queue> spfMap;

  @Override
  @SuppressWarnings("unchecked")
  public void init(Config config, TaskContext context) throws Exception {
  //Initialize stuff (maybe the kv stores?)
    driverLocation = (KeyValueStore<String, Map<String, Object>>) context.getStore("driver-loc");
    result = new HashMap<String, Object>();
    spfMap = new HashMap<String, Queue>();


  }

  @Override
  @SuppressWarnings("unchecked")
  public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
  // The main part of your code. Remember that all the messages for a particular partition
  // come here (somewhat like MapReduce). So for task 1 messages for a blockId will arrive
  // at one task only, thereby enabling you to do stateful stream processing.


    String incomingStream = envelope.getSystemStreamPartition().getStream();
    if (envelope.getMessage() != null) {
      if (incomingStream.equals(DriverMatchConfig.DRIVER_LOC_STREAM.getStream())) {
        processDriverLocEvent((Map<String, Object>) envelope.getMessage());
      } else if (incomingStream.equals(DriverMatchConfig.EVENT_STREAM.getStream())) {
        processEvent((Map<String, Object>) envelope.getMessage(), collector);
      } else {
        throw new IllegalStateException("Unexpected input stream: " + envelope.getSystemStreamPartition());
      }
    } else {
      throw new IllegalStateException("Unexpected input stream: message is null");
    }
    
  }


  @Override
  public void window(MessageCollector collector, TaskCoordinator coordinator) {
  //this function is called at regular intervals, not required for this project
  }

  /**
 * use the DRIVER_LOCATION stream to update the location data for each driver 
 * @param message
 */
public void processDriverLocEvent (Map<String, Object> message) {

    if (!message.get("type").equals("DRIVER_LOCATION")) {
      throw new IllegalStateException("Unexpected type on driver locations stream: " + message.get("event"));
    }

    String driverId = (int) message.get("driverId") + "";
    //update the driver location data only if the driverId existed in the data store
    if (driverLocation.get(driverId) != null) {
      driverLocation.get(driverId).put("latitude", message.get("latitude"));
      driverLocation.get(driverId).put("longitude", message.get("longitude"));
    }
  }

  /**
 * process different types of the event
 * @param message
 * @param collector
 */
public void processEvent(Map<String, Object> message, MessageCollector collector) {

  // if the event type is leaving block, delete the driver from the datastore
    if (message.get("type").equals("LEAVING_BLOCK")) {
      // int blockId = (int) message.get("blockId");
      String driverId = (int) message.get("driverId") + "";
      if (driverLocation.get(driverId) != null) {
        driverLocation.delete(driverId);
      }
      // if the event type is enterting block and the status is available,
      // add the driver information to the datastore
      // if the status is unavailable
      // delete the driver from the datastore
    } else if (message.get("type").equals("ENTERING_BLOCK")) {
      // int blockId = (int) message.get("blockId");
      String driverId = (int) message.get("driverId") + "";
      if(message.get("status").equals("AVAILABLE")){
        driverLocation.put(driverId, message);
      }else{
        if (driverLocation.get(driverId) != null) {
          driverLocation.delete(driverId);
        }
      } 
      //if the event type is RIDE_COMPLETE, add the driver to datastore
    } else if (message.get("type").equals("RIDE_COMPLETE")) {
      // int blockId = (int) message.get("blockId");
      String driverId = (int) message.get("driverId") + "";
      driverLocation.put(driverId, message);
      //if the event type is RIDE_REQUEST, iterate the whole driverLocation datastore to get
      //all the drivers whose blockId equals the blockId in RIDE_REQUEST
      //calculate the match_score for each driver, and get the one with lowest score
      // return the corresponding driverId, clientId, and priceFactor
    } else if (message.get("type").equals("RIDE_REQUEST")) {
      String cblockId = (int) message.get("blockId") + "";
      int cla = (int) message.get("latitude");
      int clo = (int) message.get("longitude");

      String gender_preference = (String) message.get("gender_preference");
      KeyValueIterator<String, Map<String, Object>> avaiDrivers = driverLocation.all();

      double match_score = 0;
      String driverId = "";
      String clientId = "";
      String driverkey = "";
      double avai_count = 0;
      try {
        while (avaiDrivers.hasNext()) {
          Entry<String, Map<String, Object>> entry = avaiDrivers.next();
          if (entry != null) {
            Map<String, Object> value = (Map<String, Object>)entry.getValue();
            String dblockId = (int) value.get("blockId") + "";

            if (dblockId.equals(cblockId)) {

              avai_count++;

              String key = entry.getKey();


              int dla =  (int)value.get("latitude");

              int dlo = (int)value.get("longitude");

              double rating = (double)value.get("rating");

              String gender = (String) value.get("gender");

              int salary = (int)value.get("salary");

              double distance = Math.sqrt(Math.pow((double)(dla - cla), 2) + (double)Math.pow((dlo - clo), 2));
              double MAX_DIST = 500*Math.sqrt(2);
              double distance_score = 1 - distance/MAX_DIST;


              double gender_score;
              if (gender_preference.equals(gender) || gender_preference.equals("N")) {
                gender_score = 1;
              } else {
                gender_score = 0;
              }

              double rating_score = rating / 5.0;
              double salary_score = 1- salary / 100.0;
              double tempscore = distance_score * 0.4 + gender_score * 0.2 + rating_score * 0.2 + salary_score * 0.2;

              System.out.println("tempsore: " + tempscore);
              System.out.println("match_score: " + match_score);

              if (tempscore > match_score) {
               System.out.println("if tempscore > match_score ");

               match_score = tempscore;
               driverkey = key;
             // driverId = (int) value.get("driverId") + "";
               clientId = String.valueOf(message.get("clientId"));

               driverId = String.valueOf(value.get("driverId"));

             }


           } 

           System.out.println("avai_count: " + avai_count);
           if (spfMap.get(cblockId) == null) {
            Queue newQueue = new Queue();
            newQueue.push(avai_count);
            spfMap.put(cblockId, newQueue);
          } else {
            spfMap.get(cblockId).push(avai_count);
          }
        }

      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    avaiDrivers.close();

    if (!clientId.equals("") && !driverId.equals("")) {
     System.out.println("driverkey: " + driverkey);
     System.out.println("clientId: " + clientId);
     System.out.println("driverId: " + driverId);
     result.put("clientId", clientId);
     result.put("driverId", driverId);
     result.put("priceFactor", calSPF(cblockId, spfMap));
     driverLocation.delete(driverkey);
     collector.send(new OutgoingMessageEnvelope(DriverMatchConfig.MATCH_STREAM, result));
   }   

 } 

}

/**
 * Calculate the price factor
 * @param blockId
 * @param spfMap
 * @return
 */
public double calSPF(String blockId, Map<String, Queue> spfMap) {
  Queue driverQueue = spfMap.get(blockId);
  if (driverQueue.getCount() < 5) {
    return 1;
  } else {
    double sum = 0;
    for (int i = 0; i < 5; i++) {
      sum = sum + driverQueue.getElement(i);
    }
    double average_ratio = sum/5;
    System.out.println("average_ratio: " + average_ratio);
    if (average_ratio >= 3.6) {
      return 1;
    } else {
      double sf = 4 * (3.6 - average_ratio)/(1.8 - 1);
      System.out.println("spf: " + sf + 1);
      return sf + 1;
    }

  }


}

/**
 * Queue class
 * Store the latest 5 price factors of a block
 */
public class Queue {
  private ArrayList<Double> list;
  private int count;
  public Queue () {
    list = new ArrayList();
    count = 0;
  }
  public void push (Double priceFactor) {
    if(count < 5) {
      list.add(priceFactor);
      count++;
    } else {
      list.remove(0);
      list.add(priceFactor);
    }
  }

  public int getCount() {
    return count;
  }

  public double getElement (int i) {
    return list.get(i);
  }
}


}
