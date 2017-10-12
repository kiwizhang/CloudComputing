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
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.*;
import java.text.SimpleDateFormat;
import java.math.BigInteger;

import io.undertow.Undertow;
import io.undertow.server.*;
import io.undertow.util.Headers;

/**
 * The class for undertwo framework
 * 
 */
public class HBaseQuery {
    private final static String TEAM_INFO = "ThreeKings,9675-1473-0896";
    private final static int CACHE_SIZE = 4500000;
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE, .75F, true) {
        private static final long serialVersionUID = 1L;
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > CACHE_SIZE;
        }
    };
    /**
     * put the q1 query function here:
     * @param key
     * @param message
     * @return return the result which will be displayed on the response page.
     */
    public static String q1(String key, String message) {
        String info = null;
        String result = Decryption.decrypt(key, message);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateToStr = format.format(new Date());
        info = String.format("%s\n%s\n%s\n", TEAM_INFO, DateToStr.toString(), result.toString());
        return info;
    }

    private static String q3(String startId, String stopId, String startDate, String stopDate, String[] wordList) {
        String info = "";
        String result;
        startDate = startDate.replace("-", "");
        stopDate = stopDate.replace("-", "");
        try {
            result = HBaseTasks.getTweet(startId, stopId, startDate, stopDate, wordList);
            info = String.format("ThreeKings,9675-1473-0896\n%s", result);
            } catch (Exception e) {
                e.printStackTrace();
            }      
        return info;
    }

    private static String q2(String userid, String hashtag) {
        String info = null;
        if (cache.containsKey(userid + "&" + hashtag)) {
            info = cache.get(userid + "&" + hashtag);
        } else {
            List<String> resultList;
            try {
                resultList = Q2Task.getTweet(userid, hashtag);
                StringBuilder builder = new StringBuilder();
                for (String item: resultList) {
                    builder.append(item);
                }
            //System.out.println("builder:" + builder.toString());
                info = String.format("ThreeKings,9675-1473-0896\n%s", builder.toString().replaceAll("#&1&#","\t").replaceAll("#&2&#","\n").replaceAll("#&3&#","\r").replaceAll("#&5&#",",") + ";;");
                //System.out.println(builder.toString().replaceAll("\\n","\n"));
            cache.put(userid+ "&" + hashtag, info);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }      
        }
        return info;
    }
    
    public static void main(final String[] args) {
        System.out.println("before start server");

        Undertow server = Undertow.builder().setIoThreads(10).addHttpListener(8080, "ec2-54-152-22-185.compute-1.amazonaws.com")
                .setHandler(new HttpHandler() {
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        //System.out.println("before get parameters:" + System.currentTimeMillis());
                        String info = "initial info";
                        System.out.println("befre getResponseHeaders");
                        //System.out.println(exchange);
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=utf-8");
                        try {
                            String path = exchange.getRequestPath();
                            if (path != null && path.equals("/q1")) {
                                String key = exchange.getQueryParameters().get("key").getFirst();
                                String message = exchange.getQueryParameters().get("message").getFirst();
                                info = q1(key, message);
                                System.out.println("in q1: " + info);

                            }

                            if (path != null && path.equals("/q2")) {
                                System.out.println("in q2");

                                String userId = exchange.getQueryParameters().get("userid").getFirst();
                                String tag = exchange.getQueryParameters().get("hashtag").getFirst();
                                info = q2(userId, tag);
                                 System.out.println("in q2: " + info);

                            }

                            if (path != null && path.equals("/q3")) {
                                String start_date = exchange.getQueryParameters().get("start_date").getFirst();
                                String end_date = exchange.getQueryParameters().get("end_date").getFirst();
                                String start_userid = exchange.getQueryParameters().get("start_userid").getFirst();
                                String end_userid = exchange.getQueryParameters().get("end_userid").getFirst();
                                String words = exchange.getQueryParameters().get("words").getFirst();
                                String[] wordList = words.split(",");
                                info = q3(start_userid, end_userid, start_date, end_date, wordList);
                                System.out.println("in q3: " + info);

                            }

                            // System.out.println("START query:" + System.currentTimeMillis());
                            // String startId = exchange.getQueryParameters().get("start_userid").getFirst();
                            // //System.out.println("startId: " + startId);
                            // String stopId = exchange.getQueryParameters().get("end_userid").getFirst();
                            // //System.out.println("endId: " + stopId);
                            // String startDate = exchange.getQueryParameters().get("start_date").getFirst().replace("-", "");
                            // //System.out.println("startdate: " + startDate);
                            // String stopDate = exchange.getQueryParameters().get("end_date").getFirst().replace("-", "");
                            // //System.out.println("stopdate: " + stopDate);
                            // String words = exchange.getQueryParameters().get("words").getFirst();
                            // //System.out.println("after get parameters:" + System.currentTimeMillis());
                            // String[] wordList = words.split(",");
                            // //System.out.println("before query:" + System.currentTimeMillis());
                            // info = retrieveTweets(startId, stopId, startDate, stopDate, wordList);
                            // //System.out.println("after query:" + System.currentTimeMillis());
                            exchange.getResponseSender().send(info);
                            //System.out.println("AFTER query:" + System.currentTimeMillis());
                        } catch (Exception e) {
                            //exchange.getResponseSender().send("this is an error page.");
                            e.printStackTrace();
                        }
                    }
                }).build();
        server.start();
    }
}
