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
public class q1Servelet extends HttpServlet {

    public BigInteger X = new BigInteger("64266330917908644872330635228106713310880186591609208114244758680898150367880703152525200743234420230");
    public String TeamID = "ThreeKings";
    public String TeamAWSID = "9675-1473-0896";
    private final String TEAM_INFO = "ThreeKings,9675-1473-0896";
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                String info = "";
                response.setContentType("text/plain;charset=utf-8");
                String key = request.getParameter("key");
                String message = request.getParameter("message");
                PrintWriter out = response.getWriter();
                info = q1(key, message);
                out.print(info);
    }

    /**
     * put the q1 query function here:
     * @param key
     * @param message
     * @return return the result which will be displayed on the response page.
     */
    public String q1(String key, String message) {
        String info = null;
        String result = Decryption.decrypt(key, message);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateToStr = format.format(new Date());
        info = String.format("%s\n%s\n%s\n", TEAM_INFO, DateToStr.toString(), result.toString());
        return info;
    }

    public class seqNumber {
        long seq;
        public seqNumber(long s) {
            this.seq = s;
        }
    }
}

