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

/**
 * @author jingjinghuangfu
 *
 */
public class q4Servelet extends HttpServlet {

    private final String TEAM_INFO = "ThreeKings,9675-1473-0896";
    private Map<String, seqNumber> q4seq = new HashMap<String, seqNumber>();
    private TweetDAO tweetDAO;

    public void init() throws ServletException {
        tweetDAO = AbstractDAOFactory.getDAOFactory().getTweetDAO();
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

                if (q4seq.get(tweetid) != null && sequence < q4seq.get(tweetid).seq) return;

                if (q4seq.get(tweetid) == null) {
                    q4seq.put(tweetid, new seqNumber(1L));
                }

                if (op.equals("set")) {
                    String result = "";
                    seqNumber sn = q4seq.get(tweetid);
                    synchronized (sn) {
                        while (sequence != sn.seq) {
                            try {
                                sn.wait();
                            } catch(Exception e) {
                                sn.notifyAll();
                            }
                        }
                        try {
                            result = tweetDAO.q4Put(tweetid, fields, payload);
                            info = String.format("%s\n%s\n", TEAM_INFO, result.toString());
                            out.print(info);
                            sn.seq = sequence + 1;
                            sn.notifyAll();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                        }
                    }
                } else if (op.equals("get")) {
                    String result = "initial result";
                    seqNumber sn = q4seq.get(tweetid);
                    synchronized (sn) {
                        while (sequence != sn.seq) {
                            try {
                                sn.wait();
                            } catch(Exception e) {
                                sn.notifyAll();
                            }
                        }
                        try {
                            sn.seq = sequence + 1;
                            sn.notifyAll();
                            result = tweetDAO.q4Get(tweetid, fields);
                            info = String.format("%s\n%s\n", TEAM_INFO, result.toString());
                            out.print(info);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
    }

    public class seqNumber {
        long seq;
        public seqNumber(long s) {
            this.seq = s;
        }
    }
}

