import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jingjinghuangfu
 *
 */
public class q4Servelet extends HttpServlet {

    private final String TEAM_INFO = "ThreeKings,9675-1473-0896";
    private Map<String, AtomicInteger> q4seq = new HashMap<String, AtomicInteger>();
    private TweetDAO tweetDAO;
    private final int DATACENTER_NUMBER = 4;
    private String[] DataCenters = new String[5];

    public void init() throws ServletException {
        tweetDAO = AbstractDAOFactory.getDAOFactory().getTweetDAO();
        DataCenters[0] = "ec2-52-23-215-52.compute-1.amazonaws.com";
        DataCenters[1] = "ec2-52-91-113-90.compute-1.amazonaws.com";
        DataCenters[2] = "ec2-54-165-154-210.compute-1.amazonaws.com";
        DataCenters[3] = "ec2-54-165-97-101.compute-1.amazonaws.com";
        DataCenters[4] = "ec2-54-89-59-165.compute-1.amazonaws.com";
    }

    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                String info = "";
                response.setContentType("text/plain;charset=utf-8");
                String tweetid = request.getParameter("tweetid");
                String op = request.getParameter("op");
                String seq = request.getParameter("seq");
                String fields = request.getParameter("fields");
                String payload = request.getParameter("payload");
                PrintWriter out = response.getWriter();
                long sequence = Long.parseLong(seq);

                int datacenter = hashFunction(tweetid);

                String requestpara = "/q4?tweetid="+tweetid+"&op="+op+"&seq="+seq+"&fields="+fields+"&payload="+payload;

                if (datacenter != DATACENTER_NUMBER) {
                    try {
                        dispatchrequest(datacenter, response, requestpara);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                //System.out.println("tweetid=" + tweetid + " seq=" + seq);

                //if (q4seq.get(tweetid) != null && sequence < q4seq.get(tweetid).seq) return;
                synchronized (q4seq) {
                    if (q4seq.get(tweetid) == null) {
                        q4seq.put(tweetid, new AtomicInteger(1));
                    }
                }   
                //seqNumber sn = q4seq.get(tweetid);
                AtomicInteger sn = q4seq.get(tweetid);
                String result = "";
                synchronized (sn) {
                        while (sequence != sn.get()) {
                            try {
                                System.out.println(op + " is for tweetid " + tweetid + " blocking seq=" + seq + " current sequence=" + sn.toString());
                                sn.wait();
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            result = tweetDAO.doq4(tweetid, fields, payload,op);
                            info = String.format("%s\n%s\n", TEAM_INFO, result.toString());
                            out.print(info);
                            sn.set(sn.incrementAndGet());
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            sn.notifyAll();
                        }
                }
    }

    public void dispatchrequest(int dc, HttpServletResponse response, String requestpara) throws IOException{
        response.setContentType("text/plain;charset=utf-8");
        String url = "http://" + DataCenters[dc] + requestpara;
        response.sendRedirect(url);
        return;
    }

    public int hashFunction(String tweetid) {
        int lastdigit = tweetid.charAt(tweetid.length() - 1) - '0';
        int datacenter = lastdigit % 5;
        return datacenter;
    }

    // public class seqNumber {
    //     long seq;
    //     public seqNumber(long s) {
    //         this.seq = s;
    //     }
    // }
}

