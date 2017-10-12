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
public class q3Servelet extends HttpServlet {

    private final String TEAM_INFO = "ThreeKings,9675-1473-0896";
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
                String start_date = request.getParameter("start_date");
                String end_date = request.getParameter("end_date");
                String start_userid = request.getParameter("start_userid");
                String end_userid = request.getParameter("end_userid");
                String words = request.getParameter("words");

                PrintWriter out = response.getWriter();

                info = q3(start_date, end_date, start_userid, end_userid, words);

                out.print(info);
    }

    /**
     * put the q3 query function here:
     * @param userId
     * @return return the result which will be displayed on the response page.
     */
    public String q3(String startDate, String endDate, String startId, String endId, String words) {
        String info = null;
        String wc = tweetDAO.getwordcount(startDate, endDate, startId, endId, words);
        info = String.format("%s\n%s", TEAM_INFO, wc);
        return info;
    }

}

