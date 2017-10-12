import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.DriverManager;

public class TweetDAO {
    //private Connection con = null;
    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String DBURL = "jdbc:mysql://localhost:3306/15619?useUnicode=true&characterEncoding=utf8";
    private String tableName = "q3";
    public static List<Connection> connectionPool = new ArrayList<Connection>();
    private int MAX_CONNECTION = 200;


    public TweetDAO() {
        initial();
    }

    public static synchronized Connection createConnection() throws Exception {
        if (connectionPool.size() > 0) {
            return connectionPool.remove(connectionPool.size() - 1);
        }
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot Find MySQL Driver.");
            throw new Exception(e);
        }
        try {
            return (Connection) DriverManager.getConnection(DBURL, "root", "root");
        } catch (SQLException e) {
            System.out.println("Cannot Get Connection.");
            throw new Exception(e);
        }
    }

    public static synchronized void putbackconnection(Connection con) {
        connectionPool.add(con);
    }

    private void initial() {
        for (int i = 0; i < MAX_CONNECTION; ++i) {
            Connection con;
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException e) {
                System.out.println("Cannot Find MySQL Driver.");
            }
            try {
                con =  (Connection) DriverManager.getConnection(DBURL, "root", "root");
                connectionPool.add(con);
            } catch (SQLException e) {
                System.out.println("Initial: Cannot Get Connection.");
            }
        }
    }

    public String doq4(String tweetid, String fields, String payload, String op) {
        if (op.equals("get")) {
            return q4Get(tweetid, fields);
        }
        return q4Put(tweetid, fields, payload);
    }

    public String q4Put(String tweetid, String fields, String payload) {
	    String flag = "";
        String sql = null;

        String[] flds = fields.split(",");
        int len = flds.length;
        String[] values = new String[len];
        String[] vals = payload.split(",");
        int index = 0;
        for (String v : vals) {
            values[index] = v;
            index++;
        }
        for (int i = index; i < len; ++i) {
            values[i] = "";
        }

        sql = "INSERT INTO q4 (tweetid," + fields + ") VALUES (?," ;
        for (int i = 0; i < len; ++i) {
            sql = sql + "?,";
        }
        sql = sql.substring(0, sql.length() - 1);
        sql = sql + ") ON DUPLICATE KEY UPDATE ";
        for (int i = 0; i < len; ++i) {
            sql = sql + flds[i] + "=?,";
        }
        sql = sql.substring(0, sql.length() - 1);

        //System.out.println(sql);
        Connection con = null;
        try {
	       con = createConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, tweetid);

            for (int i = 1; i <= len; ++i) {
                stmt.setString(i+1, values[i-1]);
            }
            for (int i = 1; i <= len; ++i) {
                stmt.setString(len+i+1, values[i-1]);
            }
            //System.out.println(stmt.toString());
	       //System.out.println("got statement");
            stmt.execute();
            //System.out.println("Inserted into table");
            stmt.close();

	       flag = "success";
        } catch (Exception e) {
	       e.printStackTrace();
	       flag = "exception";
	        //System.out.println("Q4 Put Excetpion");
            //return "exception";
        }  finally {
            putbackconnection(con);
        }
        // finally {
        //     if (con != null) {
        //         try {
        //             con.close();
        //         } catch (SQLException e1) {
        //             e1.printStackTrace();
        //         }
        //     }
        // }
        // putbackconnection(con);

        return flag;
    }

    public String q4Get(String tweetid, String fields) {
        String result = "";
        Connection con = null;
        try {
            con = createConnection();
            String sql = "select " + fields + " from q4 where tweetid = " + tweetid;
            //System.out.println(sql);
            Statement stmt = con.createStatement();
            stmt.execute(sql);
            String[] flds = fields.split(",");
            ResultSet rs = stmt.getResultSet();
            if (rs.next()) {
                for (String f : flds) {
                    result = result + rs.getString(f);
                }
            }
            //System.out.println("Finish getting from database");
            rs.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            putbackconnection(con);
        }
        // finally {
        //     if (con != null) {
        //         try {
        //             con.close();
        //         } catch (SQLException e1) {
        //             e1.printStackTrace();
        //         }
        //     }
        // }
        // putbackconnection(con);

        return result.replaceAll(" ", "+");
    }
    /**
     * This method is used for Q2.
     */
    public String getTweetStringByUseridAndHashtag(String userId, String hashtag) {
	String tweets = "";
    Connection con = null;
        try {
            con = createConnection();
            String sql = "select tweet_text from q2 where id = binary ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, userId + "&" + hashtag);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
		tweets = rs.getString("tweet_text");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            putbackconnection(con);
        }
    //     finally {
	   //  if (con != null) {
    //             try {
    //                 con.close();
    //             } catch (SQLException e1) {
    //                 e1.printStackTrace();
    //             }
    //         }
	   // }
        // putbackconnection(con);
        return tweets;
    }
    /**
     * This method is used for Q3.
     */
    public String getwordcount(String sd, String ed, String su, String eu, String words) {
        Map<String, Integer> wordcountmap = new HashMap<String, Integer>();
        String[] wordslist = words.split(",");
        wordcountmap.put(wordslist[0], 0);
        wordcountmap.put(wordslist[1], 0);
        wordcountmap.put(wordslist[2], 0);
        sd = sd.replace("-", "");
        ed = ed.replace("-", "");
        Connection con = null;
        StringBuilder test = new StringBuilder();
        try {
            con = createConnection();
            String sql = "select words from q3 where user_id>=? and user_id<=? and date>=? and date<=?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, su);
            stmt.setString(2, eu);
            stmt.setString(3, sd);
            stmt.setString(4, ed);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String text = rs.getString("words");
                test.append(text + "\n");
                String[] list = text.split(" +");
                for (String s : list) {
                    String[] wc = s.split(":");
                    if (wordcountmap.containsKey(wc[0])) {
                        wordcountmap.put(wc[0], wordcountmap.get(wc[0]) + Integer.parseInt(wc[1]));
                    }
                }
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("su: " + su + ", eu: " + eu + ", sd: " + sd + ", ed: " + ed);
            System.out.println(test.toString());
        } finally {
            putbackconnection(con);
        }
        StringBuilder result = new StringBuilder();
        for (String s : wordslist) {
            result.append(s).append(":").append(wordcountmap.get(s)).append("\n");
        }
        return result.toString();
    }
}
