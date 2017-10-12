import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.undertow.Undertow;
import io.undertow.server.*;
import io.undertow.util.Headers;

/**
 * The class for undertwo framework
 * 
 */
public class HBaseQuery {
	// private final static int CACHE_SIZE = 4500000;
 //    /**
 //    *  LinkedHashMap for LRU Cache
 //    */
 //    private static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE, .75F, true) {
 //        /**
 //         * 
 //         */
 //        private static final long serialVersionUID = 1L;
 //        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
 //            return size() > CACHE_SIZE;
 //        }
 //    };
    
    /**
     *  The method to retrieve tweets from HBase
     *  The return value will be the response to the client
     */
    private static String retrieveTweets(String userid, String hashtag) {
        String info = null;
        // if (cache.containsKey(userid + "&" + hashtag)) {
        //     info = cache.get(userid + "&" + hashtag);
        // } else {
        	List<String> resultList;
			try {
				resultList = HBaseTasks.getTweet(userid, hashtag);
				StringBuilder builder = new StringBuilder();
	            for (String item: resultList) {
	                builder.append(item);
	            }
	            info = String.format("ThreeKings,9675-1473-0896\n%s", builder.toString().replaceAll("#&1&#","\t").replaceAll("#&2&#","\n").replaceAll("#&3&#","\r").replaceAll("#&5&#",",") + ";;");
		    // cache.put(userid + "&" + hashtag, info);
			} catch (IOException e) {
				e.printStackTrace();
			}      
        // }
        return info;
    }
    
    public static void main(final String[] args) {
        Undertow server = Undertow.builder().setIoThreads(30).addHttpListener(8080, "ec2-54-172-42-245.compute-1.amazonaws.com")
                .setHandler(new HttpHandler() {
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=utf-8");
                        try {
                            String userid = exchange.getQueryParameters().get("userid").getFirst();
                            String hashtag = exchange.getQueryParameters().get("hashtag").getFirst();
                            String info = retrieveTweets(userid, hashtag);
                            exchange.getResponseSender().send(info);
                        } catch (Exception e) {
                            exchange.getResponseSender().send("this is an error page.");
                        }
                    }
                }).build();
        server.start();
    }
}
