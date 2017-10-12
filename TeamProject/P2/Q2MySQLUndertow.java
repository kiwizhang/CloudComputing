import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.undertow.Undertow;
import io.undertow.server.*;
import io.undertow.util.Headers;

/**
 * Hello world!
 * 
 */
public class App {
    private final static String DNS_OF_THIS_MACHINE = "ec2-54-210-34-90.compute-1.amazonaws.com";
    //use a cache of size 450W, LRU cache
    private final static int CACHE_SIZE = 4500000;
    private static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE, .75F, true) {
        private static final long serialVersionUID = 1L;
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > CACHE_SIZE;
        }
    };
    private static String retrieveTweets(String userid, String hashtag) {
        String info = null;
	    String combine = userid + "&&" + hashtag;
        if (cache.containsKey(combine)) {
            info = cache.get(combine);
        } else {
            List<String> resultList = AbstractDAOFactory.getDAOFactory().getTweetDAO().getTweetStringByUseridAndHashtag(userid, hashtag);
            StringBuilder builder = new StringBuilder();
            for (String item: resultList) {
                builder.append(item);
            }
            builder.append("\n");
            info = String.format("ThreeKings,9675-1473-0896\n%s", builder.toString());
	    cache.put(combine, info);
        }
        return info;
    }
    public static void main(final String[] args) {
        //open server and linsten on 8080 port
        Undertow server = Undertow.builder().addHttpListener(8080, DNS_OF_THIS_MACHINE)
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
