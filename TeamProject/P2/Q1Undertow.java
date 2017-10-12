import io.undertow.Undertow;
import io.undertow.server.*;
import io.undertow.util.Headers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigInteger;
import java.util.*;

public class App {
    public static BigInteger X = new BigInteger("64266330917908644872330635228106713310880186591609208114244758680898150367880703152525200743234420230");
    public static String TeamID = "ThreeKings";
    public static String TeamAWSID = "9675-1473-0896";
	/* 
	 * init -initializes the variables which store the 
	 *	     DNS of your database instances
	 */

	public static String decrypt(String y, String encryptMessage) {
		BigInteger Y = new BigInteger(y);
		int key = getKey(Y);
        int len = encryptMessage.length();
        int line = (int)Math.sqrt(len);
        String pre = despiral(encryptMessage, line);
        String result = reduction(pre, key);

        return result;
    }

    public static int getKey(BigInteger Y) {
        String gcdstring = X.gcd(Y).toString();
        BigInteger gcd = new BigInteger(gcdstring);
        int k = 1 + gcd.mod(new BigInteger("25")).intValue();
        return k;
    }


    public static String despiral(String s, int line) {   	
        StringBuilder result = new StringBuilder();
        int top = 0, bottom = line - 1, left = 0, right = line - 1;
        int dir = 0;
        int i = 0;
        while (top <= bottom && left <= right) {
             if (dir == 0) {
                 for (i = left; i <= right; i++) {
                      result.append(s.charAt(line*top+i));
                 }
                 top++;
             }
             else if (dir == 1) {
                 for (i = top; i <= bottom; i++) {
                      result.append(s.charAt(line*i+right));
                 }
                 right--;
             }
             else if (dir == 2) {
                 for (i = right; i >= left; i--) {
                     result.append(s.charAt(line*bottom+i));
                 }
                 bottom--;
             } 
             else if (dir == 3) {
                 for (i = bottom; i >= top; i--) {
                     result.append(s.charAt(line*i+left));
                 }
                 left++;
             }
             // change direction
             dir = (dir + 1) % 4;
        }
        return result.toString();           
    }

    public static String reduction(String s, int key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char origin = (char)((s.charAt(i) - 'A' + 26 - key) % 26 + 'A');
            result.append(origin);
        }       
        return result.toString();
    }

    public static String getQ1ResponseText(String key, String message) {
        String result = decrypt(key, message);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateToStr = format.format(new Date());
        return String.format("%s,%s\n%s\n%s\n", TeamID.toString(), TeamAWSID.toString(), DateToStr.toString(), result.toString());
    }
	
	/*
	 * start - starts the server
	 */
  	public static void main(final String[] args) {
        Undertow server = Undertow.builder().addHttpListener(80, "ec2-54-164-172-219.compute-1.amazonaws.com")
                .setHandler(new HttpHandler() {
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        String key = exchange.getQueryParameters().get("key").getFirst();
                        String message = exchange.getQueryParameters().get("message").getFirst();
                        //String key = "4024123659485622445001958636275419709073611535463684596712464059093821";
                        //String message = "URYEXYBJB";
                        String result = getQ1ResponseText(key, message);                       
                        exchange.getResponseSender().send(result);
                    }
                }).build();
        server.start();
	}
}
