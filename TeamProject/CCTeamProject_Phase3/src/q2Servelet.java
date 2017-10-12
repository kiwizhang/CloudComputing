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
 * Customer login function
 * @author jingjinghuangfu
 *
 */
public class q2Servelet extends HttpServlet {

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
                String userId = request.getParameter("userid");
                String tag = request.getParameter("hashtag");
                PrintWriter out = response.getWriter();
                info = q2(userId, tag);
                out.print(info);

    }

    /**
     * put the q2 query function here:
     * @param userId
     * @param tag
     * @return return the result which will be displayed on the response page.
     */
    public String q2(String userId, String tag) {
    String info = null;
    String result = tweetDAO.getTweetStringByUseridAndHashtag(userId, tag);
    info = String.format("%s\n%s", TEAM_INFO, result.replaceAll("#&1&#","\t").replaceAll("#&2&#","\n").replaceAll("#&3&#","\r").replaceAll("#&5&#",",") + ";;");
    return info;
    }

}

