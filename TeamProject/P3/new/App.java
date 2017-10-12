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
    private final static String DNS_OF_THIS_MACHINE = "ec2-52-201-214-65.compute-1.amazonaws.com";
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
    }
}
