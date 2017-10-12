import java.util.List;

import io.undertow.Undertow;
import io.undertow.server.*;
import io.undertow.util.Headers;

/**
 * Hello world!
 * 
 */
public class HBaseQuery {
    public static void main(final String[] args) {
        Undertow server = Undertow.builder().addHttpListener(8080, "localhost")
                .setHandler(new HttpHandler() {
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=utf-8");
                        try {
                            String userid = exchange.getQueryParameters().get("userid").getFirst();
                            String hashtag = exchange.getQueryParameters().get("hashtag").getFirst();
                            List<String> resultList = HBaseTasks.getTweet(userid, hashtag);
                            StringBuilder builder = new StringBuilder();
                            for (String item: resultList) {
                                builder.append(item);
                                System.out.println(item);
                            }
                            String info = String.format("ThreeKings,9675-1473-0896\n%s", builder.toString());
                            exchange.getResponseSender().send(info);
//                            exchange.getResponseSender().send("hello world");
                        } catch (Exception e) {
//                            e.printStackTrace();
                            exchange.getResponseSender().send("this is an error page.");
                        }
                    }
                }).build();
        server.start();
    }
}
