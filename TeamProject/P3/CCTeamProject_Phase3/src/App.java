import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigInteger;

import io.undertow.Undertow;
import io.undertow.server.*;
import io.undertow.util.Headers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import javax.servlet.ServletException;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Handlers;

/**
 * This is a class work as MySQL front end.
 */
public class App {
    public static BigInteger X = new BigInteger("64266330917908644872330635228106713310880186591609208114244758680898150367880703152525200743234420230");
    public static String TeamID = "ThreeKings";
    public static String TeamAWSID = "9675-1473-0896";
    private final static String TEAM_INFO = "ThreeKings,9675-1473-0896";
    private final static String DNS_OF_THIS_MACHINE = "ec2-54-152-146-77.compute-1.amazonaws.com";
    //the map of tweetid and priority queue of options on this tweetid that is sharding in this backend server.
    //private static Map<String, PriorityQueue<Long>> q4map = new HashMap<String, PriorityQueue<Long>>();
    //the map of the current sequence number each tweetid should work on.
    //private static Map<String, seqNumber> q4seq = new HashMap<String, seqNumber>();

    public static final String PATH = "/";

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
    /**
     * put the q2 query function here:
     * @param userId
     * @param tag
     * @return return the result which will be displayed on the response page.
     */
    public static String q2(String userId, String tag) {
	String info = null;
	String result = AbstractDAOFactory.getDAOFactory().getTweetDAO().getTweetStringByUseridAndHashtag(userId, tag);
	info = String.format("%s\n%s", TEAM_INFO, result.replaceAll("#&1&#","\t").replaceAll("#&2&#","\n").replaceAll("#&3&#","\r").replaceAll("#&5&#",",") + ";;");
	return info;
    }

    /**
     * put the q3 query function here:
     * @param userId
     * @return return the result which will be displayed on the response page.
     */
    public static String q3(String startDate, String endDate, String startId, String endId, String words) {
        String info = null;
        String wc = AbstractDAOFactory.getDAOFactory().getTweetDAO().getwordcount(startDate, endDate, startId, endId, words);
	info = String.format("%s\n%s", TEAM_INFO, wc);
        return info;
    }
    public static void main(final String[] args) {
        // PathHandler path = new PathHandler().addPrefixPath("/q1", new HttpHandler() {
        //     public void handleRequest(final HttpServerExchange exchange) throws Exception {
        //         String info = "";
        //         exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=utf-8");
        //         String key = exchange.getQueryParameters().get("key").getFirst();
        //         String message = exchange.getQueryParameters().get("message").getFirst();
        //         info = q1(key, message);
        //         exchange.getResponseSender().send(info);
        //     }
        // }).addPrefixPath("/q4", new HttpHandler() {
        //     public void handleRequest(final HttpServerExchange exchange) throws Exception {
        //         String info = "";

        //         exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=utf-8");
        //         final String tweetid = exchange.getQueryParameters().get("tweetid").getFirst();
        //         String op = exchange.getQueryParameters().get("op").getFirst();
        //         String seq = exchange.getQueryParameters().get("seq").getFirst();
        //         final String fields = exchange.getQueryParameters().get("fields").getFirst();
        //         final String payload = exchange.getQueryParameters().get("payload").getFirst();
        //         final long sequence = Long.parseLong(seq);

        //         System.out.println("new request seq = " + seq);

        //         if (q4seq.get(tweetid) != null && sequence < q4seq.get(tweetid).seq) return;

        //         if (q4seq.get(tweetid) == null) {
        //             q4seq.put(tweetid, new seqNumber(1L));
        //         }
        //         // synchronized(q4map) {
        //         //     if (q4map.containsKey(tweetid)) {
        //         //         q4map.get(tweetid).offer(sequence);
        //         //     } else {
        //         //         PriorityQueue<Long> que = new PriorityQueue<Long>(10, new Comparator<Long>(){
        //         //             public int compare(Long l1, Long l2) {
        //         //                 if (l1 < l2) return -1;
        //         //                 if (l1 > l2) return 1;
        //         //                 return 0;
        //         //             }
        //         //         });
        //         //         q4map.put(tweetid, que);
        //         //     }
        //         // }

        //         if (op.equals("set")) {
        //             String result = "";
        //             //PriorityQueue<Long> seqs = q4map.get(tweetid);
        //             //System.out.println("current que: " + seqs);
        //             seqNumber sn = q4seq.get(tweetid);
        //             synchronized (sn) {
        //                 while (sequence != sn.seq) {
        //                     try {
        //                         System.out.println("set with seq= " + seq + " is waiting, current seq is " + sn.seq + " for tweetid= " + tweetid);
        //                         sn.wait();
        //                     } catch(Exception e) {
        //                         sn.notifyAll();
        //                     }
        //                 }
        //                 try {
        //                     result = AbstractDAOFactory.getDAOFactory().getTweetDAO().q4Put(tweetid, fields, payload);
        //                     info = String.format("%s\n%s\n", TEAM_INFO, result.toString());
        //                     exchange.getResponseSender().send(info);
        //                     //q4seq.put(tweetid, sequence + 1);
        //                     sn.seq = sequence + 1;
        //                     //q4map.get(tweetid).poll();
        //                     sn.notifyAll();
        //                 } catch (Exception e) {
        //                     e.printStackTrace();
        //                 } finally {
        //                     //System.out.println(q4seq.get(tweetid)); 
        //                 }
        //             }
        //         } else if (op.equals("get")) {
        //             String result = "initial result";
        //             //PriorityQueue<Long> seqs = q4map.get(tweetid);
        //             //System.out.println("current que: " + seqs);
        //             seqNumber sn = q4seq.get(tweetid);
        //             synchronized (sn) {
        //                 //System.out.println(q4seq.get(tweetid));
        //                 while (sequence != sn.seq) {
        //                     try {
        //                         System.out.println("get with seq= " + seq + " is waiting, current seq is " + sn.seq + " for tweetid= " + tweetid);
        //                         sn.wait();
        //                     } catch(Exception e) {
        //                         sn.notifyAll();
        //                     }
        //                 }
        //                 try {
        //                     //q4map.get(tweetid).poll();
        //                     //q4seq.put(tweetid, sequence + 1);
        //                     sn.seq = sequence + 1;
        //                     sn.notifyAll();
        //                     //System.out.println(q4seq.get(tweetid));
        //                     result = AbstractDAOFactory.getDAOFactory().getTweetDAO().q4Get(tweetid, fields);
        //                     info = String.format("%s\n%s\n", TEAM_INFO, result.toString());
        //                     exchange.getResponseSender().send(info);
        //                 } catch (Exception e) {
        //                     e.printStackTrace();
        //                 }
        //             }
        //         }
        //     }
        // });

        try {
            DeploymentInfo servletBuilder = deployment()
                    .setClassLoader(App.class.getClassLoader())
                    .setContextPath(PATH)
                    .setDeploymentName("handler.war")
                    .addServlets(
                            servlet("q1Servelet", q1Servelet.class)
                            .addMapping("/q1"),
                            servlet("q4Servelet", q4Servelet.class)
                            .addMapping("/q4"),
                            servlet("q2Servelet", q2Servelet.class)
                            .addMapping("/q2"),
                            servlet("q3Servelet", q3Servelet.class)
                            .addMapping("/q3")
                    );


            DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            HttpHandler servletHandler = manager.start();
            PathHandler path = Handlers.path(Handlers.redirect(PATH))
                    .addPrefixPath(PATH, servletHandler);

            Undertow server = Undertow.builder().setIoThreads(10)
                    .addHttpListener(8080, DNS_OF_THIS_MACHINE)
                    .setHandler(path)
                    .build();
            server.start();
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }

        // Undertow server = Undertow.builder().addHttpListener(8080, DNS_OF_THIS_MACHINE)
        //         .setHandler(path).build();
        // server.start();
    }
}
